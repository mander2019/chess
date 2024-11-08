package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import client.websocket.NotificationHandler;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import server.ServerFacade;
import service.response.RegisterResponse;
import service.response.LoginResponse;
import service.response.CreateGameResponse;
import ui.EscapeSequences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private String username;
    private ServerFacade server;
    private String serverUrl;
    private NotificationHandler notificationHandler;
    private LoginState state = LoginState.SIGNEDOUT;
    private Collection<AuthData> auths = new ArrayList<>();
    private Collection<GameData> games = new ArrayList<>();
    private Map<Integer, GameData> gameDirectory = new HashMap<>();
    
    public Client(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
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
                addAuth(server.register(username, password, email));
            } catch (ResponseException e) {
                if (e.StatusCode() == 403) {
                    return "Username already taken\n";
                } else if (e.StatusCode() == 400) {
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
                addAuth(server.login(username, password));
            } catch (ResponseException e) {
                if (e.StatusCode() == 401) {
                    return "Invalid login credentials\n";
                } else if (e.StatusCode() == 400) {
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
            server.logout(auth);
        } catch (ResponseException e) {
            if (e.StatusCode() == 401) {
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
                CreateGameResponse game = server.createGame(getAuthToken(), name);
                int gameID = game.gameID();
                games.add(new GameData(gameID, null, null, name, new ChessGame()));
            } catch (ResponseException e) {
                if (e.StatusCode() == 400) {
                    return "Bad input\nExpected: <" + magentaString("NAME") + ">\n";
                } else if (e.StatusCode() == 401) {
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
            if (e.StatusCode() == 401) {
                return "Unauthorized\n";
            } else {
                throw e;
            }
        }
    }

    private String listHelper() {
        String output = "GAME NUMBER | NAME | WHITE PLAYER | BLACK PLAYER\n";
        int gameNumber = 1;

        for (GameData game : games) {

            String blackPlayer = game.blackUsername();
            String whitePlayer = game.whiteUsername();

            if (blackPlayer == null) {
                blackPlayer = "\t";
            }
            if (whitePlayer == null) {
                whitePlayer = "\t";
            }

            output += gameNumber + "\t" + game.gameName() + "\t";
            output += whitePlayer + "\t" + blackPlayer + "\n";
            gameNumber++;
        }
        return output + "\n";
    }

    private void updateGameDirectory() throws ResponseException {
        gameDirectory = new HashMap<>();
        games = server.listGames(getAuthToken()).games();
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
                server.joinGame(getAuthToken(), teamColor, Integer.toString(gameID));
            } catch (ResponseException e) {
                if (e.StatusCode() == 400) {
                    return "Bad input\nExpected: <" + magentaString("ID") + "> <" + magentaString("COLOR") + ">\n";
                } else if (e.StatusCode() == 401) {
                    return "Unauthorized\n";
                } else if (e.StatusCode() == 403) {
                    return "Game already taken\n";
                } else {
                    return e.getMessage();
                }
            }

            return "You have successfully joined game " + gameID + " as " + color + "\n";
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

private String printGame(ChessGame game, ChessGame.TeamColor color) {
    ChessBoard board = game.getBoard();
    ChessPiece[][] pieces = board.getSquares();
    String output = "";
    if (color == ChessGame.TeamColor.WHITE) {
        output += "   a  b  c  d  e  f  g  h\n";
        for (int i = 0; i < 8; i++) {
            output += (8 - i) + " ";
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 1) {
                    output += EscapeSequences.SET_BG_COLOR_BLACK + " " + chessPieceToString(pieces[i][j]);
                } else {
                    output += EscapeSequences.SET_BG_COLOR_WHITE + " " + chessPieceToString(pieces[i][j]);
                }
                output += " " + EscapeSequences.RESET_BG_COLOR;
            }
            output += " " + (8 - i) + "\n";
        }
        output += "   a  b  c  d  e  f  g  h\n";
    } else {
        output += "   h  g  f  e  d  c  b  a\n";
        for (int i = 7; i >= 0; i--) {
            output += (8 - i) + " ";
            for (int j = 7; j >= 0; j--) {
                if ((i + j) % 2 == 0) {
                    output += EscapeSequences.SET_BG_COLOR_BLACK + " " + chessPieceToString(pieces[i][j]);
                } else {
                    output += EscapeSequences.SET_BG_COLOR_WHITE + " " + chessPieceToString(pieces[i][j]);
                }
                output += " " + EscapeSequences.RESET_BG_COLOR;
            }
            output += " " + (8 - i) + "\n";
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
        } else {
            return blueString("create") + " " +
                "<" + magentaString("NAME") + ">\n" +
                blueString("list") + "\n" +
                blueString("join") + " " +
                "<" + magentaString("ID") + "> " +
                "<" + magentaString("COLOR") + ">\n" +
                blueString("observe") + " " +
                "<" + magentaString("ID") + ">\n" +
                blueString("quit") + "\n" +
                blueString("help") + "\n";
        }
    }

    public LoginState getState() {
        return state;
    }

    public String getUsername() {
        return username;
    }

    private void addAuth(Object response) throws ResponseException {
        if (response instanceof RegisterResponse registerResponse) {
            AuthData auth = new AuthData(registerResponse.authToken(), registerResponse.username());
            auths.add(auth);
        } else if (response instanceof LoginResponse loginResponse) {
            AuthData auth = new AuthData(loginResponse.authToken(), loginResponse.username());
            auths.add(auth);
        } else {
            throw new ResponseException(500, "Couldn't parse auth data.");
        }
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

    private void assertSignedIn() throws ResponseException {
        if (state == LoginState.SIGNEDOUT) {
            throw new ResponseException(400, "You must be signed in to perform this action.");
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
