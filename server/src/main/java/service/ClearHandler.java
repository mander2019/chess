package service;

import dataaccess.ServerErrorException;

public class ClearHandler extends Handler {
    public ClearHandler(Services services) {
        super(services);
    }

    public ClearResponse clear() throws ServerErrorException {
        return services.clear();
    }
}
