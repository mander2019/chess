package dataaccess;

import java.util.ArrayList;
import java.util.Collection;

import model.AuthData;
import model.GameData;
import model.UserData;

public class MemoryUserDAO implements DAO {
    private final Collection<UserData> users = new ArrayList<>();
    private final Collection<AuthData> auths = new ArrayList<>();
    private final Collection<GameData> games = new ArrayList<>();

    public void addUser(UserData user) {
        users.add(user);
    }

    public Collection<UserData> getUsers() {
        return users;
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

    public void addAuthData(AuthData auth) {
        auths.add(auth);
    }

    public void deleteAuthData(String authToken) {
        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                auths.remove(auth);
                break;
            }
        }
    }

    public Collection<AuthData> getAuths() {
        return auths;
    }

    public boolean authExists(String authToken) {
        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                return true;
            }
        }
        return false;
    }

    public void addGame(GameData game) {
        games.add(game);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("Error: game not found");
    }

    public Collection<GameData> getGames() {
        return games;
    }

    public void addBlackPlayerToGame(GameData game, String username) {
        game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        games.add(game);
    }

    public void addWhitePlayerToGame(GameData game, String username) {
        game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        games.add(game);
    }

    public void removeGame(int gameID) {
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                games.remove(game);
                break;
            }
        }
    }

    public void clear() {
        auths.clear();
        games.clear();
        users.clear();
    }

}
