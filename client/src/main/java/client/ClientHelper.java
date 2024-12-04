package client;

import chess.*;
import exception.ResponseException;
import ui.EscapeSequences;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class ClientHelper extends Client {
    private Client client;

    public ClientHelper(Client client) {
        super();
        this.client = client;
    }

    public String chessPieceToString(ChessPiece piece) {
        if  (piece == null) {
            return " ";
        }

        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();
        String string = "";

        String pieceSymbol = switch (type) {
            case ROOK -> "R";
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case QUEEN -> "Q";
            case KING -> "K";
            case PAWN -> "P";
            default -> "Error: piece type not found";
        };

        if (color == ChessGame.TeamColor.WHITE) {
            string += EscapeSequences.SET_TEXT_COLOR_RED + pieceSymbol;
        } else if (color == ChessGame.TeamColor.BLACK) {
            string += EscapeSequences.SET_TEXT_COLOR_BLUE + pieceSymbol;
        } else {
            return "Error: piece color not found";
        }

        string += EscapeSequences.RESET_TEXT_COLOR;

        return string;
    }

    public String help() {
        if (!client.isSignedIn()) { // Register or login menu
            return blueString("register") + " " +
                    "<" + magentaString("USERNAME") + "> " +
                    "<" + magentaString("PASSWORD") + "> " +
                    "<" + magentaString("EMAIL") + ">\n" +
                    blueString("login") + " " +
                    "<" + magentaString("USERNAME") + "> " +
                    "<" + magentaString("PASSWORD") + ">\n" +
                    blueString("quit") + "\n" +
                    blueString("help") + "\n";
        } else if (!client.isPlaying() && !client.isObserving()) { // Logged in menu
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
        } else if (client.isPlaying()) { // Playing game menu
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
            for (int x = 0; x < 8; x++) {
                output += (x + 1) + " ";
                for (int y = 7; y >= 0; y--) {
                    if ((x + y) % 2 == 0) {
                        output += EscapeSequences.SET_BG_COLOR_BLACK + " " + chessPieceToString(pieces[x][y]);
                    } else {
                        output += EscapeSequences.SET_BG_COLOR_WHITE + " " + chessPieceToString(pieces[x][y]);
                    }
                    output += " " + EscapeSequences.RESET_BG_COLOR;
                }
                output += " " + (x + 1) + "\n";
            }
            output += "   h  g  f  e  d  c  b  a\n";
        }

        if (game.getWinner() != null) {
//            output += "\ngame over: " + game.getWinner().toString().toLowerCase() + " wins\n";
        } else if (game.isDraw()) {
//            output += "\ngame over: draw\n\n";
        } else {
            output += "\n——" + game.getTeamTurn().toString().toLowerCase() + "'s turn——\n";
        }

        return output;
    }

    public String moves(String... params) {
        if (!client.isPlaying() && !client.isObserving()) {
            return "You are not currently in a game\n";
        } else if (params.length != 1) {
            return "Bad input\nExpected: <" + magentaString("PIECE") + ">\n";
        }

        ChessPosition position = getChessPosition(params[0]);
        if (position == null) {
            return "Bad input\nExpected: <" + magentaString("PIECE") + ">\n";
        }

        ChessGame.TeamColor observeColor = client.playerColor;
        if (client.isObserving()) {
            observeColor = ChessGame.TeamColor.WHITE;
        }

        ChessGame game = client.getCurrentGame();
        ChessBoard board = game.getBoard();
        ChessPiece[][] pieces = board.getSquares();
        ChessPiece piece = board.getPiece(position);

        if (piece == null) {
            return "No piece at " + position + "\n";
        }

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
//                    end = new ChessPosition(i + 1, j + 1);
                    move = getMoveFromList(new ChessPosition(i + 1, j + 1), moves);
                    if (move != null) {
                        output += EscapeSequences.SET_BG_COLOR_GREEN + " " + chessPieceToString(pieces[i][i]);
                    } else if (new ChessPosition(i + 1, j + 1).equals(start)) {
                        output += EscapeSequences.SET_BG_COLOR_YELLOW + " " + chessPieceToString(pieces[i][j]);
                    } else if ((i + j) % 2 == 0) {
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
                    end = new ChessPosition(i + 1, j + 1);
                    move = getMoveFromList(end, moves);
                    if (new ChessPosition(i + 1, j + 1).equals(start)) {
                        output += EscapeSequences.SET_BG_COLOR_YELLOW + " " + chessPieceToString(pieces[i][j]);
                    } else if (move != null) {
                        output += EscapeSequences.SET_BG_COLOR_GREEN + " " + chessPieceToString(pieces[i][j]);
                    } else if ((i + j) % 2 == 0) {
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

    public String joinGame(String... params) throws ResponseException {
        client.assertSignedIn();
        client.updateGameDirectory();

        if (params.length == 2) {
            int directoryIndex;
            int gameID;
            try { // Convert input to gameID
                directoryIndex = Integer.parseInt(params[0]);

                if (directoryIndex > client.games.size() || directoryIndex <= 0) {
                    return "Game not found";
                }

                gameID = client.gameDirectory.get(directoryIndex).gameID();
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
                client.serverFacade.joinGame(client.getAuthToken(), teamColor, Integer.toString(gameID));
                client.gameState = GameState.PLAYING;
                client.playerColor = teamColor;
                client.updateCurrentGame(client.getGame(gameID));
                client.currentGameID = gameID;
                client.serverFacade.enterGame(client.getAuthToken(), gameID);
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

            ChessGame game;

            try {
                game = client.getGame(gameID);

                if (game == null) {
                    return "Game not found";
                } else if (client.isGameOver(game)) {
                    client.leave();
                    return "this game is already over\nplease select another game\n";
                }
            } catch (Exception e) {
                return "Game doesn't exist";
            }

            return "you have successfully joined game " + gameID + " as " + color;
        } else {
            return "Bad input—incorrect number of arguments\nExpected: <" + magentaString("ID") + "> <" + magentaString("COLOR") + ">";
        }
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

            if (client.state == LoginState.SIGNEDOUT) {
                return switch (function) {
                    case "register" -> client.register(params);
                    case "login" -> client.login(params);
                    case "quit" -> "quit";
                    default -> client.help();
                };
            } else if (client.state == LoginState.SIGNEDIN && client.gameState == GameState.NONE) {
                return switch (function) {
                    case "create" -> client.createGame(params);
                    case "list" -> client.list();
                    case "join" -> client.joinGame(params);
                    case "observe" -> client.observe(params);
                    case "logout" -> client.logout();
                    case "quit" -> "quit";
                    default -> client.help();
                };
            } else if (client.state == LoginState.SIGNEDIN && client.gameState == GameState.PLAYING) {
                return switch (function) {
                    case "move" -> client.makeMove(params);
                    case "moves" -> client.moves(params);
                    case "redraw" -> client.redraw();
                    case "resign" -> client.resign();
                    case "leave" -> client.leave();
                    default -> client.help();
                };
            } else if (client.state == LoginState.SIGNEDIN && client.gameState == GameState.OBSERVING) {
                return switch (function) {
                    case "moves" -> client.moves(params);
                    case "redraw" -> client.redraw();
                    case "leave" -> client.leave();
                    default -> client.help();
                };
            } else {
                return client.help();
            }
        } catch (Throwable e) {
            return e.getMessage();
        }
    }


}
