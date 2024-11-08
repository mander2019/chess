package service.handler;

import model.request.JoinGameRequest;
import model.response.JoinGameResponse;
import service.Services;

public class JoinGameHandler extends Handler {
    private JoinGameRequest request;

    public JoinGameHandler(JoinGameRequest request, Services services) {
        super(services);
        this.request = request;
    }

    public JoinGameResponse joinGame() throws Exception {
        return services.joinGame(request);
    }
}
