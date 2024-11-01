package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DAO {
    void addUser(UserData user) throws DataAccessException;
    Collection<UserData> getUsers() throws DataAccessException;
    String getUserPassword(String username) throws DataAccessException;
    boolean userExists(String username) throws DataAccessException;
    void addAuthData(AuthData auth) throws DataAccessException;
    void deleteAuthData(String username) throws DataAccessException;
    Collection<AuthData> getAuths() throws DataAccessException;
    boolean authExists(String authToken) throws DataAccessException;
    void addGame(GameData game);
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> getGames();
    void addBlackPlayerToGame(GameData game, String username);
    void addWhitePlayerToGame(GameData game, String username);
    void removeGame(int gameID);
    void clear() throws DataAccessException;

}
