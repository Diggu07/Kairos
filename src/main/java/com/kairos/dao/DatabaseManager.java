package com.kairos.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton database manager for Kairos.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Creates the application data directory at {@code ~/kairos/} on first run.</li>
 *   <li>Opens and maintains a single SQLite {@link Connection}.</li>
 *   <li>Creates the {@code entries} table if it does not already exist.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 *   Connection conn = DatabaseManager.getInstance().getConnection();
 * }</pre>
 *
 * @author Kairos
 * @version 1.0.0
 */
public class DatabaseManager {

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    /** Sub-directory inside the user's home folder where all app data lives. */
    private static final String APP_DIR_NAME = "kairos";

    /** SQLite database filename. */
    private static final String DB_FILE_NAME = "kairos.db";

    /** DDL statement for the entries table. */
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS entries (" +
            "  id                INTEGER  PRIMARY KEY AUTOINCREMENT," +
            "  title             TEXT     NOT NULL," +
            "  content           TEXT," +
            "  type              TEXT     NOT NULL," +
            "  priority          TEXT     NOT NULL DEFAULT 'MEDIUM'," +
            "  tags              TEXT," +
            "  created_at        TEXT     NOT NULL," +
            "  updated_at        TEXT     NOT NULL," +
            "  reminder_time     TEXT," +
            "  is_completed      INTEGER  NOT NULL DEFAULT 0," +
            "  encrypted_content TEXT" +
            ");";

    // ─────────────────────────────────────────────────────────────────────────
    // Singleton State
    // ─────────────────────────────────────────────────────────────────────────

    /** The sole instance of this class. */
    private static DatabaseManager instance;

    /** Active JDBC connection to the SQLite file. */
    private Connection connection;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor (private)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Private constructor — enforces singleton usage.
     * Initialises the database directory, opens the connection, and creates
     * the schema on first run.
     */
    private DatabaseManager() {
        try {
            String dbPath = initAppDirectory();
            openConnection(dbPath);
            createTable();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Failed to initialise database: " + e.getMessage());
            throw new RuntimeException("Cannot initialise Kairos database.", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Singleton Access
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the singleton {@code DatabaseManager} instance, creating it on
     * the first call.
     *
     * @return the application-wide {@code DatabaseManager}
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the active {@link Connection} to the Kairos SQLite database.
     * If the connection has been closed (e.g. after a crash), it attempts
     * to re-open it automatically.
     *
     * @return a live {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String dbPath = initAppDirectory();
            openConnection(dbPath);
        }
        return connection;
    }

    /**
     * Closes the database connection gracefully.
     * Should be called when the application exits.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DatabaseManager] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Error closing connection: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ensures {@code ~/kairos/} exists and returns the absolute path to
     * {@code kairos.db} inside it.
     *
     * @return absolute path string for the SQLite file
     */
    private String initAppDirectory() {
        String home   = System.getProperty("user.home");
        File   appDir = new File(home, APP_DIR_NAME);

        if (!appDir.exists()) {
            boolean created = appDir.mkdirs();
            if (created) {
                System.out.println("[DatabaseManager] Created app directory: " + appDir.getAbsolutePath());
            } else {
                System.err.println("[DatabaseManager] Warning: could not create app directory.");
            }
        }

        return appDir.getAbsolutePath() + File.separator + DB_FILE_NAME;
    }

    /**
     * Opens the JDBC connection to the SQLite file at the given path.
     * Enables WAL journal mode for better concurrent read performance.
     *
     * @param dbPath absolute path to the {@code .db} file
     * @throws SQLException if the driver cannot open the file
     */
    private void openConnection(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        connection = DriverManager.getConnection(url);
        // WAL mode: readers don't block writers
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA foreign_keys=ON;");
        }
        System.out.println("[DatabaseManager] Connected to: " + dbPath);
    }

    /**
     * Executes the DDL to create the {@code entries} table if it does not
     * already exist. Safe to call on every application start.
     *
     * @throws SQLException if the DDL fails
     */
    private void createTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(CREATE_TABLE_SQL);
            System.out.println("[DatabaseManager] Table 'entries' ready.");
        }
    }
}
