package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, Connection> connections = new ConcurrentHashMap<>();

    public void add(int gameID, Session session) {
        var connection = new Connection(gameID, session);
        connections.put(gameID, connection);
    }

    public void remove(int gameID) {
        connections.remove(gameID);
    }

    public void broadcast(Session excludeSession, String msg, int gameID) throws IOException {
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.session.equals(excludeSession)) {
                    c.send(msg);
                }
            }
        }
    }
}
