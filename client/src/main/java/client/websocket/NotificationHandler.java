package client.websocket;

import client.Client;
import websocket.messages.*;
import websocket.commands.*;

public interface NotificationHandler {
    void notify(ServerMessage serverMessage);
    Client getClient();
}
