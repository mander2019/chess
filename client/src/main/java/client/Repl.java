package client;

import client.websocket.NotificationHandler;
import java.util.*;

public class Repl implements NotificationHandler {
    private final Client client;

    public Repl(String serverUrl) {
        client = new Client(serverUrl, this);
    }

    public void run() {
        System.out.println("♕ 240 Chess client\nType '" + client.blueString("help") + "' for a list of commands");
        System.out.print(client.help());

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

    private void printPrompt() {
        if (client.getState() == LoginState.SIGNEDIN) {
            System.out.print("[" + client.greenString("logged in") + " | " + client.greenString(client.getUsername()) + "]");
        } else {
            System.out.print("[" + client.redString("logged out") + "]");
        }

        System.out.print(" >>> ");
    }

}
