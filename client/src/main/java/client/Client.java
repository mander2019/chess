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
    private final ServerFacade serverFacade;
//    private String serverUrl;
//    private NotificationHandler notificationHandler;
    private LoginState state = LoginState.SIGNEDOUT;
    private GameState gameState = GameState.NONE;
    private ChessGame currentGame;
    private Integer currentGameID = -1;
    private ChessGame.TeamColor playerColor;
    private Collection<AuthData> auths = new ArrayList<>();
    private Collection<GameData> games = new ArrayList<>();
    private Map<Integer, GameData> gameDirectory = new HashMap<>();
    
    public Client(String serverUrl, NotificationHandler notificationHandler) {
        serverFacade = new ServerFacade(serverUrl, notificationHandler);
//        this.serverUrl = serverUrl;
//        this.notificationHandler = notificationHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            String function;
            if (tokens.length > 0) {
                function = tokens[0];
            } else {
                function = "help";
            }
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == LoginState.SIGNEDOUT) {
                return switch (function) {
                    case "register" -> register(params);
                    case "login" -> login(params);
                    case "quit" -> "quit";
                    default -> help();
                };
            } else if (state == LoginState.SIGNEDIN && gameState == GameState.NONE) {
                return switch (function) {
                    case "create" -> createGame(params);
                    case "list" -> list();
                    case "join" -> joinGame(params);
                    case "observe" -> observe(params);
                    case "logout" -> logout();
                    case "quit" -> "quit";
                    default -> help();
                };
            } else if (state == LoginState.SIGNEDIN && gameState == GameState.PLAYING) {
                return switch (function) {
                    case "move" -> makeMove(params);
                    case "moves" -> moves(params);
                    case "redraw" -> redraw();
                    case "resign" -> resign();
                    case "leave" -> leave();
                    default -> help();
                };
            } else if (state == LoginState.SIGNEDIN && gameState == GameState.OBSERVING) {
                return switch (function) {
                    case "moves" -> moves(params);
                    case "redraw" -> redraw();
                    case "leave" -> leave();
                    default -> help();
                };
            } else {
                return help();
            }
        } catch (Throwable e) {
            return e.getMessage();
        }
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

    private void updateGameDirectory() throws ResponseException {
        gameDirectory = new HashMap<>();
        games = serverFacade.listGames(getAuthToken());
        int gameNumber = 1;
        for (GameData game : games) {
            gameDirectory.put(gameNumber, game);
            gameNumber++;
        }
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        updateGameDirectory();

        if (params.length == 2) {
            int directoryIndex;
            int gameID;
            try { // Convert input to gameID
                directoryIndex = Integer.parseInt(params[0]);

                if (directoryIndex > games.size() || directoryIndex <= 0) {
                    return "Game not found\n";
                }

                gameID = gameDirectory.get(directoryIndex).gameID();
            } catch (NumberFormatException e) {
                return "Bad <" + magentaString("ID")+ "> input\nExpected: <" + magentaString("ID") + "> <" + magentaString("COLOR") + ">\n";
            }

            // Get color
            String color = params[1].toLowerCase();
            ChessGame.TeamColor teamColor;
            if (color.equals("white")) {
                teamColor = ChessGame.TeamColor.WHITE;
            } else if (color.equals("black")) {
                teamColor = ChessGame.TeamColor.BLACK;
            } else {
                return "Bad <" + magentaString("COLOR")+ "> input\nExpected: <" + magentaString("ID") + "> <" + magentaString("COLOR") + ">\n";
            }

            try {
                serverFacade.joinGame(getAuthToken(), teamColor, Integer.toString(gameID));
                gameState = GameState.PLAYING;
                playerColor = teamColor;
                updateCurrentGame(getGame(gameID));
                currentGameID = gameID;

                serverFacade.enterGame(getAuthToken(), gameID);
            } catch (ResponseException e) {
                if (e.getStatusCode() == 400) {
                    return "Bad input\nExpected: <" + magentaString("ID") + "> <" + magentaString("COLOR") + ">\n";
                } else if (e.getStatusCode() == 401) {
                    return "Unauthorized\n";
                } else if (e.getStatusCode() == 403) {
                    return "Game already taken\n";
                } else {
                    return e.getMessage();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String output;
            ChessGame game;

            try {
                game = getGame(gameID);

                if (game == null) {
                    return "Game not found\n";
                }

//                output = printGame(game, teamColor);
                output = "";


//                output = printGame(game, ChessGame.TeamColor.WHITE);
//                output += "\n";
//                output += printGame(game, ChessGame.TeamColor.BLACK);
            } catch (Exception e) {
                return "Game doesn't exist\n";
            }

            return output + "you have successfully joined game " + gameID + " as " + color + "\n";
        } else {
            return "Bad input—incorrect number of arguments\nExpected: <" + magentaString("ID") + "> <" + magentaString("COLOR") + ">\n";
        }
    }

    public String observe(String... params) throws ResponseException {
        assertSignedIn();
        updateGameDirectory();
        String output;

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

//            output = printGame(currentGame, ChessGame.TeamColor.WHITE);
//            output += "\n\n";
//            output += printGame(currentGame, ChessGame.TeamColor.BLACK);

            output = "you have successfully joined game " + gameID + " as an observer\n";

            return output;
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
//            return printGame(getCurrentGame(), playerColor) + "\nMove successful: " + move + "\n";
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

//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        ChessGame game = getCurrentGame();

        if (isObserving()) {
            return printGame(game, ChessGame.TeamColor.WHITE);
        } else if (isPlaying() && !isGameOver(game)) {
            return printGame(game, playerColor);
        } else if (isPlaying() && isGameOver(game)) {
//            if (game.getWinner() != null) {
//                return "\n\nGame over: " + game.getWinner().toString() + " wins\n";
//            } else {
//                return "\n\nGame over: draw\n";
//            }

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
        if (!isPlaying() && !isObserving()) {
            return "You are not currently in a game\n";
        } else if (params.length != 1) {
            return "Bad input\nExpected: <" + magentaString("PIECE") + ">\n";
        }

        ChessPosition position = getChessPosition(params[0]);
        if (position == null) {
            return "Bad input\nExpected: <" + magentaString("PIECE") + ">\n";
        }

        ChessGame.TeamColor observeColor = playerColor;
        if (isObserving()) {
            observeColor = ChessGame.TeamColor.WHITE;
        }

        ChessGame game = getCurrentGame();
        ChessBoard board = game.getBoard();
        ChessPiece[][] pieces = board.getSquares();
        ChessPiece piece = board.getPiece(position);
        ChessPiece.PieceType type = piece.getPieceType();
        ChessGame.TeamColor color = piece.getTeamColor();

        Collection<ChessMove> moves = game.validMoves(position);

        String output = "";
        ChessMove move;
        ChessPosition start = position;
        ChessPosition end;

        if (observeColor == ChessGame.TeamColor.BLACK) {
            output += "   h  g  f  e  d  c  b  a\n";
            for (int i = 0; i < 8; i++) {
                output += (i + 1) + " ";
                for (int j = 7; j >= 0; j--) {
                    end = new ChessPosition(i + 1, j + 1);
                    move = getMoveFromList(end, moves);
                    if (move != null) {
                        output += EscapeSequences.SET_BG_COLOR_GREEN + " " + chessPieceToString(pieces[i][i]);
                    } else if (new ChessPosition(i + 1, j + 1).equals(start)) {
                        output += EscapeSequences.SET_BG_COLOR_YELLOW + " " + chessPieceToString(pieces[i][j]);
                    }

                    else if ((i + j) % 2 == 0) {
                        output += EscapeSequences.SET_BG_COLOR_BLACK + " " + chessPieceToString(pieces[i][j]);
                    } else {
                        output += EscapeSequences.SET_BG_COLOR_WHITE + " " + chessPieceToString(pieces[i][j]);
                    }
                    output += " " + EscapeSequences.RESET_BG_COLOR;
                }
                output += " " + (i + 1) + "\n";
            }
            output += "   h  g  f  e  d  c  b  a\n";
        } else {
            output += "   a  b  c  d  e  f  g  h\n";
            for (int i = 7; i >= 0; i--) {
                output += (i + 1) + " ";
                for (int j = 0; j < 8; j++) {
                    if ((i + j) % 2 == 0) {
                        output += EscapeSequences.SET_BG_COLOR_BLACK + " " + chessPieceToString(pieces[i][j]);
                    } else {
                        output += EscapeSequences.SET_BG_COLOR_WHITE + " " + chessPieceToString(pieces[i][j]);
                    }
                    output += " " + EscapeSequences.RESET_BG_COLOR;
                }
                output += " " + (i + 1) + "\n";
            }
            output += "   a  b  c  d  e  f  g  h\n";
        }

        return output;
    }

    private ChessMove getMoveFromList(ChessPosition end, Collection<ChessMove> moves) {
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(end)) {
                return move;
            }
        }
        return null;
    }

    public String printGame(ChessGame game, ChessGame.TeamColor color) {
        ChessBoard board = game.getBoard();
        ChessPiece[][] pieces = board.getSquares();
//        String output = "\n";
        String output = "";
        if (color == ChessGame.TeamColor.WHITE) {
            output += "   a  b  c  d  e  f  g  h\n";
            for (int i = 7; i >= 0; i--) {
                output += (i + 1) + " ";
                for (int j = 0; j < 8; j++) {
                    if ((i + j) % 2 == 0) {
                        output += EscapeSequences.SET_BG_COLOR_BLACK + " " + chessPieceToString(pieces[i][j]);
                    } else {
                        output += EscapeSequences.SET_BG_COLOR_WHITE + " " + chessPieceToString(pieces[i][j]);
                    }
                    output += " " + EscapeSequences.RESET_BG_COLOR;
                }
                output += " " + (i + 1) + "\n";
            }
            output += "   a  b  c  d  e  f  g  h\n";
        } else {
            output += "   h  g  f  e  d  c  b  a\n";
            for (int i = 0; i < 8; i++) {
                output += (i + 1) + " ";
                for (int j = 7; j >= 0; j--) {
                    if ((i + j) % 2 == 0) {
                        output += EscapeSequences.SET_BG_COLOR_BLACK + " " + chessPieceToString(pieces[i][j]);
                    } else {
                        output += EscapeSequences.SET_BG_COLOR_WHITE + " " + chessPieceToString(pieces[i][j]);
                    }
                    output += " " + EscapeSequences.RESET_BG_COLOR;
                }
                output += " " + (i + 1) + "\n";
            }
            output += "   h  g  f  e  d  c  b  a\n";
        }

        if (game.getWinner() != null) {
            output += "\ngame over: " + game.getWinner().toString().toLowerCase() + " wins\n";
        } else if (game.isDraw()) {
            output += "\ngame over: draw\n\n";
        } else {
            output += "\n——" + game.getTeamTurn().toString().toLowerCase() + "'s turn——\n";
        }

        return output;
    }

    private String chessPieceToString(ChessPiece piece) {

        if  (piece == null) {
            return " ";
        }

        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();
        String string = "";

        if (color == ChessGame.TeamColor.WHITE) {
            string += EscapeSequences.SET_TEXT_COLOR_RED;

            if (type == ChessPiece.PieceType.ROOK) {
                string += "R";
            } else if (type == ChessPiece.PieceType.KNIGHT) {
                string += "N";
            } else if (type == ChessPiece.PieceType.BISHOP) {
                string += "B";
            } else if (type == ChessPiece.PieceType.QUEEN) {
                string += "Q";
            } else if (type == ChessPiece.PieceType.KING) {
                string += "K";
            } else if (type == ChessPiece.PieceType.PAWN) {
                string += "P";
            } else {
                return "Error: piece type not found";
            }
        } else if (color == ChessGame.TeamColor.BLACK) {
            string += EscapeSequences.SET_TEXT_COLOR_BLUE;

            if (type == ChessPiece.PieceType.ROOK) {
                string += "R";
            } else if (type == ChessPiece.PieceType.KNIGHT) {
                string += "N";
            } else if (type == ChessPiece.PieceType.BISHOP) {
                string += "B";
            } else if (type == ChessPiece.PieceType.QUEEN) {
                string += "Q";
            } else if (type == ChessPiece.PieceType.KING) {
                string += "K";
            } else if (type == ChessPiece.PieceType.PAWN) {
                string += "P";
            } else {
                return "Error: piece type not found";
            }
        } else {
            return "Error: piece color not found";
        }

        string += EscapeSequences.RESET_TEXT_COLOR;

        return string;
    }

    public String help() {
        if (!isSignedIn()) { // Register or login menu
            return blueString("register") + " " +
                   "<" + magentaString("USERNAME") + "> " +
                   "<" + magentaString("PASSWORD") + "> " +
                   "<" + magentaString("EMAIL") + ">\n" +
                   blueString("login") + " " +
                   "<" + magentaString("USERNAME") + "> " +
                   "<" + magentaString("PASSWORD") + ">\n" +
                   blueString("quit") + "\n" +
                   blueString("help") + "\n";
        } else if (!isPlaying() && !isObserving()) { // Logged in menu
            return blueString("create") + " " +
                   "<" + magentaString("NAME") + ">\n" +
                   blueString("list") + "\n" +
                   blueString("join") + " " +
                   "<" + magentaString("ID") + "> " +
                   "<" + magentaString("COLOR") + ">\n" +
                   blueString("observe") + " " +
                   "<" + magentaString("ID") + ">\n" +
                   blueString("logout") + "\n" +
                   blueString("quit") + "\n" +
                   blueString("help") + "\n";
        } else if (isPlaying()) { // Playing game menu
            return blueString("move") + " " +
                   "<" + magentaString("START") + "> " +
                   "<" + magentaString("END") + "> " +
                   "<" + magentaString("PROMOTION TYPE") + ">\n" +
                   blueString("moves") + " " +
                   "<" + magentaString("PIECE") + ">\n" +
                   blueString("redraw") + "\n" +
                   blueString("resign") + "\n" +
                   blueString("leave") + "\n" +
                   blueString("help") + "\n";
        } else { // Observing game menu
            return blueString("moves") + "\n" +
                   blueString("redraw") + "\n" +
                   blueString("leave") + "\n" +
                   blueString("help") + "\n";
        }
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

    private String getAuthToken() {
        for (AuthData auth : auths) {
            if (auth.username().equals(username)) {
                return auth.authToken();
            }
        }
        return null;
    }

    private ChessGame getGame(int id) {
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

    private ChessPosition getChessPosition(String position) {
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

    private void assertSignedIn() throws ResponseException {
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

    public String greenHighlightString(String str) {
        return EscapeSequences.SET_BG_COLOR_GREEN + str + EscapeSequences.RESET_BG_COLOR;
    }

    public String yellowHighlightString(String str) {
        return EscapeSequences.SET_BG_COLOR_YELLOW + str + EscapeSequences.RESET_BG_COLOR;
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

    private boolean isPlaying() {
        return gameState == GameState.PLAYING;
    }

    private boolean isObserving() {
        return gameState == GameState.OBSERVING;
    }

    private boolean isSignedIn() {
        return state == LoginState.SIGNEDIN;
    }

}
