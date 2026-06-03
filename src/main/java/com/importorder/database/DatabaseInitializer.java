package com.importorder.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * Creates application tables on first run (idempotent).
 */
public class DatabaseInitializer {

    private static final List<String> CREATE_TABLE_STATEMENTS = List.of(
            """
            CREATE TABLE IF NOT EXISTS merchandise (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                unit TEXT NOT NULL,
                description TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS import_site (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                site_code TEXT UNIQUE NOT NULL,
                site_name TEXT NOT NULL,
                ship_delivery_days INTEGER NOT NULL,
                air_delivery_days INTEGER NOT NULL,
                other_information TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS site_merchandise (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                site_id INTEGER NOT NULL,
                merchandise_id INTEGER NOT NULL,
                UNIQUE(site_id, merchandise_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS inventory (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                site_id INTEGER NOT NULL,
                merchandise_id INTEGER NOT NULL,
                in_stock_quantity INTEGER NOT NULL,
                unit TEXT NOT NULL,
                last_updated_at TEXT,
                UNIQUE(site_id, merchandise_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS import_request (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                request_code TEXT UNIQUE NOT NULL,
                request_date TEXT NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS import_request_item (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                import_request_id INTEGER NOT NULL,
                merchandise_id INTEGER NOT NULL,
                quantity_ordered INTEGER NOT NULL,
                unit TEXT NOT NULL,
                desired_delivery_date TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS allocation_result (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                import_request_id INTEGER NOT NULL,
                request_item_id INTEGER NOT NULL,
                site_id INTEGER,
                merchandise_id INTEGER NOT NULL,
                allocated_quantity INTEGER,
                unit TEXT,
                delivery_means TEXT,
                status TEXT NOT NULL,
                error_message TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS site_order (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                import_request_id INTEGER NOT NULL,
                site_id INTEGER NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT,
                sent_at TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS site_order_item (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                site_order_id INTEGER NOT NULL,
                merchandise_id INTEGER NOT NULL,
                quantity_ordered INTEGER NOT NULL,
                unit TEXT NOT NULL,
                delivery_means TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS received_goods (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                site_order_item_id INTEGER NOT NULL,
                actual_received_quantity INTEGER NOT NULL,
                received_at TEXT,
                status TEXT NOT NULL,
                note TEXT
            )
            """
    );

    private final DatabaseConnectionFactory connectionFactory;

    public DatabaseInitializer(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory must not be null");
    }

    /**
     * Ensures all schema tables exist.
     */
    public void initialize() {
        try (Connection connection = connectionFactory.createConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : CREATE_TABLE_STATEMENTS) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database schema", e);
        }
    }
}
