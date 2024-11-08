package client;

import chess.ChessGame;
import exception.ResponseException;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
//import server.Server;
import serverfacade.ServerFacade;

import java.util.Collection;


public class ServerFacadeTests {
//    private static Server server;
    static ServerFacade serverFacade;

    private static UserData existingUser;
    private static String existingAuth;
    private static UserData newUser;
    private static GameData game1;

    @BeforeAll
    public static void init() {
//        server = new Server();
//        var port = server.run(0);

        var port = 8080;
        String serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade(serverUrl);

        existingUser = new UserData("c shane reese", "statistics", "sreese@byu.edu");
        newUser = new UserData("joseph smith", "bom", "jsmith@churchofjesuschrist.org");
        game1 = new GameData(1, null, null, "game1", new ChessGame());
    }

    @AfterAll
    static void stopServer() {
        serverFacade.stop();
    }

    @BeforeEach
    void setup() throws ResponseException {
        serverFacade.clearData();
        existingAuth = serverFacade.register(existingUser.username(), existingUser.password(), existingUser.email());
        serverFacade.createGame(existingAuth, game1.gameName());
    }

    @Test
    @DisplayName("Successfully register new user")
    void registerUser() throws ResponseException {
        var response = serverFacade.register(newUser.username(), newUser.password(), newUser.email());
        Assertions.assertNotNull(response);
    }

    @Test
    @DisplayName("Fail to register new user (user already exists)")
    void registerExistingUser() {
        try {
            serverFacade.register(existingUser.username(), existingUser.password(), existingUser.email());
        } catch (ResponseException e) {
            Assertions.assertEquals(403, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Successfully login")
    void loginUser() throws ResponseException {
        var response = serverFacade.login(existingUser.username(), existingUser.password());
        Assertions.assertNotNull(response);
    }

    @Test
    @DisplayName("Fail to login (user does not exist)")
    void loginNonExistentUser() {
        try {
            serverFacade.login(newUser.username(), newUser.password());
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Successfully logout")
    void logoutUser() throws ResponseException {
        serverFacade.logout(existingAuth);

        try {
            serverFacade.listGames(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Fail to logout (user not logged in)")
    void logoutNotLoggedIn() {
        try {
            serverFacade.logout(existingAuth);
            serverFacade.logout(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Successfully create game")
    void createGame() throws ResponseException {
        var response = serverFacade.createGame(existingAuth, "game2");
        Assertions.assertEquals(2, response);
    }

    @Test
    @DisplayName("Fail to create game (user not logged in)")
    void createExistingGame() {
        try {
            serverFacade.logout(existingAuth);
            serverFacade.createGame(existingAuth, game1.gameName());
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Successfully list games")
    void listGames() throws ResponseException {
        var response = serverFacade.listGames(existingAuth);
        Assertions.assertEquals(1, response.size());

        serverFacade.createGame(existingAuth, "game2");
        response = serverFacade.listGames(existingAuth);
        Assertions.assertEquals(2, response.size());
    }

    @Test
    @DisplayName("Fail to list games (user not logged in)")
    void listGamesNotLoggedIn() {
        try {
            serverFacade.logout(existingAuth);
            serverFacade.listGames(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Successfully join game")
    void joinGame() throws ResponseException {
        serverFacade.joinGame(existingAuth, ChessGame.TeamColor.BLACK, "1");

        Collection<GameData> games = serverFacade.listGames(existingAuth);

        for (GameData game : games) {
            if (game.gameID() == 1) {
                Assertions.assertEquals(game.blackUsername(), existingUser.username());
            }
        }
    }

    @Test
    @DisplayName("Fail to join game (user not logged in)")
    void joinGameNotLoggedIn() {
        try {
            serverFacade.logout(existingAuth);
            serverFacade.joinGame(existingAuth, ChessGame.TeamColor.BLACK, "1");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Fail to join game (already taken)")
    void joinGameAlreadyTaken() {
        try {
            serverFacade.joinGame(existingAuth, ChessGame.TeamColor.BLACK, "1");
            serverFacade.joinGame(existingAuth, ChessGame.TeamColor.BLACK, "1");
        } catch (ResponseException e) {
            Assertions.assertEquals(403, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Clear data")
    void clearData() {
        try {
            serverFacade.clearData();
            serverFacade.listGames(existingAuth);
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.StatusCode());
        }
    }

}
