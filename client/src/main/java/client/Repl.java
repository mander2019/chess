package client;

import chess.ChessGame;
import client.websocket.NotificationHandler;
import client.*;
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
        System.out.println();
    }

    public void notify(ServerMessage notification) {
        ServerMessage.ServerMessageType type = notification.getServerMessageType();
        String msg = notification.getMessage();
        ChessGame game = notification.getChessGame();

        if (type == ServerMessage.ServerMessageType.LOAD_GAME) {
            try {
                System.out.println("Updating game...");
                client.updateCurrentGame(game);
                Thread.sleep(1000);
                client.redraw();
            } catch (Exception e) {
                System.out.print("\nError: " + e.getMessage());
            }
        } else if (type == ServerMessage.ServerMessageType.ERROR || type == ServerMessage.ServerMessageType.NOTIFICATION) {
            System.out.print("\n\n" + msg + "\n\n");
            printPrompt();
        } else {
            System.out.print("\nError: Invalid server message");
        }
    }

    private void printPrompt() {
        if (client.getState() == LoginState.SIGNEDIN) {
            System.out.print("[" + client.greenString("logged in") + " | " + client.greenString(client.getUsername()) + "]");
        } else {
            System.out.print("[" + client.redString("logged out") + "]");
        }

        System.out.print(" >>> ");
    }

    public Client getClient() {
        return client;
    }

}
