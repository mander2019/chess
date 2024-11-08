package service.handler;


import model.request.RegisterRequest;
import model.response.RegisterResponse;
import service.Services;

public class RegisterHandler extends Handler {
    private RegisterRequest request;

    public RegisterHandler(RegisterRequest request, Services services) {
        super(services);
        this.request = request;
    }

    public RegisterResponse register() throws Exception {
        return services.registerUser(request);
    }

}
