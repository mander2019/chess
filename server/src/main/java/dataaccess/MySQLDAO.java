package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLDAO implements DAO {

    public MySQLDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
    }

    public void addUser(UserData user) throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();

        String username = user.username();
        String password = user.password();
        String email = user.email();

        var json = new Gson().toJson(user);

        var statement = "INSERT INTO users (username, password, email, json) ";
        statement += "VALUES ('" + username + "', '" + password + "', '";
        statement += email + "', '" + json + "');";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public Collection<UserData> getUsers() throws DataAccessException {
        Collection<UserData> users = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String username = rs.getString("username");
                        String password = rs.getString("password");
                        String email = rs.getString("email");
                        users.add(new UserData(username, password, email));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        return users;
    }

    public String getUserPassword(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password FROM users";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getString("username").equals(username)) {
                            return rs.getString("password");
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    public boolean userExists(String username) throws DataAccessException {
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

        String username = auth.username();
        String authtoken = auth.authToken();

        var json = new Gson().toJson(auth);

        var statement = "INSERT INTO auths (authToken, username, json) ";
        statement += "VALUES ('" + authtoken + "', '" + username;
        statement += "', '" + json + "');";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteAuthData(String authToken) throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();

        var statement = "DELETE FROM auths WHERE authtoken='" + authToken + "';";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Collection<AuthData> getAuths() throws DataAccessException {
        Collection<AuthData> auths = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authtoken, username FROM auths";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String authToken = rs.getString("authtoken");
                        String username = rs.getString("username");
                        auths.add(new AuthData(authToken, username));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        return auths;
    }

    public boolean authExists(String authToken) throws DataAccessException {
        Collection<AuthData> auths = getAuths();

        for (AuthData auth : auths) {
            if (auth.authToken().equals(authToken)) {
                return true;
            }
        }

        return false;
    }

    public void addGame(GameData game) throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();

        int gameID = game.gameID();
        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();
        String gameName = game.gameName();
        gameName = new Gson().toJson(gameName);
        var json = new Gson().toJson(game.game());

        var statement = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, json) ";
        statement += "VALUES ('" + gameID + "', '" + whiteUsername + "', '";
        statement += blackUsername + "', '" + gameName + "', '" + json + "');";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
       Collection<GameData> games = getGames();

        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }

        throw new DataAccessException("Error: game not found");
    }

    public Collection<GameData> getGames() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, json FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int gameID = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        gameName = new Gson().fromJson(gameName, String.class);
                        var json = rs.getString("json");
                        ChessGame gameState = new Gson().fromJson(json, ChessGame.class);

                        if (Objects.equals(whiteUsername, "null")) {
                            whiteUsername = null;
                        }
                        if (Objects.equals(blackUsername, "null")) {
                            blackUsername = null;
                        }

                        games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, gameState));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }

        return games;
    }

    public void addBlackPlayerToGame(GameData game, String username) throws DataAccessException {
        game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        addGame(game);
    }

    public void addWhitePlayerToGame(GameData game, String username) throws DataAccessException {
        game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        addGame(game);
    }

    public void removeGame(int gameID) throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();

        var statement = "DELETE FROM games WHERE gameID='" + gameID + "';";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clear() throws DataAccessException {
        clearHelper("users");
        clearHelper("auths");
        clearHelper("games");
    }

    private void clearHelper(String table) throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();

        String statement = "TRUNCATE " + table + ";";

        try (var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof UserData p) ps.setString(i + 1, p.toString());
                    else if (param instanceof AuthData p) ps.setString(i + 1, p.toString());
                    else if (param instanceof GameData p) ps.setString(i + 1, p.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

}
