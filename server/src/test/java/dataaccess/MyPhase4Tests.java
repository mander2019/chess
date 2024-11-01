package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.Services;

import java.util.Collection;
import java.util.Objects;


public class MyPhase4Tests {
        private static Services services;
        private static DAO dao;

        private static UserData existingUser;
        private static String existingAuth;
        private static UserData newUser;

        private static GameData game1;


    @BeforeAll
        public static void setup() throws DataAccessException {
            dao = new MemoryUserDAO();
            services = new Services(dao);

            existingUser = new UserData("c shane reese", "statistics", "sreese@byu.edu");
            newUser = new UserData("joseph smith", "bom", "jsmith@churchofjesuschrist.org");
            game1 = new GameData(1, null, null, "game1", new ChessGame());
        }


        @BeforeEach // Clears database after each test
        public void clear() throws DataAccessException {
            dao.clear();

            existingAuth = getAuthToken(existingUser.username());
        }

        // 1. Adding a user successfully

        @Test
        @DisplayName("Adding a user successfully")
        public void testAddUser() {
            try {
                dao.addUser(newUser);

                Collection<UserData> users = dao.getUsers();

                boolean userFound = false;

                for (UserData u : users) {
                    if (u.equals(newUser)) {
                        userFound = true;
                    }
                }

                Assertions.assertTrue(userFound, "User not found in database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. Adding a user with a username that is already taken

        @Test
        @DisplayName("Adding a user with an unavailable username")
        public void testAddUserWithTakenUsername() {
            try {
                dao.addUser(existingUser);

                Collection<UserData> users = dao.getUsers();

                Assertions.assertFalse(users.size() > 1, "Too many users found in database");
                Assertions.assertFalse(users.isEmpty(), "No users found in database");

            } catch (Exception e) {
                Assertions.assertEquals("Error: already taken", e.getMessage(), "Error message should be 'Error: already taken'");

                e.printStackTrace();
            }
        }


        // 3. Adding auth data successfully

        @Test
        @DisplayName("Adding auth data successfully")
        public void testAddAuthData() {
            try {
                dao.addAuthData(new AuthData("auth token", existingUser.username()));

                Collection<AuthData> auths = dao.getAuths();

                boolean authFound = false;

                for (AuthData a : auths) {
                    if (a.authToken().equals("auth token")) {
                        authFound = true;
                    }
                }

                Assertions.assertTrue(authFound, "Auth data not found in database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // 5. Deleting auth data successfully

        @Test
        @DisplayName("Deleting auth data successfully")
        public void testDeleteAuthData() {
            try {
                dao.addAuthData(new AuthData("auth token", existingUser.username()));

                dao.deleteAuthData("auth token");

                Collection<AuthData> auths = dao.getAuths();

                boolean authFound = false;

                for (AuthData a : auths) {
                    if (a.authToken().equals("auth token")) {
                        authFound = true;
                    }
                }

                Assertions.assertFalse(authFound, "Auth data should not be found in database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 6. Deleting auth data that does not exist

        @Test
        @DisplayName("Deleting auth data that does not exist")
        public void testDeleteNonexistentAuthData() {
            try {
                dao.deleteAuthData("auth token");

                Collection<AuthData> auths = dao.getAuths();

                boolean authFound = false;

                for (AuthData a : auths) {
                    if (a.authToken().equals(existingAuth)) {
                        authFound = true;
                    }
                }

                Assertions.assertFalse(authFound, "Auth data should not be found in database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 7. Add a game successfully

        @Test
        @DisplayName("Adding a game successfully")
        public void testAddGame() {
            try {
                dao.addGame(game1);

                Collection<GameData> games = dao.getGames();

                boolean gameFound = false;

                for (GameData g : games) {
                    if (g.equals(game1)) {
                        gameFound = true;
                    }
                }

                Assertions.assertTrue(gameFound, "Game found in database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 8. Get a game successfully

        @Test
        @DisplayName("Getting a game successfully")
        public void testGetGame() {
            try {
                dao.addGame(game1);

                GameData game = dao.getGame(game1.gameID());

                Assertions.assertEquals(game1, game, "Game should be found in database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 9. Get a game that does not exist

        @Test
        @DisplayName("Getting a game that does not exist")
        public void testGetNonexistentGame() {
            try {
                dao.addGame(game1);

                dao.getGame(2);

            } catch (Exception e) {
                Assertions.assertEquals("Error: game not found", e.getMessage(), "Error message should be 'Error: game not found'");
            }
        }

        // 10. Get users successfully

        @Test
        @DisplayName("Getting users successfully")
        public void testGetUsers() {
            try {
                dao.addUser(newUser);
                dao.addUser(new UserData("brigham young", "education", "byoung@gmail.com"));

                Collection<UserData> users = dao.getUsers();

                Assertions.assertEquals(2, users.size(), "There are two users in the list");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 11. Get games successfully

        @Test
        @DisplayName("Getting games successfully")
        public void testGetGames() {
            try {
                dao.addGame(game1);
                dao.addGame(new GameData(2, null, null, "game2", new ChessGame()));

                Collection<GameData> games = dao.getGames();

                Assertions.assertEquals(2, games.size(), "There are two games in the list");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 12. Get auth data successfully

        @Test
        @DisplayName("Getting auth data successfully")
        public void testGetAuths() {
            try {
                dao.addAuthData(new AuthData("auth token", "c shane reese"));
                dao.addAuthData(new AuthData("auth token2", "brigham young"));

                Collection<AuthData> auths = dao.getAuths();

                Assertions.assertEquals(2, auths.size(), "There are two auths in the list");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 13. Find if auth data exists successfully

        @Test
        @DisplayName("Finding if auth data exists successfully")
        public void testAuthExists() {
            try {
                dao.addAuthData(new AuthData("auth token", "c shane reese"));
                dao.addAuthData(new AuthData("auth token2", "brigham young"));

                Assertions.assertTrue(dao.authExists("auth token"), "Auth token should exist");
                Assertions.assertFalse(dao.authExists("auth token3"), "Auth token should not exist");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 14. Find if user exists successfully

        @Test
        @DisplayName("Finding if user exists successfully")
        public void testUserExists() {
            try {
                dao.addUser(newUser);

                Assertions.assertTrue(dao.userExists(newUser.username()), "User should exist");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 15. Add black player to game successfully

        @Test
        @DisplayName("Adding black player to game successfully")
        public void testAddBlackPlayer() {
            try {
                dao.addGame(game1);
                dao.addUser(newUser);

                dao.removeGame(game1.gameID());
                dao.addBlackPlayerToGame(game1, newUser.username());

                GameData game = dao.getGame(game1.gameID());


                Assertions.assertEquals(newUser.username(), game.blackUsername(), "Black player should be added to game");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 16. Add white player to game successfully

        @Test
        @DisplayName("Adding white player to game successfully")
        public void testAddWhitePlayer() {
            try {
                dao.addGame(game1);
                dao.addUser(newUser);

                dao.removeGame(game1.gameID());
                dao.addWhitePlayerToGame(game1, newUser.username());

                GameData game = dao.getGame(game1.gameID());

                Assertions.assertEquals(newUser.username(), game.whiteUsername(), "White player should be added to game");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 17. Remove game successfully

        @Test
        @DisplayName("Removing game successfully")
        public void testRemoveGame() {
            try {
                dao.addGame(game1);

                dao.removeGame(game1.gameID());

                Collection<GameData> games = dao.getGames();

                Assertions.assertEquals(0, games.size(), "There are no games in the list");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 18. Get user password successfully

        @Test
        @DisplayName("Getting user password successfully")
        public void testGetUserPassword() {
            try {
                dao.addUser(existingUser);

                String password = dao.getUserPassword(existingUser.username());

                Assertions.assertEquals(existingUser.password(), password, "Password should be found in database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 19. Fail to get a user password that does not exist

        @Test
        @DisplayName("Failing to get a user password that does not exist")
        public void testGetNonexistentUserPassword() {
            try {
                dao.addUser(existingUser);

                dao.getUserPassword("brigham young");

            } catch (Exception e) {
                Assertions.assertEquals("Error: user not found", e.getMessage(), "Error message should be 'Error: user not found'");
            }
        }

        // 20. Fail to remove a game that does not exist

        @Test
        @DisplayName("Failing to remove a game that does not exist")
        public void testRemoveNonexistentGame() {
            try {
                dao.addGame(game1);

                dao.removeGame(2);

            } catch (Exception e) {
                Assertions.assertEquals("Error: game not found", e.getMessage(), "Error message should be 'Error: game not found'");
            }
        }

        @Test
        @DisplayName("Clear all data successfully")
        public void testClear() {
            try {
                dao.addAuthData(new AuthData("auth token", "c shane reese"));
                dao.addUser(new UserData("c shane reese", "statistics", "sreese@byu.edu"));
                dao.addGame(new GameData(1, null, null, "game1", new ChessGame()));

                dao.clear();

                Assertions.assertFalse(dao.authExists(getAuthToken(existingUser.username())), "Auth token should not exist");
                Assertions.assertFalse(dao.userExists(existingUser.username()), "User should not exist");
                Assertions.assertEquals(0, dao.getGames().size(), "There are no games in the list");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getAuthToken(String username) throws DataAccessException {
            Collection<AuthData> auths = dao.getAuths();

            for (AuthData auth : auths) {
                if (Objects.equals(auth.username(), existingUser.username())) {
                    return auth.authToken();
                }
            }

            return null;
        }

}