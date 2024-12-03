package websocket.messages;

import chess.ChessGame;
import com.google.gson.Gson;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String message;
    String errorMessage;
//    String game;
    ChessGame game;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String newMessage) {
        this.serverMessageType = type;

        this.message = newMessage;

        if (type == ServerMessageType.ERROR) {
            this.errorMessage = newMessage;
            this.message = null;
        }
        this.game = null;
    }

    public ServerMessage(ChessGame game) {
//        this.game = new Gson().toJson(game);

        this.game = game;
        this.serverMessageType = ServerMessageType.LOAD_GAME;
        this.message = null;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String getMessage() {
        return this.message;
    }

    public ChessGame getChessGame() {
        return this.game;
//        return new Gson().fromJson(game, ChessGame.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
