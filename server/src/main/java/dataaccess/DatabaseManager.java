package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    public static final String DATABASE_NAME; // Changed to public to allow clearing database based on config file
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    static void createDatabase() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);

            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME + ";";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            statement = "USE " + DATABASE_NAME + ";";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            // Create the users table
            statement = "CREATE TABLE IF NOT EXISTS users (\n";
            statement += "\tusername VARCHAR(255) NOT NULL,\n";
            statement += "\tpassword VARCHAR(255) NOT NULL,\n";
            statement += "\temail VARCHAR(255) NOT NULL,\n";
            statement += "\tjson TEXT DEFAULT NULL,\n";
            statement += "\tPRIMARY KEY (username)\n);";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            // Create the auths table
            statement = "CREATE TABLE IF NOT EXISTS auths (\n";
            statement += "\tauthtoken VARCHAR(255) NOT NULL,\n";
            statement += "\tusername VARCHAR(255) NOT NULL,\n";
            statement += "\tjson TEXT DEFAULT NULL,\n";
            statement += "\tPRIMARY KEY (authtoken)\n);";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            // Create the games table
            statement = "CREATE TABLE IF NOT EXISTS games (\n";
//            statement += "\tgameID INT NOT NULL,\n";
            statement += "\tgameID INT NOT NULL,\n";
            statement += "\twhiteUsername VARCHAR(255) DEFAULT NULL,\n";
            statement += "\tblackUsername VARCHAR(255) DEFAULT NULL,\n";
            statement += "\tgameName VARCHAR(255) NOT NULL,\n";
//            statement += "\tgameState TEXT NOT NULL,\n";
            statement += "\tjson TEXT DEFAULT NULL,\n";
            statement += "\tPRIMARY KEY (gameID)\n);";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}