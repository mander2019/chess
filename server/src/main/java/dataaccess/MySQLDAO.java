package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MySQLDAO implements DAO {
    private final Collection<UserData> users = new ArrayList<>();
    private final Collection<AuthData> auths = new ArrayList<>();
    private final Collection<GameData> games = new ArrayList<>();

    public MySQLDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();


//        try (var conn = DatabaseManager.getConnection()) {
//            for (var statement : createStatements) {
//                try (var preparedStatement = conn.prepareStatement(statement)) {
//                    preparedStatement.executeUpdate();
//                }
//            }
//        } catch (SQLException ex) {
//            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
//        }
    }

    public void addUser(UserData user) {

    }

    public Collection<UserData> getUsers() {
        return null;
    }

    public String getUserPassword(String username) {

        return null;
    }

    public boolean userExists(String username) {
        return false;
    }

    public void addAuthData(AuthData auth) {

    }

    public String createAuthToken(String username) {
        return null;
    }

    public void deleteAuthData(String username) {

    }

    public Collection<AuthData> getAuths() {
        return null;
    }

    public boolean authExists(String authToken) {
        return false;
    }

    public void addGame(GameData game) {

    }

    public GameData getGame(int gameID) throws DataAccessException {
       return null;
    }

    public Collection<GameData> getGames() {
        return null;
    }

    public void addBlackPlayerToGame(GameData game, String username) {

    }

    public void addWhitePlayerToGame(GameData game, String username) {

    }

    public void removeGame(int gameID) {

    }

    public void clear() {

    }


}
