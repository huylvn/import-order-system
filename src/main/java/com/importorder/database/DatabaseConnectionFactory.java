package com.importorder.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Factory Method style factory that creates JDBC connections to SQLite.
 * DAO classes should obtain connections through this factory instead of hardcoding URLs.
 */
public class DatabaseConnectionFactory {

    private final String jdbcUrl;

    public DatabaseConnectionFactory() {
        this(DatabaseConfig.getJdbcUrl());
    }

    public DatabaseConnectionFactory(String jdbcUrl) {
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl must not be null");
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * Opens a new database connection. Caller is responsible for closing it.
     */
    public Connection createConnection() throws SQLException {
        ensureDatabaseDirectoryExists();
        return DriverManager.getConnection(jdbcUrl);
    }

    private void ensureDatabaseDirectoryExists() throws SQLException {
        if (!jdbcUrl.startsWith("jdbc:sqlite:")) {
            return;
        }
        String databasePathText = jdbcUrl.substring("jdbc:sqlite:".length());
        if (databasePathText.isBlank() || databasePathText.startsWith(":")) {
            return;
        }
        Path parent = Path.of(databasePathText).toAbsolutePath().getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (java.io.IOException e) {
            throw new SQLException("Cannot create SQLite database directory: " + parent, e);
        }
    }
}
