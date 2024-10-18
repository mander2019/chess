package dataaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import model.AuthData;
import model.GameData;
import model.UserData;
import service.*;

public class MemoryUserDAO implements DAO {
    private final Collection<UserData> users = new ArrayList<>();
    private final Collection<AuthData> auths = new ArrayList<>();
    private final Collection<GameData> games = new ArrayList<>();

    @Override
    public void addUser(UserData user) {
        users.add(user);
    }

    public String getUser(String authToken) throws DataAccessException {
        try {
            for (AuthData auth : auths) {
                if (auth.authToken().equals(authToken)) {
                    return auth.username();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error: user not found");
        }

        return null;
    }

    public String getUserPassword(String username) {
        for (UserData user : users) {
            if (user.username().equals(username)) {
                return user.password();
            }
        }
        return null;
    }

    public boolean userExists(String username) {
        for (UserData user : users) {
            if (user.username().equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteUser(String username) {
        for (UserData user : users) {
            if (user.username().equals(username)) {
                users.remove(user);
                break;
            }
        }

        deleteAuthData(username);
    }

    @Override
    public void addAuthData(AuthData auth) {
        auths.add(auth);
    }

    public String createAuthToken(String username) {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getAuthData(String username) throws DataAccessException {
        try {
            for (AuthData auth : auths) {
                if (auth.username().equals(username)) {
                    return auth.authToken();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error: user not found");
        }

        return null;
    }

    @Override
    public void deleteAuthData(String username) {
        for (AuthData auth : auths) {
            if (auth.username().equals(username)) {
                auths.remove(auth);
                break;
            }
        }
    }

    @Override
    public void clear() {
        auths.clear();
        games.clear();
        users.clear();
    }

}
