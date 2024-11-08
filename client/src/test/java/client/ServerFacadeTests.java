package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var serverUrl = "http://localhost:8080";
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade(serverUrl);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() {
        // Setup
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    // Successfully register new user

    // Fail to register new user (user already exists)

    // Successfully login

    // Fail to login (user does not exist)



}
