package service;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import passoff.model.*;
import server.Server;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public class MyTests {
    private static Server server;

    @BeforeAll
    public static void setup() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started my personal test HTTP server on " + port);

        // Finish initializing the server
    }

    // Write test cases for the following:

    // 1. Registering a user successfully
    // 2. Registering a user with a username that is already taken

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
