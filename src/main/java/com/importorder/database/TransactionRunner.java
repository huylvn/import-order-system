package com.importorder.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Runs a unit of work inside a single JDBC transaction (commit / rollback).
 */
public final class TransactionRunner {

    @FunctionalInterface
    public interface TransactionWork {
        void execute(Connection connection) throws SQLException;
    }

    private TransactionRunner() {
    }

    public static void runInTransaction(DatabaseConnectionFactory connectionFactory, TransactionWork work) {
        try (Connection connection = connectionFactory.createConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                work.execute(connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseException("Transaction rolled back", e);
            } catch (RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to run transaction", e);
        }
    }
}
