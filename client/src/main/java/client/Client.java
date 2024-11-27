package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import client.websocket.NotificationHandler;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import ui.EscapeSequences;
import server.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private String username;
    private ServerFacade serverFacade;
    private String serverUrl;
    private NotificationHandler notificationHandler;
    private LoginState state = LoginState.SIGNEDOUT;
    private boolean playingGame = false;
    private boolean observingGame = false;
    private ChessGame currentGame;
    private int currentGameID;
    private ChessGame.TeamColor currentColor;
    private Collection<AuthData> auths = new ArrayList<>();
    private Collection<GameData> games = new ArrayList<>();
    private Map<Integer, GameData> gameDirectory = new HashMap<>();
    
    public Client(String serverUrl, NotificationHandler notificationHandler) {
        serverFacade = new ServerFacade(serverUrl, notificationHandler);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
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

            return switch (function) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> list();
                case "join" -> joinGame(params);
                case "observe" -> observe(params);
                case "quit" -> "quit";
                case "redraw" -> redraw();
//                case "leave" -> leave();
//                case "move" -> move(params);
//                case "resign" -> resign();
//                case "moves" -> moves();
                default -> help();
            };
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
                playingGame = true;
                currentColor = teamColor;
                updateCurrentGame(getGame(gameID), gameID);
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
            }

            String output;

            try {
                ChessGame game = getGame(gameID);

                if (game == null) {
                    return "Game not found\n";
                }

                output = printGame(game, ChessGame.TeamColor.WHITE);
                output += "\n";
                output += printGame(game, ChessGame.TeamColor.BLACK);
            } catch (Exception e) {
                return "Game doesn't exist\n";
            }

            return output + "\nYou have successfully joined game " + gameID + " as " + color + "\n";
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
            try {
                gameID = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new ResponseException(400, "Bad input\nExpected: <" + magentaString("ID") + ">\n");
            }

            ChessGame game = getGame(gameID);
            if (game == null) {
                throw new ResponseException(400, "Game not found\n");
            }

            output = printGame(game, ChessGame.TeamColor.WHITE);
            output += "\n";
            output += printGame(game, ChessGame.TeamColor.BLACK);

            return output;
        } else {
            throw new ResponseException(400, "Bad input\nExpected: <" + magentaString("ID") + ">\n");
        }
    }

    public String redraw() throws ResponseException {
        assertSignedIn();
        ChessGame game = getCurrentGame();

        if (observingGame) {
            return printGame(game, ChessGame.TeamColor.WHITE) + "\n";
        } else if (playingGame) {
            if (currentColor == ChessGame.TeamColor.WHITE) {
                return printGame(game, ChessGame.TeamColor.WHITE) + "\n";
            } else {
                return printGame(game, ChessGame.TeamColor.BLACK) + "\n";
            }
        } else {
            return "You are not currently in a game\n";
        }
    }

    private String printGame(ChessGame game, ChessGame.TeamColor color) {
        ChessBoard board = game.getBoard();
        ChessPiece[][] pieces = board.getSquares();
        String output = "";
        if (color == ChessGame.TeamColor.WHITE) {
            output += "   a  b  c  d  e  f  g  h\n";
            for (int i = 7; i >= 0; i--) {
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
            output += "   a  b  c  d  e  f  g  h\n";
        } else {
            output += "   h  g  f  e  d  c  b  a\n";
            for (int i = 0; i < 8; i++) {
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
            output += "   h  g  f  e  d  c  b  a\n";
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
                string += "r";
            } else if (type == ChessPiece.PieceType.KNIGHT) {
                string += "n";
            } else if (type == ChessPiece.PieceType.BISHOP) {
                string += "b";
            } else if (type == ChessPiece.PieceType.QUEEN) {
                string += "q";
            } else if (type == ChessPiece.PieceType.KING) {
                string += "k";
            } else if (type == ChessPiece.PieceType.PAWN) {
                string += "p";
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
        if (state == LoginState.SIGNEDOUT) {
            return blueString("register") + " " +
               "<" + magentaString("USERNAME") + "> " +
               "<" + magentaString("PASSWORD") + "> " +
               "<" + magentaString("EMAIL") + ">\n" +
               blueString("login") + " " +
               "<" + magentaString("USERNAME") + "> " +
               "<" + magentaString("PASSWORD") + ">\n" +
               blueString("quit") + "\n" +
               blueString("help") + "\n";
        } else if (state == LoginState.SIGNEDIN && !playingGame && !observingGame) {
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
        } else if (state == LoginState.SIGNEDIN && playingGame) {
            return blueString("redraw") + "\n" +
                    blueString("leave") + "\n" +
                    blueString("move") + " " +
                    "<" + magentaString("ROW") + "> " +
                    "<" + magentaString("COLUMN") + ">\n" +
                    blueString("resign") + "\n" +
                    blueString("moves") + "\n" +
                    blueString("help") + "\n";
        } else {
            return blueString("redraw") + "\n" +
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

    private void updateCurrentGame(ChessGame game, int gameID) {
        currentGame = game;
        currentGameID = gameID;
    }

    private ChessGame getCurrentGame() {
        return currentGame;
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

}
