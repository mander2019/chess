package service;

import dataaccess.DAO;


public class Services {
    private final DAO dao;

    public Services(DAO dao) {
        this.dao = dao;
    }

    public RegisterResponse registerUser(RegisterRequest registerData) throws Exception {
        return dao.registerUser(registerData);
    }

    public ClearResponse clear() {
        return dao.clear();
    }

    private String getUser(String authToken) {
        return dao.getUser(authToken);
    }

    private String createAuthToken(String username) {
        return dao.createAuthToken(username);
    }

    private String getAuthData(String username) {
        return dao.getAuthData(username);
    }

    private void deleteAuthData(String username) {
        dao.deleteAuthData(username);
    }

}
