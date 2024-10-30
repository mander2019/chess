package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DAO {
    void addUser(UserData user) throws DataAccessException;
    Collection<UserData> getUsers();
    String getUserPassword(String username);
    boolean userExists(String username);
    String createAuthToken(String username);
    void addAuthData(AuthData auth) throws DataAccessException;
    void deleteAuthData(String username);
    Collection<AuthData> getAuths();
    boolean authExists(String authToken);
    void addGame(GameData game);
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> getGames();
    void addBlackPlayerToGame(GameData game, String username);
    void addWhitePlayerToGame(GameData game, String username);
    void removeGame(int gameID);
    void clear();

}
