package service;

import chess.ChessGame;
import dataaccess.DAO;
import dataaccess.DataAccessException;
import dataaccess.ServerErrorException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;


public class Services {
    private final DAO dao;

    public Services(DAO dao) {
        this.dao = dao;
    }

    public RegisterResponse registerUser(RegisterRequest registerData) throws ServerErrorException {
        String username = registerData.username();
        String password = registerData.password();
        String email = registerData.email();
        String authToken = createAuthToken(username);

        try {
            if (userExists(username)) { // Check if user already exists
                throw new ServerErrorException(403, "Error: already taken");
            }

            // Check for null or empty fields
            if (username == null || password == null || email == null ||
                username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                throw new ServerErrorException(400, "Error: bad request");
            }

            UserData newUser = new UserData(username, password, email);
            addUser(newUser);

            AuthData newAuth = new AuthData(authToken, username);
            addAuthData(newAuth);

            return new RegisterResponse(username, authToken);
        } catch (ServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerErrorException(500, "Internal server error");
        }
    }

    public LoginResponse loginUser(LoginRequest loginData) throws ServerErrorException {
        String username = loginData.username();
        String password = loginData.password();

        try {
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                throw new ServerErrorException(400, "Error: bad request");
            } else if (!userExists(username)) {
                throw new ServerErrorException(401, "Error: user not found");
            } else if (!password.equals(getUserPassword(username))) {
                throw new ServerErrorException(401, "Error: unauthorized");
            } else if (password.equals(getUserPassword(username))) {
                String authToken = createAuthToken(username);
                AuthData newAuth = new AuthData(authToken, username);
                addAuthData(newAuth);
                return new LoginResponse(username, authToken);
            }
        } catch (ServerErrorException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServerErrorException(401, "Error: not found");
        } catch (Exception e) {
            throw new ServerErrorException(500, "Internal server error");
        }

        return null;
    }

    public LogoutResponse logout(LogoutRequest logoutData) throws ServerErrorException {
        try {
            String authToken = logoutData.authToken();

            System.out.println("Logging out user: " + authToken);

            String username = getUserFromAuthToken(authToken);

            System.out.println("Logging out user: " + username);

            if (username == null) {
                throw new ServerErrorException(401, "Error: user does not exist");
            }

            deleteAuthData(username);

            return new LogoutResponse();
        } catch (ServerErrorException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServerErrorException(500, "Internal server error");
        }
    }

    public ListGamesResponse listGames(ListGamesRequest listGameData) throws ServerErrorException {
        String authToken = listGameData.authToken();

        try {
            if (invalidAuthToken(authToken)) {
                throw new ServerErrorException(401, "Error: unauthorized");
            }

            Collection<GameData> games = getGames();

            return new ListGamesResponse(games);
        } catch (ServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerErrorException(500, "Internal server error");
        }
    }

    public CreateGameResponse createGame(CreateGameRequest createGameData) throws ServerErrorException{
        String authToken = createGameData.authToken();

        try {
            if (invalidAuthToken(authToken)) {
                throw new ServerErrorException(401, "Error: unauthorized");
            }

            Collection<GameData> games = getGames();
            int gameID = games.size() + 1;

            GameData newGame = new GameData(gameID, null, null, createGameData.gameName(), new ChessGame());

            addGame(newGame);

            return new CreateGameResponse(gameID);
        } catch (ServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerErrorException(500, "Internal server error");
        }
    }

    public JoinGameResponse joinGame (JoinGameRequest joinGameData) throws ServerErrorException {
        String authToken = joinGameData.authToken();
        int gameID = joinGameData.gameID();
        ChessGame.TeamColor color = joinGameData.teamColor();

        if (color == null) {
            throw new ServerErrorException(400, "Error: bad request");
        } else if (invalidAuthToken(authToken)) {
            throw new ServerErrorException(401, "Error: unauthorized");
        }

        try {
            GameData game = getGame(gameID);
            JoinGameResponse joinGameResponse = null;

            if ((game.whiteUsername() == null && color == ChessGame.TeamColor.WHITE) ||
                (game.blackUsername() == null && color == ChessGame.TeamColor.BLACK)) {
                addPlayerToGame(game, getUserFromAuthToken(authToken), color);
            } else {
                throw new ServerErrorException(403, "Error: already taken");
            }

            return joinGameResponse;
        } catch (DataAccessException e) {
            throw new ServerErrorException(500, e.getMessage());
        }
    }

    public ClearResponse clear() {
        clearData();
        return new ClearResponse();
    }

    private void addUser(UserData user) {
        dao.addUser(user);
    }

    private String getUserFromAuthToken(String authToken) throws DataAccessException {
        return dao.getUser(authToken);
    }

    private String getUserPassword(String username) throws DataAccessException {
        return dao.getUserPassword(username);
    }

    private boolean userExists(String username) {
        return dao.userExists(username);
    }

    private void addAuthData(AuthData auth) {
        dao.addAuthData(auth);
    }

    private String createAuthToken(String username) {
        return dao.createAuthToken(username);
    }

    private String getAuthData(String username) throws DataAccessException {
        return dao.getAuthData(username);
    }

    private void deleteAuthData(String username) {
        dao.deleteAuthData(username);
    }

    private boolean invalidAuthToken(String authToken) {
        return !dao.authExists(authToken);
    }

    private void addGame(GameData game) {
        dao.addGame(game);
    }

    private GameData getGame(int gameID) throws DataAccessException {
        return dao.getGame(gameID);
    }

    private Collection<GameData> getGames() {
        return dao.getGames();
    }

    private void addPlayerToGame(GameData game, String username, ChessGame.TeamColor color) {
        dao.removeGame(game.gameID());

        if (color == ChessGame.TeamColor.WHITE) {
            dao.addWhitePlayerToGame(game, username);
        } else {
            dao.addBlackPlayerToGame(game, username);
        }
    }

    public void clearData() {
        dao.clear();
    }
}
