package dataaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import model.AuthData;
import model.GameData;
import model.UserData;
import service.ClearResponse;
import service.RegisterRequest;
import service.RegisterResponse;

public class MemoryUserDAO implements DAO {
    private final Collection<UserData> users = new ArrayList<>();
    private final Collection<AuthData> auths = new ArrayList<>();
    private final Collection<GameData> games = new ArrayList<>();

    public RegisterResponse registerUser(RegisterRequest registerData) throws ServerErrorException {
        String username = registerData.username();
        String password = registerData.password();
        String email = registerData.email();
        String authToken = createAuthToken(username);

        System.out.println("Registering user: " + username);
        System.out.println("Registering password: " + password);
        System.out.println("Registering email: " + email);

        try {
            for (UserData user : users) {
                System.out.println("Checking user: " + user.username());
                if (user.username().equals(username)) {
                    throw new ServerErrorException(403, "Error: already taken");
                }
            }

            if (username == null || password == null || email == null ||
                username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                throw new ServerErrorException(400, "Error: bad request");
            }

            UserData newUser = new UserData(username, password, email);

            System.out.println("Registering user: " + username);

            users.add(newUser);

            AuthData newAuth = new AuthData(authToken, username);
            auths.add(newAuth);

            return new RegisterResponse(username, authToken);

        } catch (ServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerErrorException(500, "Internal server error");
        }
    }

    public ClearResponse clear() {
        auths.clear();
        games.clear();
        users.clear();

        return new ClearResponse();
    }

    public String createAuthToken(String username) {
        String authToken = UUID.randomUUID().toString();
        System.out.println("Creating auth token: " + authToken);
        return authToken;
    }

    public String getUser(String authToken) {
        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                return auth.username();
            }
        }
        return null;
    }

    public void deleteUser(String username) {
        users.removeIf(user -> user.username().equals(username));
        auths.removeIf(auth -> auth.username().equals(username));
    }

    public String getAuthData(String username) {
        for (AuthData auth : auths) {
            if (auth.username().equals(username)) {
                return auth.authToken();
            }
        }
        return null;
    }

    public void deleteAuthData(String username) {
        auths.removeIf(auth -> auth.username().equals(username));
    }

}
