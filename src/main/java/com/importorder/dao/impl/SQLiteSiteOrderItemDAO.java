package com.importorder.dao.impl;

import com.importorder.dao.SiteOrderItemDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.SiteOrderItem;
import com.importorder.model.enums.DeliveryMeans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSiteOrderItemDAO extends JdbcDaoSupport implements SiteOrderItemDAO {

    public SQLiteSiteOrderItemDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<SiteOrderItem> findAll() {
        String sql = """
                SELECT id, site_order_id, merchandise_id, quantity_ordered, unit, delivery_means
                FROM site_order_item ORDER BY site_order_id, id
                """;
        return queryList(sql);
    }

    @Override
    public Optional<SiteOrderItem> findById(long id) {
        String sql = """
                SELECT id, site_order_id, merchandise_id, quantity_ordered, unit, delivery_means
                FROM site_order_item WHERE id = ?
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
            throw failed("Failed to find site order item by id: " + id, e);
        }
    }

    @Override
    public List<SiteOrderItem> findBySiteOrderId(long siteOrderId) {
        String sql = """
                SELECT id, site_order_id, merchandise_id, quantity_ordered, unit, delivery_means
                FROM site_order_item WHERE site_order_id = ? ORDER BY id
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, siteOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<SiteOrderItem> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find site order items for site order id: " + siteOrderId, e);
        }
    }

    @Override
    public SiteOrderItem save(SiteOrderItem siteOrderItem) {
        try (Connection connection = openConnection()) {
            return save(connection, siteOrderItem);
        } catch (SQLException e) {
            throw failed("Failed to save site order item", e);
        }
    }

    @Override
    public SiteOrderItem save(Connection connection, SiteOrderItem siteOrderItem) {
        String sql = """
                INSERT INTO site_order_item (site_order_id, merchandise_id, quantity_ordered, unit, delivery_means)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, siteOrderItem.getSiteOrderId());
            ps.setLong(2, siteOrderItem.getMerchandiseId());
            ps.setInt(3, siteOrderItem.getQuantityOrdered());
            ps.setString(4, siteOrderItem.getUnit());
            ps.setString(5, siteOrderItem.getDeliveryMeans().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                siteOrderItem.setId(requireGeneratedId(keys));
            }
            return siteOrderItem;
        } catch (SQLException e) {
            throw failed("Failed to save site order item", e);
        }
    }

    @Override
    public void update(SiteOrderItem siteOrderItem) {
        String sql = """
                UPDATE site_order_item
                SET site_order_id = ?, merchandise_id = ?, quantity_ordered = ?, unit = ?, delivery_means = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, siteOrderItem.getSiteOrderId());
            ps.setLong(2, siteOrderItem.getMerchandiseId());
            ps.setInt(3, siteOrderItem.getQuantityOrdered());
            ps.setString(4, siteOrderItem.getUnit());
            ps.setString(5, siteOrderItem.getDeliveryMeans().name());
            ps.setLong(6, siteOrderItem.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update site order item id: " + siteOrderItem.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM site_order_item WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete site order item id: " + id, e);
        }
    }

    private List<SiteOrderItem> queryList(String sql) {
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<SiteOrderItem> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to query site order items", e);
        }
    }

    private static SiteOrderItem mapRow(ResultSet rs) throws SQLException {
        SiteOrderItem item = new SiteOrderItem();
        item.setId(rs.getLong("id"));
        item.setSiteOrderId(rs.getLong("site_order_id"));
        item.setMerchandiseId(rs.getLong("merchandise_id"));
        item.setQuantityOrdered(rs.getInt("quantity_ordered"));
        item.setUnit(rs.getString("unit"));
        item.setDeliveryMeans(DeliveryMeans.fromDbValue(rs.getString("delivery_means")));
        return item;
    }
}
