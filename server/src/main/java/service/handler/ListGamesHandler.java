package service.handler;

import records.request.ListGamesRequest;
import records.response.ListGamesResponse;
import service.Services;

public class ListGamesHandler extends Handler {
    private ListGamesRequest request;

    public ListGamesHandler(ListGamesRequest request, Services services) {
        super(services);
        this.request = request;
    }

    public ListGamesResponse listGames() throws Exception {
        return services.listGames(request);
    }
}
