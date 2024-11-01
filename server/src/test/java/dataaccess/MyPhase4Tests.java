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