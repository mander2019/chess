package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.request.*;
import service.response.*;

import java.util.Collection;

public class MyTests {
    private static Services services;
    private static MemoryUserDAO dao;

    @BeforeAll
    public static void setup() throws DataAccessException {
        dao = new MemoryUserDAO();
        services = new Services(dao);
    }


    @BeforeEach // Clears server after each test
    public void clear() {
        dao.clear();
    }

    // 1. Registering a user successfully

    @Test
    public void testRegisterUser() {
        try {
            RegisterRequest registerRequest = new RegisterRequest("c shane reese", "statistics", "sreese@byu.edu");

            RegisterResponse registerResponse = services.registerUser(registerRequest);

            Assertions.assertEquals("c shane reese", registerResponse.username(), "Username should be 'c shane reese'");
            Assertions.assertNotNull(registerResponse.authToken(), "Auth token should not be null");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Registering a user with a username that is already taken

    @Test
    public void testRegisterUserWithTakenUsername() {
        try {
            RegisterRequest registerRequest = new RegisterRequest("c shane reese", "statistics", "sreese@byu.edu");

            RegisterResponse registerResponse = services.registerUser(registerRequest);

            // This should throw an exception
            RegisterResponse registerResponse2 = services.registerUser(registerRequest);

        } catch (Exception e) {
            Assertions.assertEquals("Error: already taken", e.getMessage(), "Error message should be 'Error: already taken'");

            e.printStackTrace();
        }
    }


    // 3. Login successfully

    @Test
    public void testLoginUser() {
        try {
            RegisterRequest registerRequest = new RegisterRequest("c shane reese", "statistics", "sreese@byu.edu");
            RegisterResponse registerResponse = services.registerUser(registerRequest);


            LoginRequest loginRequest = new LoginRequest("c shane reese", "statistics");
            LoginResponse loginResponse = services.loginUser(loginRequest);

            Assertions.assertEquals("c shane reese", loginResponse.username(), "Username should be 'c shane reese'");
            Assertions.assertNotNull(loginResponse.authToken(), "Auth token should not be null");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4. Login with an invalid password

    @Test
    public void testLoginUserWithInvalidPassword() {
        try {
            RegisterRequest registerRequest = new RegisterRequest("c shane reese", "statistics", "sreese@byu.edu");

            RegisterResponse registerResponse = services.registerUser(registerRequest);

            LoginRequest loginRequest = new LoginRequest("c shane reese", "wrong password");

            // This should throw an exception
            LoginResponse loginResponse = services.loginUser(loginRequest);

        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Error message should be 'Error: unauthorized'");

            e.printStackTrace();
        }
    }

    // 5. Logout successfully

    @Test
    public void testLogoutUser() {
        try {
            RegisterRequest registerRequest = new RegisterRequest("c shane reese", "statistics", "sreese@byu.edu");
            RegisterResponse registerResponse = services.registerUser(registerRequest);

            LoginRequest loginRequest = new LoginRequest("c shane reese", "statistics");
            LoginResponse loginResponse = services.loginUser(loginRequest);

            LogoutRequest logoutRequest = new LogoutRequest(loginResponse.authToken());
            LogoutResponse logoutResponse = services.logout(logoutRequest);

            Assertions.assertTrue(dao.authExists(loginResponse.authToken()),"Auth token should be null");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 6. Logout without being logged in

    @Test
    public void testLogoutUserWithInvalidPassword() {
        try {
            RegisterRequest registerRequest = new RegisterRequest("c shane reese", "statistics",
                                                                    "sreese@byu.edu");
            RegisterResponse registerResponse = services.registerUser(registerRequest);

            LoginRequest loginRequest = new LoginRequest("c shane reese", "statistics");
            LoginResponse loginResponse = services.loginUser(loginRequest);

            LogoutRequest logoutRequest = new LogoutRequest("wrong auth token");

            // This should throw an exception
            LogoutResponse logoutResponse = services.logout(logoutRequest);


        } catch (Exception e) {
            Assertions.assertEquals("Error: user does not exist", e.getMessage(),
                                    "Error message should be 'Error: user does not exist'");
            Assertions.assertFalse(dao.authExists("wrong auth token"), "Auth token should not exist");

            e.printStackTrace();
        }
    }

    // 7. List all games successfully

    @Test
    public void testListGames() {
        try {
            dao.addGame(new GameData(1, null, null, "game1", new ChessGame()));
            dao.addGame(new GameData(2, null, null, "game2", new ChessGame()));
            dao.addGame(new GameData(3, null, null, "game3", new ChessGame()));
            dao.addGame(new GameData(4, null, null, "game4", new ChessGame()));

            dao.addAuthData(new AuthData("auth token", "c shane reese"));

            ListGamesRequest listGamesRequest = new ListGamesRequest("auth token");

            Collection<GameData> games = services.listGames(listGamesRequest).games();

            Assertions.assertEquals(4, games.size(), "There should be 4 games");
            Assertions.assertTrue(games.contains(new GameData(1, null, null, "game1", new ChessGame())), "Game 1 should be in the list");
            Assertions.assertTrue(games.contains(new GameData(2, null, null, "game2", new ChessGame())), "Game 2 should be in the list");
            Assertions.assertTrue(games.contains(new GameData(3, null, null, "game3", new ChessGame())), "Game 3 should be in the list");
            Assertions.assertTrue(games.contains(new GameData(4, null, null, "game4", new ChessGame())), "Game 4 should be in the list");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 8. List all games when there are no games

    @Test
    public void testListGamesWhileEmpty() {
        try {
            dao.addAuthData(new AuthData("auth token", "c shane reese"));

            ListGamesRequest listGamesRequest = new ListGamesRequest("auth token");

            Collection<GameData> games = services.listGames(listGamesRequest).games();

            Assertions.assertEquals(0, games.size(), "There should be 4 games");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 9. Create a game successfully

    @Test
    public void testCreateGame() {
        try {
            dao.addAuthData(new AuthData("auth token", "c shane reese"));

            CreateGameRequest createGameRequest = new CreateGameRequest("auth token", "game1");
            CreateGameResponse createGameResponse = services.createGame(createGameRequest);

            Assertions.assertEquals(1, createGameResponse.gameID(), "Game ID should be 1");
            Assertions.assertEquals(dao.getGames().size(), 1, "There is one game in the list");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 10. Create a game with bad auth token

    @Test
    public void testCreateGameWithBadAuth() {
        try {
            dao.addAuthData(new AuthData("auth token", "c shane reese"));

            CreateGameRequest createGameRequest = new CreateGameRequest("auth token2", "game1");
            CreateGameResponse createGameResponse = services.createGame(createGameRequest);
        } catch (Exception e) {
            Assertions.assertEquals("Error: unauthorized", e.getMessage(), "Error message should be 'Error: unauthorized'");
            Assertions.assertEquals(dao.getGames().size(), 0, "There are no games in the list");

            e.printStackTrace();
        }
    }

    // 11. Join a game successfully

    @Test
    public void testJoinGame() {
        try {
            dao.addAuthData(new AuthData("auth token", "c shane reese"));

            CreateGameRequest createGameRequest = new CreateGameRequest("auth token", "game1");
            CreateGameResponse createGameResponse = services.createGame(createGameRequest);

            JoinGameRequest joinGameRequest = new JoinGameRequest("auth token", ChessGame.TeamColor.WHITE, 1);
            JoinGameResponse joinGameResponse = services.joinGame(joinGameRequest);

            GameData game = dao.getGame(1);

            Assertions.assertEquals(game.whiteUsername(), "c shane reese", "White player should be 'c shane reese'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 12. Join a game that does not exist

    @Test
    public void testJoinGameThatDoesNotExist() {
        try {
            dao.addAuthData(new AuthData("auth token", "c shane reese"));

            JoinGameRequest joinGameRequest = new JoinGameRequest("auth token", ChessGame.TeamColor.WHITE, 1);
            JoinGameResponse joinGameResponse = services.joinGame(joinGameRequest);

            GameData game = dao.getGame(1);
        } catch (Exception e) {
            Assertions.assertEquals("Error: game not found", e.getMessage(), "Error message should be 'Error: game not found'");
            Assertions.assertEquals(dao.getGames().size(), 0, "There are no games in the list");

            e.printStackTrace();
        }
    }

    // 13. Clear all data successfully

    @Test
    public void testClear() {
        try {
            dao.addAuthData(new AuthData("auth token", "c shane reese"));
            dao.addUser(new UserData("c shane reese", "statistics", "sreese@byu.edu"));
            dao.addGame(new GameData(1, null, null, "game1", new ChessGame()));

            ClearResponse clearResponse = services.clear();

            Assertions.assertFalse(dao.authExists("auth token"), "Auth token should not exist");
            Assertions.assertFalse(dao.userExists("c shane reese"), "User should not exist");
            Assertions.assertEquals(0, dao.getGames().size(), "There are no games in the list");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
