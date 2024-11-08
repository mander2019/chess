package service.handler;

import service.request.CreateGameRequest;
import service.response.CreateGameResponse;
import service.Services;

public class CreateGameHandler extends Handler {
    CreateGameRequest request;

    public CreateGameHandler(CreateGameRequest request, Services services) {
        super(services);
        this.request = request;
    }

    public CreateGameResponse createGame() throws Exception {
        return services.createGame(request);
    }
}
