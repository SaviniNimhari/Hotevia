package com.hotevia.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager for Hotevia.
 * Manages JDBC connection lifecycle and auto-schema initialization.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "hotevia_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static Connection connection;

    private DatabaseConnection() {
        // Private constructor for singleton
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                String host = System.getProperty("db.host", DEFAULT_HOST);
                String port = System.getProperty("db.port", DEFAULT_PORT);
                String dbName = System.getProperty("db.name", DEFAULT_DB);
                String user = System.getProperty("db.user", DEFAULT_USER);
                String pass = System.getProperty("db.password", DEFAULT_PASS);

                // First ensure database exists
                createDatabaseIfNotExists(host, port, dbName, user, pass);

                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName 
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
                
                connection = DriverManager.getConnection(url, user, pass);
                LOGGER.info("Connected to Hotevia MySQL Database successfully.");
                
                // Ensure schema and seed data are populated
                initializeSchema(connection);

            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found in classpath.", e);
                throw new SQLException("MySQL JDBC Driver missing. Check Maven dependencies.", e);
            }
        }
        return connection;
    }

    private static void createDatabaseIfNotExists(String host, String port, String dbName, String user, String pass) {
        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(serverUrl, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Notice during database existence check: " + e.getMessage());
        }
    }

    private static void initializeSchema(Connection conn) {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream("/db/schema.sql")) {
            if (is == null) {
                LOGGER.warning("schema.sql not found in resources/db folder.");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--") || line.isEmpty()) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    sql = sql.substring(0, sql.length() - 1); // remove semicolon
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql);
                    } catch (SQLException ex) {
                        // Table/data might already exist, log at debug/warning
                        LOGGER.log(Level.FINE, "SQL executed with note: " + ex.getMessage());
                    }
                    sb.setLength(0);
                }
            }
            LOGGER.info("Schema verification completed.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error initializing database schema: " + e.getMessage());
        }
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close database connection.", e);
            }
        }
    }
}
