package com.importorder.dao.impl;

import com.importorder.dao.ImportRequestItemDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.ImportRequestItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteImportRequestItemDAO extends JdbcDaoSupport implements ImportRequestItemDAO {

    public SQLiteImportRequestItemDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<ImportRequestItem> findAll() {
        String sql = """
                SELECT id, import_request_id, merchandise_id, quantity_ordered, unit, desired_delivery_date
                FROM import_request_item ORDER BY import_request_id, id
                """;
        return queryList(sql);
    }

    @Override
    public Optional<ImportRequestItem> findById(long id) {
        String sql = """
                SELECT id, import_request_id, merchandise_id, quantity_ordered, unit, desired_delivery_date
                FROM import_request_item WHERE id = ?
                """;
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
            throw failed("Failed to find import request item by id: " + id, e);
        }
    }

    @Override
    public List<ImportRequestItem> findByRequestId(long requestId) {
        String sql = """
                SELECT id, import_request_id, merchandise_id, quantity_ordered, unit, desired_delivery_date
                FROM import_request_item WHERE import_request_id = ? ORDER BY id
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ImportRequestItem> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find import request items for request id: " + requestId, e);
        }
    }

    @Override
    public ImportRequestItem save(ImportRequestItem item) {
        String sql = """
                INSERT INTO import_request_item
                (import_request_id, merchandise_id, quantity_ordered, unit, desired_delivery_date)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getImportRequestId());
            ps.setLong(2, item.getMerchandiseId());
            ps.setInt(3, item.getQuantityOrdered());
            ps.setString(4, item.getUnit());
            ps.setString(5, item.getDesiredDeliveryDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                item.setId(requireGeneratedId(keys));
            }
            return item;
        } catch (SQLException e) {
            throw failed("Failed to save import request item", e);
        }
    }

    @Override
    public void update(ImportRequestItem item) {
        String sql = """
                UPDATE import_request_item
                SET import_request_id = ?, merchandise_id = ?, quantity_ordered = ?, unit = ?, desired_delivery_date = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, item.getImportRequestId());
            ps.setLong(2, item.getMerchandiseId());
            ps.setInt(3, item.getQuantityOrdered());
            ps.setString(4, item.getUnit());
            ps.setString(5, item.getDesiredDeliveryDate());
            ps.setLong(6, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update import request item id: " + item.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM import_request_item WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete import request item id: " + id, e);
        }
    }

    private List<ImportRequestItem> queryList(String sql) {
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ImportRequestItem> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to query import request items", e);
        }
    }

    private static ImportRequestItem mapRow(ResultSet rs) throws SQLException {
        ImportRequestItem item = new ImportRequestItem();
        item.setId(rs.getLong("id"));
        item.setImportRequestId(rs.getLong("import_request_id"));
        item.setMerchandiseId(rs.getLong("merchandise_id"));
        item.setQuantityOrdered(rs.getInt("quantity_ordered"));
        item.setUnit(rs.getString("unit"));
        item.setDesiredDeliveryDate(rs.getString("desired_delivery_date"));
        return item;
    }
}
