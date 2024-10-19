package service.handler;

import service.request.LoginRequest;
import service.response.LoginResponse;
import service.Services;

public class LoginHandler extends Handler {
    private LoginRequest request;

    public LoginHandler(LoginRequest request, Services services) {
        super(services);
        this.request = request;
    }

    public LoginResponse login() throws Exception {
        return services.loginUser(request);
    }
}
