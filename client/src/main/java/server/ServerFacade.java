package server;

import chess.ChessGame;
import chess.ChessMove;
import client.Client;
import client.websocket.NotificationHandler;
import com.google.gson.Gson;
import model.*;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.response.*;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Map;

import exception.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ServerFacade extends Endpoint {
    private String serverUrl;
    NotificationHandler notificationHandler;
    Session session;
    Client client;

    public ServerFacade(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        client = notificationHandler.getClient();
    }

    private void webSocketConnection() throws ResponseException {
        try {
            String WS_serverUrl = serverUrl.replace("http", "ws");
            URI socketURI = new URI(WS_serverUrl + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void enterGame(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move));
    }

    public void leaveGame(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
        this.session.close();
    }

    public void resignGame(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private void sendCommand(UserGameCommand cmd) throws IOException {
        session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

//    public void enterPetShop(String visitorName) throws ResponseException {
//        try {
//            var action = new Action(Action.Type.ENTER, visitorName);
//            this.session.getBasicRemote().sendText(new Gson().toJson(action));
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    public void leavePetShop(String visitorName) throws ResponseException {
//        try {
//            var action = new Action(Action.Type.EXIT, visitorName);
//            this.session.getBasicRemote().sendText(new Gson().toJson(action));
//            this.session.close();
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public String register(String username, String password, String email) throws ResponseException {
        var body = new Gson().toJson(new RegisterRequest(username, password, email));

        RegisterResponse response = this.makeRequest("POST", "/user", null, body, RegisterResponse.class);

        return response.authToken();
    }

    public String login(String username, String password) throws ResponseException {
        var body = new Gson().toJson(new LoginRequest(username, password));
        LoginResponse response = this.makeRequest("POST", "/session", null, body, LoginResponse.class);

        return response.authToken();
    }

    public void logout(String authToken) throws ResponseException {
        this.makeRequest("DELETE", "/session", authToken, null, LogoutResponse.class);
    }

    public int createGame(String authToken, String name) throws ResponseException {
        var body = new Gson().toJson(Map.of("gameName", name));
        CreateGameResponse response = this.makeRequest("POST", "/game", authToken, body, CreateGameResponse.class);

        return response.gameID();
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        ListGamesResponse response = this.makeRequest("GET", "/game", authToken, null, ListGamesResponse.class);

        return response.games();
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, String gameID) throws ResponseException {
        String color;
        if (teamColor == ChessGame.TeamColor.BLACK) {
            color = "black";
        } else {
            color = "white";
        }

        var body = new Gson().toJson(Map.of("playerColor", color, "gameID", gameID));
        this.makeRequest("PUT", "/game", authToken, body, JoinGameResponse.class);

        webSocketConnection();
    }

    public void clearData() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null, ClearResponse.class);
    }

    private <T> T makeRequest(String method, String path, String header, String body, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeHeader(header, http);
            writeBody(body, http);

            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception e) {
            throw errorMessageHelper(e);
        }
    }

    private static void writeHeader(String header, HttpURLConnection http) {
        if (header != null) {
            http.setRequestProperty("Authorization", header);
        }
    }

    private static void writeBody(String body, HttpURLConnection http) throws IOException {
        if (body != null) {
            try (var os = http.getOutputStream()) {
                var input = body.getBytes();
                os.write(input, 0, input.length);
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }

        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();

        if (!isSuccessful(status)) {
            throw new ResponseException(status, http.getResponseMessage());
        }
    }

    private boolean isSuccessful(int status) {
        return status >= 200 && status < 300;
    }

    private ResponseException errorMessageHelper(Exception e) {
        if (e instanceof ResponseException) {
            return (ResponseException) e;
        } else {
            return new ResponseException(500, e.getMessage());
        }
    }

}
