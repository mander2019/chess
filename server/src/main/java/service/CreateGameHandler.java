package service;

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
