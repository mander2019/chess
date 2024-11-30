package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DAO;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private DAO dao;

    public WebSocketHandler(DAO dao) {
        this.dao = dao;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand cmd = new Gson().fromJson(message, UserGameCommand.class);

        UserGameCommand.CommandType type = cmd.getCommandType();
        String auth = cmd.getAuthToken();
        int gameID = cmd.getGameID();

        AuthData authData = null;
        GameData gameData = null;

        try {
            authData = getAuthData(auth);
            gameData = getGameData(gameID);
        } catch (DataAccessException e) {
            connections.broadcast(session, "Error: " + e.getMessage(), gameID);
            return;
        }

        if (authData == null) {
            connections.broadcast(session, "Error: No auth token provided", gameID);
            return;
        } else if (gameData == null) {
            connections.broadcast(session, "Error: Game not found", gameID);
            return;
        }

//        System.out.println("TESTING - ONMESSAGE: " + message);

        switch (cmd.getCommandType()) {
            case CONNECT -> connect(session, authData.username(), gameData);
//            case MAKE_MOVE -> makeMove(session, authData.username(), gameData, cmd.getMove());
//            case LEAVE -> leave(session, authData.username(), gameData);
//            case RESIGN -> resign(session, authData.username(), gameData);
        }
    }

    void connect(Session session, String username, GameData gameData) throws IOException {

        connections.add(gameData.gameID(), session);
        connections.send(session, new Gson().toJson(gameData.game()));

        String message;

        if (gameData.whiteUsername().equals(username)) {
            message = username + " has joined the game as white";
        } else if (gameData.blackUsername().equals(username)) {
            message = username + " has joined the game as black";
        } else {
            message = username + " is now observing the game";
        }

        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

//        System.out.println("TESTING - CONNECT: " + message);

        connections.broadcast(session, new Gson().toJson(notification), gameData.gameID());
        connections.send(session, new Gson().toJson(notification));
    }

    // Template code for websocket methods

//    private void enter(String visitorName, Session session) throws IOException {
//        connections.add(visitorName, session);
//        var message = String.format("%s is in the shop", visitorName);
//        var notification = new Notification(Notification.Type.ARRIVAL, message);
//        connections.broadcast(visitorName, notification);
//    }
//
//    private void exit(String visitorName) throws IOException {
//        connections.remove(visitorName);
//        var message = String.format("%s left the shop", visitorName);
//        var notification = new Notification(Notification.Type.DEPARTURE, message);
//        connections.broadcast(visitorName, notification);
//    }
//
//    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var notification = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast("", notification);
//        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }


    private AuthData getAuthData(String authToken) throws DataAccessException {
        Collection<AuthData> auths = dao.getAuths();

        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }

        return null;
    }

    private GameData getGameData(int gameID) throws DataAccessException {
        Collection<GameData> games = dao.getGames();

        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }

        return null;
    }

}