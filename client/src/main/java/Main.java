import client.*;
import server.*;

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        int serverPort = Integer.parseInt(serverUrl.split(":")[2]);
        Server server = new Server();
        server.run(serverPort);

        new Repl(serverUrl).run();
    }
}
