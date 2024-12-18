package service.handler;

import model.request.LogoutRequest;
import model.response.LogoutResponse;
import service.Services;

public class LogoutHandler extends Handler {
    private LogoutRequest request;

    public LogoutHandler(LogoutRequest request, Services services) {
        super(services);
        this.request = request;
    }

    public LogoutResponse logout() throws Exception {
        return services.logout(request);
    }
}
