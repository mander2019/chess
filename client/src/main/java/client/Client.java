package client;

import client.websocket.NotificationHandler;
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
                case "login" -> login(params);
//                case "create" -> create(params);
//                case "list" -> list();
//                case "join" -> join(params);
//                case "observe" -> observe(params);
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
                server.register(username, password, email);
            } catch (ResponseException e) {
                if (e.StatusCode() == 403) {
                    return "Username already taken.\n";
                } else if (e.StatusCode() == 400) {
                    return "Bad input.\nExpected: <USERNAME> <PASSWORD> <EMAIL>\n";
                } else {
                    throw e;
                }
            }
            state = LoginState.SIGNEDIN;
            return "You have been successfully registered and signed in as " + username + ".\n";
        }
        throw new ResponseException(400, "Bad input.\nExpected: <USERNAME> <PASSWORD> <EMAIL>\n");
    }

    public String login(String... params) throws ResponseException {
        if (state == LoginState.SIGNEDIN) {
            throw new ResponseException(400, "You are already signed in.\n");
        }

        if (params.length == 2) {
            username = params[0];
            var password = params[1];
            server.login(username, password);
            state = LoginState.SIGNEDIN;
            return "You have been successfully signed in as " + username + ".\n";
        }
        throw new ResponseException(400, "Bad input.\nExpected: <USERNAME> <PASSWORD>\n");
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

    private void assertSignedIn() throws ResponseException {
        if (state == LoginState.SIGNEDOUT) {
            throw new ResponseException(400, "You must be signed in to perform this action.");
        }
    }
}
