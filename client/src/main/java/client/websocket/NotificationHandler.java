package client.websocket;

import websocket.messages.*;
import websocket.commands.*;

public interface NotificationHandler {
    void notify(ServerMessage serverMessage);
}
