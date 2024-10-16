package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.


        Spark.post("/user", (req, res) -> { // Register



            System.out.println("Registration endpoint");
            return "Registration endpoint";
        });

        Spark.post("/session", (req, res) -> { // Login
            System.out.println(req);
            System.out.println("Login endpoint");
            return "Login endpoint";
        });

        Spark.delete("/session", (req, res) -> { // Logout
            System.out.println(req);
            System.out.println("Logout endpoint");
            return "Logout endpoint";
        });

        Spark.get("/game", (req, res) -> { // Get games
            System.out.println(req);
            System.out.println("Get games endpoint");
            return "Get games endpoint";
        });

        Spark.post("/game", (req, res) -> { // Create a game
            System.out.println(req);
            System.out.println("Create game endpoint");
            return "Create game endpoint";
        });

        Spark.put("/game", (req, res) -> { // Join a game
            System.out.println(req);
            System.out.println("Join game endpoint");
            return "Join game endpoint";
        });

        Spark.delete("/db", (req, res) -> { // Clear data
            System.out.println(req);
            System.out.println("Clear data endpoint");
            return "Clear data endpoint";
        });


        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
