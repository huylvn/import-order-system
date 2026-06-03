package com.importorder.database;

import java.nio.file.Path;

/**
 * SQLite connection settings for the embedded database.
 */
public final class DatabaseConfig {

    private static final String APP_DATA_DIR = ".import-order-system";
    public static final String DB_FILE_NAME = "import_order.db";

    private DatabaseConfig() {
    }

    public static String getJdbcUrl() {
        Path databasePath = Path.of(System.getProperty("user.home"), APP_DATA_DIR, DB_FILE_NAME);
        return "jdbc:sqlite:" + databasePath;
    }
}
