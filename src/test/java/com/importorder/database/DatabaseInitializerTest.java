package com.importorder.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInitializerTest {

    @TempDir
    Path tempDir;

    @Test
    void initialize_createsAllTables() throws SQLException {
        String dbPath = tempDir.resolve("test_import_order.db").toAbsolutePath().toString();
        String jdbcUrl = "jdbc:sqlite:" + dbPath;

        DatabaseConnectionFactory factory = new DatabaseConnectionFactory(jdbcUrl);
        new DatabaseInitializer(factory).initialize();

        try (Connection connection = factory.createConnection();
             Statement statement = connection.createStatement()) {

            assertTableExists(statement, "merchandise");
            assertTableExists(statement, "import_site");
            assertTableExists(statement, "site_merchandise");
            assertTableExists(statement, "inventory");
            assertTableExists(statement, "import_request");
            assertTableExists(statement, "import_request_item");
            assertTableExists(statement, "allocation_result");
            assertTableExists(statement, "site_order");
            assertTableExists(statement, "site_order_item");
            assertTableExists(statement, "received_goods");
        }
    }

    private static void assertTableExists(Statement statement, String tableName) throws SQLException {
        try (ResultSet rs = statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
            assertTrue(rs.next(), "Table should exist: " + tableName);
        }
    }
}
