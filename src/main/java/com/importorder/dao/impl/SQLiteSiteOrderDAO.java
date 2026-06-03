package com.importorder.dao.impl;

import com.importorder.dao.SiteOrderDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.SiteOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSiteOrderDAO extends JdbcDaoSupport implements SiteOrderDAO {

    public SQLiteSiteOrderDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<SiteOrder> findAll() {
        String sql = "SELECT id, import_request_id, site_id, status, created_at, sent_at FROM site_order ORDER BY id DESC";
        return queryList(sql);
    }

    @Override
    public Optional<SiteOrder> findById(long id) {
        String sql = "SELECT id, import_request_id, site_id, status, created_at, sent_at FROM site_order WHERE id = ?";
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
            throw failed("Failed to find site order by id: " + id, e);
        }
    }

    @Override
    public List<SiteOrder> findByImportRequestId(long importRequestId) {
        String sql = """
                SELECT id, import_request_id, site_id, status, created_at, sent_at
                FROM site_order WHERE import_request_id = ? ORDER BY site_id
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, importRequestId);
            try (ResultSet rs = ps.executeQuery()) {
                List<SiteOrder> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find site orders for import request id: " + importRequestId, e);
        }
    }

    @Override
    public SiteOrder save(SiteOrder siteOrder) {
        try (Connection connection = openConnection()) {
            return save(connection, siteOrder);
        } catch (SQLException e) {
            throw failed("Failed to save site order", e);
        }
    }

    @Override
    public SiteOrder save(Connection connection, SiteOrder siteOrder) {
        String sql = """
                INSERT INTO site_order (import_request_id, site_id, status, created_at, sent_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, siteOrder.getImportRequestId());
            ps.setLong(2, siteOrder.getSiteId());
            ps.setString(3, siteOrder.getStatus());
            ps.setString(4, siteOrder.getCreatedAt());
            ps.setString(5, siteOrder.getSentAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                siteOrder.setId(requireGeneratedId(keys));
            }
            return siteOrder;
        } catch (SQLException e) {
            throw failed("Failed to save site order", e);
        }
    }

    @Override
    public void update(SiteOrder siteOrder) {
        String sql = """
                UPDATE site_order
                SET import_request_id = ?, site_id = ?, status = ?, created_at = ?, sent_at = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, siteOrder.getImportRequestId());
            ps.setLong(2, siteOrder.getSiteId());
            ps.setString(3, siteOrder.getStatus());
            ps.setString(4, siteOrder.getCreatedAt());
            ps.setString(5, siteOrder.getSentAt());
            ps.setLong(6, siteOrder.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update site order id: " + siteOrder.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM site_order WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete site order id: " + id, e);
        }
    }

    private List<SiteOrder> queryList(String sql) {
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<SiteOrder> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to query site orders", e);
        }
    }

    private static SiteOrder mapRow(ResultSet rs) throws SQLException {
        SiteOrder siteOrder = new SiteOrder();
        siteOrder.setId(rs.getLong("id"));
        siteOrder.setImportRequestId(rs.getLong("import_request_id"));
        siteOrder.setSiteId(rs.getLong("site_id"));
        siteOrder.setStatus(rs.getString("status"));
        siteOrder.setCreatedAt(rs.getString("created_at"));
        siteOrder.setSentAt(rs.getString("sent_at"));
        return siteOrder;
    }
}
