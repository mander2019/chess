package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String visitorName, Session session) {
        var connection = new Connection(visitorName, session);
        connections.put(visitorName, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
    }

    public void broadcast(Session excludeSession, String msg) throws IOException {
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.session.equals(excludeSession)) {
                    c.send(msg);
                }
            }
        }
    }

    public void send(Session session, String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}
