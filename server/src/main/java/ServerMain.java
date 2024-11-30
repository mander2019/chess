import server.Server;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.run(8080);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
