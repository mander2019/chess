package service;

import dataaccess.ServerErrorException;

public class ClearHandler extends Handler {
    public ClearHandler(Services services) {
        super(services);
    }

    public ClearResponse clear() throws Exception {
        return services.clear();
    }
}
