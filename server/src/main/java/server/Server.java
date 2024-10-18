package server;

import com.google.gson.Gson;
import dataaccess.ErrorMessage;
import dataaccess.MemoryUserDAO;
import dataaccess.ServerErrorException;
import service.*;
import spark.*;

public class Server {
    private Services service = new Services(new MemoryUserDAO());

    public int run(int port) {
        Spark.port(port);

        Spark.staticFiles.location("web");


        // Register your endpoints and handle exceptions here.

        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::getGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearData);

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object registerUser(Request req, Response res) throws Exception {
        try{
            RegisterRequest registerRequest = new Gson().fromJson(req.body(), RegisterRequest.class);
            RegisterHandler registerHandler = new RegisterHandler(registerRequest, service);
            RegisterResponse registerResponse = registerHandler.register();

            return new Gson().toJson(registerResponse);
        } catch (ServerErrorException e) {
            return errorMessageHelper(res, e);
        }
    }

    private Object loginUser(Request req, Response res) {
        System.out.println(req.body());

        return new Gson().toJson(req.body());
    }

    private Object logoutUser(Request req, Response res) {
        System.out.println(req.body());

        return new Gson().toJson(req.body());
    }

    private Object getGames(Request req, Response res) {
        System.out.println(req.body());

        return new Gson().toJson(req.body());
    }

    private Object createGame(Request req, Response res) {
        System.out.println(req.body());

        return new Gson().toJson(req.body());
    }

    private Object joinGame(Request req, Response res) {
        System.out.println(req.body());

        return new Gson().toJson(req.body());
    }

    private Object clearData(Request req, Response res) {
//        System.out.println("Clearing data...");

        ClearHandler clearHandler = new ClearHandler(service);
        ClearResponse clearResponse = clearHandler.clear();

        return new Gson().toJson(clearResponse);
    }

    private Object errorMessageHelper(Response res, ServerErrorException e) {
        res.status(e.StatusCode());
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage());
        return new Gson().toJson(errorMessage);
    }

}
