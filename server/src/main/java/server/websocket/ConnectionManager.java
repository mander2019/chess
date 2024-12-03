package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;
import websocket.commands.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private ConcurrentHashMap<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    public void add(String visitorName, Session session, String gameID) {
        sessions.computeIfAbsent(gameID, k -> new HashSet<>());
        sessions.get(gameID).add(session);
    }

    public void remove(Session session, String gameID) {
        if (sessions.containsKey(gameID)) {
            sessions.get(gameID).remove(session);
        }
    }

    public void broadcast(Session excludeSession, String msg, String gameID) throws IOException {
        if (sessions.containsKey(gameID)) {
            for (var s : sessions.get(gameID)) {
                if (s.isOpen()) {
                    if (!s.equals(excludeSession)) {
                        s.getRemote().sendString(msg);
                    }
                }
            }
        }

    }

    public void send(Session session, String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}
