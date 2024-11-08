package server;

import chess.ChessGame;
import com.google.gson.Gson;
import service.request.*;
import service.response.*;
import exception.*;
import dataaccess.ServerErrorException;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public RegisterResponse register(String username, String password, String email) throws ResponseException {
        var body = new Gson().toJson(new RegisterRequest(username, password, email));
        return this.makeRequest("POST", "/user", null, body, RegisterResponse.class);
    }

    public LoginResponse login(String username, String password) throws ResponseException {
        var body = new Gson().toJson(new LoginRequest(username, password));
        return this.makeRequest("POST", "/session", null, body, LoginResponse.class);
    }

    public void logout(String authToken) throws ResponseException {
        this.makeRequest("DELETE", "/session", authToken, null, LogoutResponse.class);
    }

    public CreateGameResponse createGame(String authToken, String name) throws ResponseException {
        var body = new Gson().toJson(Map.of("gameName", name));
        return this.makeRequest("POST", "/game", authToken, body, CreateGameResponse.class);
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        return this.makeRequest("GET", "/game", authToken, null, ListGamesResponse.class);
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
        }

        int statusCode;

        if (e instanceof ServerErrorException) {
            statusCode = ((ServerErrorException) e).statusCode();
        } else {
            statusCode = 500;
        }

        return new ResponseException(statusCode, e.getMessage());
    }

}
