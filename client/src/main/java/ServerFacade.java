import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import service.*;
import service.handler.*;
import service.request.*;
import service.response.*;
import spark.*;


public class ServerFacade extends server.Server {

    public ServerFacade() {
        super();
    }

    public int run(int port) {
        return super.run(port);
    }

    public void stop() {
        super.stop();
    }

    public Request createRequest(String json) {
        return new Gson().fromJson(json, Request.class);
    }

}
