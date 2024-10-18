package dataaccess;

import model.AuthData;
import model.UserData;
import service.*;

public interface DAO {
    void addUser(UserData user);
    String getUser(String authToken) throws DataAccessException;
    String getUserPassword(String username);
    boolean userExists(String username);
    void deleteUser(String userName);
    String createAuthToken(String username);
    void addAuthData(AuthData auth);
    String getAuthData(String username) throws DataAccessException;
    void deleteAuthData(String username);
    void clear();

}
