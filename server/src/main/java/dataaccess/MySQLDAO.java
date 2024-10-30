package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MySQLDAO implements DAO {

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

    public void addUser(UserData user) throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();

        var statement = "INSERT INTO users VALUES (" + user.username() + ", " + user.password() + ", " + user.email() + ");";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();


            System.out.println("User added");

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public Collection<UserData> getUsers() {
        Collection<UserData> users = new ArrayList<>();


        


        return null;
    }

    public String getUserPassword(String username) {

        return null;
    }

    public boolean userExists(String username) {
        Collection<UserData> users = getUsers();

        if (users != null) {
            for (UserData user : users) {
                if (user.username().equals(username)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addAuthData(AuthData auth) throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();

        var statement = "INSERT INTO auths VALUES (" + auth.authToken() + ", " + auth.username() + ");";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();


            System.out.println("Auth added");

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
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
