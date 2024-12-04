package client;

import chess.ChessGame;
import client.websocket.NotificationHandler;
import client.*;
//import server.Server;
import ui.EscapeSequences;
import websocket.messages.ServerMessage;

import java.util.*;

public class Repl implements NotificationHandler {
    private final Client client;

    public Repl(String serverUrl) {
        client = new Client(serverUrl, this);
    }

    public void run() {
        System.out.println("♕ 240 Chess Client\nType '" + client.blueString("help") + "' for a list of commands\n");
        System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!"quit".equals(result)) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);

                if ("quit".equals(result)) {
                    System.out.print("Goodbye!");
                    break;
                }

                System.out.print("\n" + result + "\n");
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
//        System.out.println();
    }

    public void notify(ServerMessage notification) {
        ServerMessage.ServerMessageType type = notification.getServerMessageType();
        String msg = notification.getMessage();
        String error = notification.getErrorMessage();
        ChessGame game = notification.getChessGame();

        if (type == ServerMessage.ServerMessageType.LOAD_GAME && game != client.getCurrentGame()) {
            try {
                client.updateCurrentGame(game);
                System.out.print("\n" + client.redraw() + "\n");
            } catch (Exception e) {
                System.out.print("\nError: " + e.getMessage() + "\n");
            }

            if (msg != null) {
                System.out.print("\n" + msg + "\n");
            }
        } else if (type == ServerMessage.ServerMessageType.ERROR || type == ServerMessage.ServerMessageType.NOTIFICATION) {
            if (type == ServerMessage.ServerMessageType.ERROR) {
                System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_RED + error + EscapeSequences.RESET_TEXT_COLOR + "\n");
            } else {
                System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_BLUE + msg + EscapeSequences.RESET_TEXT_COLOR + "\n");
            }

            printPrompt();
        } else {
            System.out.print("\nError: Invalid server message");
        }
    }

    private void printPrompt() {
        String output;
        if (client.getState() == LoginState.SIGNEDIN && client.getGameState() == Client.GameState.PLAYING) {
            ChessGame game = client.getCurrentGame();
            ChessGame.TeamColor turn = game.getTeamTurn();
            ChessGame.TeamColor playerColor = client.getPlayerColor();

            if (turn == playerColor && game.getWinner() == null && !game.isDraw()) {
                output = "[" + client.greenString("playing") + " | "
                          + client.greenString(client.getUsername())
                          + " | " + client.greenString("your turn") + "]";
            } else {
                output = "[" + client.greenString("logged in") + " | " + client.greenString(client.getUsername()) + "]";
            }
        } else if (client.getState() == LoginState.SIGNEDIN) {
            output = "[" + client.greenString("logged in") + " | " + client.greenString(client.getUsername()) + "]";
        } else {
            output = ("[" + client.redString("logged out") + "]");
        }

        System.out.print(output + " >>> ");
    }

    public Client getClient() {
        return client;
    }

}
