package com.importorder.dao.impl;

import com.importorder.dao.ImportRequestDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.ImportRequest;
import com.importorder.model.enums.ImportRequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteImportRequestDAO extends JdbcDaoSupport implements ImportRequestDAO {

    public SQLiteImportRequestDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<ImportRequest> findAll() {
        String sql = "SELECT id, request_code, request_date, status, created_at FROM import_request ORDER BY id DESC";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ImportRequest> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to find all import requests", e);
        }
    }

    @Override
    public Optional<ImportRequest> findById(long id) {
        String sql = "SELECT id, request_code, request_date, status, created_at FROM import_request WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw failed("Failed to find import request by id: " + id, e);
        }
    }

    @Override
    public ImportRequest save(ImportRequest importRequest) {
        String sql = "INSERT INTO import_request (request_code, request_date, status, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, importRequest.getRequestCode());
            ps.setString(2, importRequest.getRequestDate());
            ps.setString(3, importRequest.getStatus().name());
            ps.setString(4, importRequest.getCreatedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                importRequest.setId(requireGeneratedId(keys));
            }
            return importRequest;
        } catch (SQLException e) {
            throw failed("Failed to save import request", e);
        }
    }

    @Override
    public void update(ImportRequest importRequest) {
        String sql = """
                UPDATE import_request
                SET request_code = ?, request_date = ?, status = ?, created_at = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, importRequest.getRequestCode());
            ps.setString(2, importRequest.getRequestDate());
            ps.setString(3, importRequest.getStatus().name());
            ps.setString(4, importRequest.getCreatedAt());
            ps.setLong(5, importRequest.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update import request id: " + importRequest.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM import_request WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete import request id: " + id, e);
        }
    }

    @Override
    public void updateStatus(long requestId, ImportRequestStatus status) {
        try (Connection connection = openConnection()) {
            updateStatus(connection, requestId, status);
        } catch (SQLException e) {
            throw failed("Failed to update import request status for id: " + requestId, e);
        }
    }

    @Override
    public void updateStatus(Connection connection, long requestId, ImportRequestStatus status) {
        String sql = "UPDATE import_request SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update import request status for id: " + requestId, e);
        }
    }

    private static ImportRequest mapRow(ResultSet rs) throws SQLException {
        ImportRequest request = new ImportRequest();
        request.setId(rs.getLong("id"));
        request.setRequestCode(rs.getString("request_code"));
        request.setRequestDate(rs.getString("request_date"));
        request.setStatus(ImportRequestStatus.fromDbValue(rs.getString("status")));
        request.setCreatedAt(rs.getString("created_at"));
        return request;
    }
}
