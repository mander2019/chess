import server.Server;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);
    }
}
