package client;

import client.websocket.NotificationHandler;
import exception.ResponseException;
import model.AuthData;
import server.ServerFacade;
import service.response.LoginResponse;
import service.response.RegisterResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Client {
    private String username;
    private ServerFacade server;
    private String serverUrl;
    private NotificationHandler notificationHandler;
    private LoginState state = LoginState.SIGNEDOUT;
    private Collection<AuthData> auths = new ArrayList<>();

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
                addAuth(server.register(username, password, email));
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
            return "You have been successfully registered and signed in as '" + username + "'.\n";
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
            try {
                addAuth(server.login(username, password));
            } catch (ResponseException e) {
                if (e.StatusCode() == 401) {
                    return "Invalid login credentials.\n";
                } else if (e.StatusCode() == 400) {
                    return "Bad input.\nExpected: <USERNAME> <PASSWORD>\n";
                } else {
                    throw e;
                }
            }
            state = LoginState.SIGNEDIN;
            return "You have been successfully signed in as '" + username + "'.\n";
        }
        throw new ResponseException(400, "Bad input.\nExpected: <USERNAME> <PASSWORD>\n");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        String auth;
        try {
            auth = getAuthToken();
            server.logout(auth);
        } catch (ResponseException e) {
            if (e.StatusCode() == 401) {
                return "Logout error.\n";
            } else {
                throw e;
            }
        }
        removeAuth(auth);
        state = LoginState.SIGNEDOUT;
        return "You have been successfully signed out.\n";
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

    private void assertSignedIn() throws ResponseException {
        if (state == LoginState.SIGNEDOUT) {
            throw new ResponseException(400, "You must be signed in to perform this action.");
        }
    }
}
