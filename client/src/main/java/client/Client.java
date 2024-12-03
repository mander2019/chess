package client;

import chess.*;
import client.websocket.NotificationHandler;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import ui.EscapeSequences;
import server.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private String username;
    protected final ServerFacade serverFacade;
    protected LoginState state = LoginState.SIGNEDOUT;
    protected GameState gameState = GameState.NONE;
    private ChessGame currentGame;
    protected Integer currentGameID = -1;
    protected ChessGame.TeamColor playerColor;
    private Collection<AuthData> auths = new ArrayList<>();
    protected Collection<GameData> games = new ArrayList<>();
    protected Map<Integer, GameData> gameDirectory = new HashMap<>();
    private ClientHelper helper;
    
    public Client(String serverUrl, NotificationHandler notificationHandler) {
        serverFacade = new ServerFacade(serverUrl, notificationHandler);
        helper = new ClientHelper();
    }

    protected Client() {
        serverFacade = null;
    }

    public String eval(String input) {
        return helper.eval(input);
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            username = params[0];
            var password = params[1];
            var email = params[2];

            try {
                addAuth(serverFacade.register(username, password, email));
            } catch (ResponseException e) {
                if (e.getStatusCode() == 403) {
                    return "Username already taken\n";
                } else if (e.getStatusCode() == 400) {
                    return "Bad input.\nExpected: <" + magentaString("USERNAME") + "> <"
                            + magentaString("PASSWORD") + "> <" + magentaString("EMAIL") + ">\n";
                } else {
                    throw e;
                }
            }
            state = LoginState.SIGNEDIN;
            return "You have been successfully registered and signed in as " + greenString(username) + "\n";
        }
        throw new ResponseException(400, "Bad input.\nExpected: <" + magentaString("USERNAME") + "> <"
                                         + magentaString("PASSWORD") + "> <" + magentaString("EMAIL") + ">\n");
    }

    public String login(String... params) throws ResponseException {
        if (state == LoginState.SIGNEDIN) {
            throw new ResponseException(400, "You are already signed in\n");
        }

        if (params.length == 2) {
            username = params[0];
            var password = params[1];
            try {
                addAuth(serverFacade.login(username, password));
            } catch (ResponseException e) {
                if (e.getStatusCode() == 401) {
                    return "Invalid login credentials\n";
                } else if (e.getStatusCode() == 400) {
                    return "Bad input\nExpected: <" + magentaString("USERNAME") + "> <"
                            + magentaString("PASSWORD") + ">\n";
                } else {
                    throw e;
                }
            }
            state = LoginState.SIGNEDIN;
            return "You have been successfully signed in as " + greenString(username) + "\n";
        }
        throw new ResponseException(400, "Bad input\nExpected: <" + magentaString("USERNAME") + "> <"
                                          + magentaString("PASSWORD") + ">\n");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        String auth;
        try {
            auth = getAuthToken();
            serverFacade.logout(auth);
        } catch (ResponseException e) {
            if (e.getStatusCode() == 401) {
                return "Logout error\n";
            } else {
                throw e;
            }
        }
        removeAuth(auth);
        state = LoginState.SIGNEDOUT;
        playerColor = null;
        currentGame = null;
        gameState = GameState.NONE;
        currentGameID = -1;
        return "You have been successfully signed out\n";
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 1) {
            var name = params[0];
            try {
                int gameID = serverFacade.createGame(getAuthToken(), name);
                games.add(new GameData(gameID, null, null, name, new ChessGame()));
            } catch (ResponseException e) {
                if (e.getStatusCode() == 400) {
                    return "Bad input\nExpected: <" + magentaString("NAME") + ">\n";
                } else if (e.getStatusCode() == 401) {
                    return "Unauthorized\n";
                } else {
                        throw e;
                }
            }
            return "Game " + greenString(name) + " has been successfully created\n";
        } else {
            return "Bad input\nExpected: <" + magentaString("NAME") + ">\n";
        }
    }

    public String list() throws ResponseException {
        assertSignedIn();
        try {
            updateGameDirectory();

            if (games.isEmpty()) {
                return "No games found\n";
            }

            return listHelper();
        } catch (ResponseException e) {
            if (e.getStatusCode() == 401) {
                return "Unauthorized\n";
            } else {
                throw e;
            }
        }
    }

    private String listHelper() {
        String output = "ID | NAME | WHITE | BLACK\n";
        int gameNumber = 1;

        for (GameData game : games) {

            String blackPlayer = game.blackUsername();
            String whitePlayer = game.whiteUsername();

            if (blackPlayer == null) {
                blackPlayer = "empty";
            }
            if (whitePlayer == null) {
                whitePlayer = "empty";
            }

            output += gameNumber + "\t" + game.gameName() + "\t";
            output += whitePlayer + "\t" + blackPlayer + "\n";
            gameNumber++;
        }
        return output;
    }

    protected void updateGameDirectory() throws ResponseException {
        gameDirectory = new HashMap<>();
        games = serverFacade.listGames(getAuthToken());
        int gameNumber = 1;
        for (GameData game : games) {
            gameDirectory.put(gameNumber, game);
            gameNumber++;
        }
    }

    public String joinGame(String... params) throws ResponseException {
        return helper.joinGame(params);
    }

    public String observe(String... params) throws ResponseException {
        assertSignedIn();
        updateGameDirectory();
        if (params.length == 1) {
            int gameID;
            int directoryIndex;
            try { // Convert input to gameID
                directoryIndex = Integer.parseInt(params[0]);

                if (directoryIndex > games.size() || directoryIndex <= 0) {
                    return "Game not found\n";
                }

                gameID = gameDirectory.get(directoryIndex).gameID();
            } catch (NumberFormatException e) {
                return "Bad <" + magentaString("ID")+ "> input\nExpected: <" + magentaString("ID") + "> <" + magentaString("COLOR") + ">\n";
            }

            gameState = GameState.OBSERVING;
            currentGame = getGame(gameID);
            if (currentGame == null) {
                throw new ResponseException(400, "Game not found\n");
            }

            try {
                serverFacade.enterGame(getAuthToken(), gameID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return "you have successfully joined game " + gameID + " as an observer\n";
        } else {
            throw new ResponseException(400, "Bad input\nExpected: <" + magentaString("ID") + ">\n");
        }
    }

    public String makeMove(String... params) throws ResponseException {
        assertSignedIn();
        ChessGame game = getCurrentGame();
        if (game == null) {
            return "You are not currently in a game\n";
        }

        if (params.length == 2 || params.length == 3) {
            ChessPosition startPos = getChessPosition(params[0]);
            ChessPosition endPos = getChessPosition(params[1]);
            ChessPiece.PieceType promotionType;
            ChessMove move;

            if (params.length == 3) {
                String inputPromotionType = params[2].toLowerCase();
                switch (inputPromotionType) {
                    case "rook" -> promotionType = ChessPiece.PieceType.ROOK;
                    case "knight" -> promotionType = ChessPiece.PieceType.KNIGHT;
                    case "bishop" -> promotionType = ChessPiece.PieceType.BISHOP;
                    case "queen" -> promotionType = ChessPiece.PieceType.QUEEN;
                    default -> { return "Bad input\nPromotion piece not recognized\n"; }
                }

                move = new ChessMove(startPos, endPos, promotionType);
            } else {
                move = new ChessMove(startPos, endPos, null);
            }

            try {
                serverFacade.makeMove(getAuthToken(), currentGameID, move);
            } catch (Exception e) {
                return e.getMessage();
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return printGame(getCurrentGame(), playerColor);
        } else {
            return "Bad input\nExpected: <" + magentaString("START") + "> <" + magentaString("END") + ">\n";
        }
    }

    public String resign() throws ResponseException {
        assertSignedIn();
        if (isPlaying()) {
            try {
                serverFacade.resignGame(getAuthToken(), currentGameID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "You have successfully resigned\n";
        } else {
            return "You are not currently in a game\n";
        }
    }

    public String redraw() throws ResponseException {
        assertSignedIn();
        ChessGame game = getCurrentGame();

        if (isObserving()) {
            return printGame(game, ChessGame.TeamColor.WHITE);
        } else if (isPlaying() && !isGameOver(game)) {
            return printGame(game, playerColor);
        } else if (isPlaying() && isGameOver(game)) {
            return "";
        } else {
            return "You are not currently in a game\n";
        }
    }

    public String leave() throws ResponseException {
        assertSignedIn();
        if (isPlaying() || isObserving()) {
            try {
                serverFacade.leaveGame(getAuthToken(), currentGameID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gameState = GameState.NONE;
            playerColor = null;
            currentGame = null;
            currentGameID = -1;
            return "You have successfully left the game\n";
        } else {
            return "You are not currently in a game\n";
        }
    }

    public String moves(String... params) {
        return helper.moves(params);
    }

    protected ChessMove getMoveFromList(ChessPosition end, Collection<ChessMove> moves) {
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(end)) {
                return move;
            }
        }
        return null;
    }

    public String printGame(ChessGame game, ChessGame.TeamColor color) {
        return helper.printGame(game, color);
    }

    public String help() {
        return helper.help();
    }

    public LoginState getState() {
        return state;
    }

    public String getUsername() {
        return username;
    }

    private void addAuth(String authToken) throws ResponseException {
        auths.add(new AuthData(authToken, username));
    }

    private void removeAuth(String auth) {
        for (AuthData a : auths) {
            if (a.authToken().equals(auth)) {
                auths.remove(a);
                break;
            }
        }
    }

    protected String getAuthToken() {
        for (AuthData auth : auths) {
            if (auth.username().equals(username)) {
                return auth.authToken();
            }
        }
        return null;
    }

    protected ChessGame getGame(int id) {
        for (GameData game : games) {
            if (game.gameID() == id) {
                return game.game();
            }
        }
        return null;
    }

    public void updateCurrentGame(ChessGame game) {
        currentGame = game;
    }

    public ChessGame getCurrentGame() {
        return currentGame;
    }

    private boolean isGameOver(ChessGame game) {
        return game.getWinner() != null || game.isDraw();
    }

    protected ChessPosition getChessPosition(String position) {
        if (position.length() != 2) {
            return null;
        }

        String row = position.substring(1);
        String col = position.substring(0, 1);

        int rowInt = Integer.parseInt(row) - 1;
        int colInt;

        if (rowInt < 0 || rowInt > 7) {
            return null;
        }

        switch (col) {
            case "a" -> colInt = 0;
            case "b" -> colInt = 1;
            case "c" -> colInt = 2;
            case "d" -> colInt = 3;
            case "e" -> colInt = 4;
            case "f" -> colInt = 5;
            case "g" -> colInt = 6;
            case "h" -> colInt = 7;
            default -> { return null; }
        }

        return new ChessPosition(rowInt + 1, colInt + 1);
    }

    protected void assertSignedIn() throws ResponseException {
        if (state == LoginState.SIGNEDOUT) {
            throw new ResponseException(400, "You must be signed in to perform this action\n");
        }
    }

    public String blueString(String str) {
        return EscapeSequences.SET_TEXT_COLOR_BLUE + str + EscapeSequences.RESET_TEXT_COLOR;
    }

    public String magentaString(String str) {
        return EscapeSequences.SET_TEXT_COLOR_MAGENTA + str + EscapeSequences.RESET_TEXT_COLOR;
    }

    public String redString(String str) {
        return EscapeSequences.SET_TEXT_COLOR_RED + str + EscapeSequences.RESET_TEXT_COLOR;
    }

    public String greenString(String str) {
        return EscapeSequences.SET_TEXT_COLOR_GREEN + str + EscapeSequences.RESET_TEXT_COLOR;
    }

    public enum GameState {
        PLAYING, OBSERVING, NONE
    }

    public GameState getGameState() {
        return gameState;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }

    protected boolean isPlaying() {
        return gameState == GameState.PLAYING;
    }

    protected boolean isObserving() {
        return gameState == GameState.OBSERVING;
    }

    protected boolean isSignedIn() {
        return state == LoginState.SIGNEDIN;
    }

}
