package service.handler;

import service.response.ClearResponse;
import service.Services;

public class ClearHandler extends Handler {
    public ClearHandler(Services services) {
        super(services);
    }

    public ClearResponse clear() throws Exception {
        return services.clear();
    }
}
