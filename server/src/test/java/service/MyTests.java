package service;

import chess.ChessGame;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.*;
import passoff.model.*;
import server.Server;
import service.request.RegisterRequest;
import service.response.RegisterResponse;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public class MyTests {
    private static Services services;
    private static MemoryUserDAO dao;

    @BeforeAll
    public static void setup() {
        dao = new MemoryUserDAO();
        services = new Services(dao);
    }

    // 1. Registering a user successfully

    @Test
    public void testRegisterUser() {
        try {
            RegisterRequest registerRequest = new RegisterRequest("c shane reese", "statistics", "sreese@byu.edu");

            RegisterResponse registerResponse = services.registerUser(registerRequest);

            System.out.println(registerResponse.username());

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
    // 4. Login with an invalid password

    // 5. Logout successfully
    // 6. Logout without being logged in

    // 7. List all games successfully
    // 8. List all games when there are no games

    // 9. Create a game successfully
    // 10. Create a game without complete information

    // 11. Join a game successfully
    // 12. Join a game that does not exist

    // 13. Clear all data successfully

}
