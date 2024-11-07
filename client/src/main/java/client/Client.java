package client;

import client.websocket.NotificationHandler;
import dataaccess.ServerErrorException;
import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class Client {
    private String username;
    private ServerFacade server;
    private String serverUrl;
    private NotificationHandler notificationHandler;
    private LoginState state = LoginState.SIGNEDOUT;

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
//                case "login" -> login(params);
//                case "create" -> create(params);
//                case "list" -> list();
//                case "join" -> join(params);
//                case "observe" -> observe(params);
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
            server.register(username, password, email);
            state = LoginState.SIGNEDIN;
            return "You have been successfully registered and signed in.";
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String help() {
        if (state == LoginState.SIGNEDOUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL>
                    login <USERNAME> <PASSWORD>
                    quit
                    help
                    """;
        } else {
            return """
                    create <NAME>
                    list
                    join <ID> <COLOR>
                    observe <ID>
                    quit
                    help
                    """;
        }
    }

    private void assertSignedIn() throws ServerErrorException {
        if (state == LoginState.SIGNEDOUT) {
            throw new ServerErrorException(400, "You must be signed in to perform this action.");
        }
    }
}
