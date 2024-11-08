package service.handler;

import records.request.JoinGameRequest;
import records.response.JoinGameResponse;
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
