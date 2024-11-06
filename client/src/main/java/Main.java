import chess.*;
import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import service.*;
import service.handler.*;
import service.request.*;
import service.response.*;
import spark.*;

import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Client. Type 'Help' to get started.");

        ServerFacade server = new ServerFacade();
//        int serverPort = Integer.parseInt(args[0]);
//        server.run(serverPort);

        server.run(8080);

        Request req;
        Response res;
        Scanner scanner = new Scanner(System.in);
        String input = "";

        while (!Objects.equals(input, "quit")) {
            input = scanner.nextLine();
            req = server.createRequest(input);


        }



    }
}