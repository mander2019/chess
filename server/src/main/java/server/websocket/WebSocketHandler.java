package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DAO;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final ConnectionManager connections = new ConnectionManager();
    private final DAO dao;
    private ServerMessage notification;
    private ServerMessage error;
    private ServerMessage loadGame;

    public WebSocketHandler(DAO dao) {
        this.dao = dao;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand cmd = new Gson().fromJson(message, UserGameCommand.class);

        UserGameCommand.CommandType type = cmd.getCommandType();
        String auth = cmd.getAuthToken();
        int gameID = cmd.getGameID();

        AuthData authData;
        GameData gameData;

        try {
            authData = getAuthData(auth);
            gameData = getGameData(gameID);
        } catch (DataAccessException e) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            connections.send(session, new Gson().toJson(error));
            return;
        }

        if (authData == null) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: No auth token provided");
            connections.send(session, new Gson().toJson(error));
            return;
        } else if (gameData == null || gameData.game() == null) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: No game found with that ID");
            connections.send(session, new Gson().toJson(error));
            return;
        }

        String username = authData.username();
        String move = null;

        if (type == UserGameCommand.CommandType.MAKE_MOVE) {
            move = new Gson().toJson(cmd.getMove());
        }

        switch (type) {
            case CONNECT -> connect(session, username, gameData);
            case MAKE_MOVE -> makeMove(session, username, gameData, move);
            case LEAVE -> leave(session, username, gameData);
//            case RESIGN -> resign(session, username, gameData);
        }
    }

    void connect(Session session, String username, GameData gameData) throws IOException {
        connections.add(username, session);

        loadGame = new ServerMessage(gameData.game());
        connections.send(session, new Gson().toJson(loadGame));

        String message;

        if (gameData.whiteUsername().equals(username)) {
            message = username + " has joined the game as white";
        } else if (gameData.blackUsername().equals(username)) {
            message = username + " has joined the game as black";
        } else {
            message = username + " is now observing the game";
        }

        notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

        connections.broadcast(session, new Gson().toJson(notification));
    }

    void makeMove(Session session, String username, GameData gameData, String move) throws IOException {

        ChessGame game = gameData.game();
        ChessMove chessMove = new Gson().fromJson(move, ChessMove.class);
        ChessGame.TeamColor currentTurn = game.getTeamTurn();

        ChessGame.TeamColor playerColor;

        // Ensure observer is not making moves
        if (!gameData.whiteUsername().equals(username) && !gameData.blackUsername().equals(username)) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: You are not a player in this game");
            connections.send(session, new Gson().toJson(error));
            return;
        }

        try {
            game.makeMove(chessMove);
        } catch (Exception e) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            connections.send(session, new Gson().toJson(error));
            return;
        }

        GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);

        try {
            updateGameData(updatedGameData);
        } catch (DataAccessException e) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            connections.send(session, new Gson().toJson(error));
            return;
        }

        loadGame = new ServerMessage(updatedGameData.game());
        connections.send(session, new Gson().toJson(loadGame));
        connections.broadcast(session, new Gson().toJson(loadGame));


        String message = username + " has made a move: " + moveToString(chessMove);

        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

        connections.send(session, new Gson().toJson(notification));
        connections.broadcast(session, new Gson().toJson(notification));

        ChessGame.TeamColor otherPlayer = game.getTeamTurn();

        if (game.isInCheckmate(otherPlayer)) {
            notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Checkmate!\n" + currentTurn.toString() + " wins!");
            connections.broadcast(session, new Gson().toJson(notification));
        }

        if (game.isInStalemate(otherPlayer)) {
            notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate!\nIt's a draw!");
            connections.broadcast(session, new Gson().toJson(notification));
        }

        if (game.isInCheck(otherPlayer)) {
            notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, otherPlayer.toString() + " is in check!");
            connections.broadcast(session, new Gson().toJson(notification));
        }
    }

    void leave(Session session, String username, GameData gameData) throws IOException {
        String message = username + " has left the game";

        try {
            if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
                updateGameData(new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game()));
            } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
                updateGameData(new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game()));
            } else {
                message = username + " has stopped observing the game";
            }
        } catch (DataAccessException e) {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            connections.send(session, new Gson().toJson(error));
            return;
        }

        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

        connections.send(session, new Gson().toJson(notification));
        connections.broadcast(session, new Gson().toJson(notification));
        connections.remove(username);
    }

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

    private void updateGameData(GameData gameData) throws DataAccessException {
        dao.updateGame(gameData);
    }

    private String moveToString(ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        return positionToString(start) + " to " + positionToString(end);
    }

    private String positionToString(ChessPosition pos) {

        String col = switch (pos.getColumn()) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> throw new IllegalArgumentException("Invalid column: " + pos.getColumn());
        };

        String row = String.valueOf(pos.getRow());
        return col + row;
    }
}