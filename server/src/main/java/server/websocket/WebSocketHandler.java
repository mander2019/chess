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
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {
//    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
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
            case RESIGN -> resign(session, username, gameData);
        }
    }

    void connect(Session session, String username, GameData gameData) throws IOException {
//        connections.add(username, session);
        connections.add(username, session, String.valueOf(gameData.gameID()));


        loadGame = new ServerMessage(gameData.game());
        connections.send(session, new Gson().toJson(loadGame));

        String message = "\n";
        String gameID = String.valueOf(gameData.gameID());

        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            message += username + " has joined the game (" + gameID + ") as white";
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            message += username + " has joined the game (" + gameID + ") as black";
        } else {
            message += username + " is now observing the game (" + gameID + ")";
        }

        message += "\n";

        if (isGameOver(session, gameData)) {
            message += "note: the game is already over!\n";
        }

        notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, new Gson().toJson(notification), gameID);
//        connections.send(session, new Gson().toJson(notification));
    }

    void makeMove(Session session, String username, GameData gameData, String move) throws IOException {

        ChessGame game = gameData.game();
        ChessMove chessMove = new Gson().fromJson(move, ChessMove.class);

        // Don't allow moves if game is over
        if (isGameOver(session, gameData)) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: The game is already over");
            connections.send(session, new Gson().toJson(error));
            return;
        }

        // Ensure observer is not making moves
        if (!gameData.whiteUsername().equals(username) && !gameData.blackUsername().equals(username)) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: You are not a player in this game");
            connections.send(session, new Gson().toJson(error));
            return;
        }

        ChessGame.TeamColor playerColor;
        if (gameData.whiteUsername().equals(username)) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else {
            playerColor = ChessGame.TeamColor.BLACK;
        }

        if (game.getTeamTurn() != playerColor) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: It is not your turn");
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
        connections.broadcast(session, new Gson().toJson(loadGame), String.valueOf(gameData.gameID()));

        // Message to opponent and observers
        String message = username + " has made a move: " + moveToString(chessMove);

        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, new Gson().toJson(notification), String.valueOf(gameData.gameID()));

        checkGameStatus(session, updatedGameData);
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

//        connections.send(session, new Gson().toJson(notification));
        connections.broadcast(session, new Gson().toJson(notification), String.valueOf(gameData.gameID()));
        connections.remove(session, String.valueOf(gameData.gameID()));
    }

    void resign(Session session, String username, GameData gameData) throws IOException {
        ChessGame game = gameData.game();

        if (game.getWinner() != null || game.isDraw()) {
            String message = "the game is already over!";
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            connections.send(session, new Gson().toJson(error));
            return;
        }

        if (!gameData.whiteUsername().equals(username) && !gameData.blackUsername().equals(username)) {
            error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: You are not a player in this game");
            connections.send(session, new Gson().toJson(error));
            return;
        }

        ChessGame.TeamColor winningPlayer;
//        String winningUsername;
        if (gameData.whiteUsername().equals(username)) {
            winningPlayer = ChessGame.TeamColor.BLACK;
//            winningUsername = gameData.blackUsername();
        } else {
            winningPlayer = ChessGame.TeamColor.WHITE;
//            winningUsername = gameData.whiteUsername();
        }

        ChessGame.TeamColor losingPlayer = switch (winningPlayer) {
            case BLACK -> ChessGame.TeamColor.WHITE;
            case WHITE -> ChessGame.TeamColor.BLACK;
        };

        String message = losingPlayer + " has resigned the game";
        message += "\n" + winningPlayer + " wins!";

        game.setWinner(winningPlayer);

        try {
            updateGameData(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));
        } catch (DataAccessException e) {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            connections.send(session, new Gson().toJson(error));
            return;
        }

        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

        connections.send(session, new Gson().toJson(notification));
        connections.broadcast(session, new Gson().toJson(notification), String.valueOf(gameData.gameID()));
    }

    private void checkGameStatus(Session session, GameData gameData) throws IOException {
        checkGameStatusHelper(session, gameData, ChessGame.TeamColor.WHITE);
        checkGameStatusHelper(session, gameData, ChessGame.TeamColor.BLACK);
    }

    private void checkGameStatusHelper (Session session, GameData gameData, ChessGame.TeamColor color) throws IOException {
        ChessGame game = gameData.game();
        ChessGame.TeamColor otherColor = switch (color) {
            case BLACK -> ChessGame.TeamColor.WHITE;
            case WHITE -> ChessGame.TeamColor.BLACK;
        };

        if (isGameOver(session, gameData)) {
            return;
        }

        if (game.isInCheckmate(color)) {
            notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Checkmate!\n" + otherColor + " wins!");
            connections.broadcast(session, new Gson().toJson(notification), String.valueOf(gameData.gameID()));
            connections.send(session, new Gson().toJson(notification));
        }

        if (game.isInStalemate(color)) {
            notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate!\nIt's a draw!");
            connections.broadcast(session, new Gson().toJson(notification), String.valueOf(gameData.gameID()));
            connections.send(session, new Gson().toJson(notification));
        }

        if (game.isInCheck(color)) {
            notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, color.toString() + " is in check!");
            connections.broadcast(session, new Gson().toJson(notification), String.valueOf(gameData.gameID()));
            connections.send(session, new Gson().toJson(notification));
        }
    }

    private boolean isGameOver(Session session, GameData gameData) throws IOException {
        ChessGame game = gameData.game();

        ChessGame.TeamColor winnerColor = game.getWinner();;

        if (game.getWinner() != null) { // Check for winner
            return true;
        }

        if (game.isDraw()) { // Check for stalemate
            return true;
        }

        return false;
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