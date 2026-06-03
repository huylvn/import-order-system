package com.importorder.dao.impl;

import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Shared JDBC helpers for SQLite DAO implementations.
 */
abstract class JdbcDaoSupport {

    protected final DatabaseConnectionFactory connectionFactory;

    protected JdbcDaoSupport(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory must not be null");
    }

    protected Connection openConnection() {
        try {
            return connectionFactory.createConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to open database connection", e);
        }
    }

    protected DatabaseException failed(String message, SQLException cause) {
        return new DatabaseException(message, cause);
    }

    protected static Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    protected static Integer getNullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    protected static long requireGeneratedId(ResultSet keys) throws SQLException {
        if (keys.next()) {
            return keys.getLong(1);
        }
        throw new SQLException("No generated key returned");
    }
}
