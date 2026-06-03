package com.importorder.dao.impl;

import com.importorder.dao.InventoryDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseException;
import com.importorder.model.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteInventoryDAO extends JdbcDaoSupport implements InventoryDAO {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteInventoryDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<Inventory> findAll() {
        String sql = """
                SELECT id, site_id, merchandise_id, in_stock_quantity, unit, last_updated_at
                FROM inventory ORDER BY site_id, merchandise_id
                """;
        return queryList(sql);
    }

    @Override
    public Optional<Inventory> findById(long id) {
        String sql = """
                SELECT id, site_id, merchandise_id, in_stock_quantity, unit, last_updated_at
                FROM inventory WHERE id = ?
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
            throw failed("Failed to find inventory by id: " + id, e);
        }
    }

    @Override
    public Optional<Inventory> findBySiteIdAndMerchandiseId(long siteId, long merchandiseId) {
        String sql = """
                SELECT id, site_id, merchandise_id, in_stock_quantity, unit, last_updated_at
                FROM inventory WHERE site_id = ? AND merchandise_id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, siteId);
            ps.setLong(2, merchandiseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw failed("Failed to find inventory for site " + siteId + " and merchandise " + merchandiseId, e);
        }
    }

    @Override
    public List<Inventory> findByMerchandiseId(long merchandiseId) {
        String sql = """
                SELECT id, site_id, merchandise_id, in_stock_quantity, unit, last_updated_at
                FROM inventory WHERE merchandise_id = ? ORDER BY site_id
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, merchandiseId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Inventory> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find inventory by merchandise id: " + merchandiseId, e);
        }
    }

    @Override
    public Inventory save(Inventory inventory) {
        String sql = """
                INSERT INTO inventory (site_id, merchandise_id, in_stock_quantity, unit, last_updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        String timestamp = currentTimestamp();
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, inventory.getSiteId());
            ps.setLong(2, inventory.getMerchandiseId());
            ps.setInt(3, inventory.getInStockQuantity());
            ps.setString(4, inventory.getUnit());
            ps.setString(5, inventory.getLastUpdatedAt() != null ? inventory.getLastUpdatedAt() : timestamp);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                inventory.setId(requireGeneratedId(keys));
            }
            if (inventory.getLastUpdatedAt() == null) {
                inventory.setLastUpdatedAt(timestamp);
            }
            return inventory;
        } catch (SQLException e) {
            throw failed("Failed to save inventory", e);
        }
    }

    @Override
    public void update(Inventory inventory) {
        String sql = """
                UPDATE inventory
                SET site_id = ?, merchandise_id = ?, in_stock_quantity = ?, unit = ?, last_updated_at = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, inventory.getSiteId());
            ps.setLong(2, inventory.getMerchandiseId());
            ps.setInt(3, inventory.getInStockQuantity());
            ps.setString(4, inventory.getUnit());
            ps.setString(5, inventory.getLastUpdatedAt() != null ? inventory.getLastUpdatedAt() : currentTimestamp());
            ps.setLong(6, inventory.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update inventory id: " + inventory.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM inventory WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete inventory id: " + id, e);
        }
    }

    @Override
    public void decreaseStock(long siteId, long merchandiseId, int quantity) {
        try (Connection connection = openConnection()) {
            decreaseStock(connection, siteId, merchandiseId, quantity);
        } catch (SQLException e) {
            throw failed("Failed to decrease stock", e);
        }
    }

    @Override
    public void decreaseStock(Connection connection, long siteId, long merchandiseId, int quantity) {
        if (quantity <= 0) {
            throw new DatabaseException("Decrease quantity must be positive");
        }
        String sql = """
                UPDATE inventory
                SET in_stock_quantity = in_stock_quantity - ?, last_updated_at = ?
                WHERE site_id = ? AND merchandise_id = ? AND in_stock_quantity >= ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, currentTimestamp());
            ps.setLong(3, siteId);
            ps.setLong(4, merchandiseId);
            ps.setInt(5, quantity);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new DatabaseException(
                        "Insufficient stock or inventory not found for site " + siteId + " and merchandise " + merchandiseId);
            }
        } catch (SQLException e) {
            throw failed("Failed to decrease stock", e);
        }
    }

    private List<Inventory> queryList(String sql) {
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Inventory> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to query inventory", e);
        }
    }

    private static Inventory mapRow(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setId(rs.getLong("id"));
        inventory.setSiteId(rs.getLong("site_id"));
        inventory.setMerchandiseId(rs.getLong("merchandise_id"));
        inventory.setInStockQuantity(rs.getInt("in_stock_quantity"));
        inventory.setUnit(rs.getString("unit"));
        inventory.setLastUpdatedAt(rs.getString("last_updated_at"));
        return inventory;
    }

    private static String currentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
}
