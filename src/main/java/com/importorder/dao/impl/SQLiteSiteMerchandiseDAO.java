package com.importorder.dao.impl;

import com.importorder.dao.SiteMerchandiseDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.SiteMerchandise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSiteMerchandiseDAO extends JdbcDaoSupport implements SiteMerchandiseDAO {

    public SQLiteSiteMerchandiseDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<SiteMerchandise> findAll() {
        String sql = "SELECT id, site_id, merchandise_id FROM site_merchandise ORDER BY site_id, merchandise_id";
        return queryList(sql);
    }

    @Override
    public Optional<SiteMerchandise> findById(long id) {
        String sql = "SELECT id, site_id, merchandise_id FROM site_merchandise WHERE id = ?";
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
            throw failed("Failed to find site merchandise by id: " + id, e);
        }
    }

    @Override
    public Optional<SiteMerchandise> findBySiteIdAndMerchandiseId(long siteId, long merchandiseId) {
        String sql = "SELECT id, site_id, merchandise_id FROM site_merchandise WHERE site_id = ? AND merchandise_id = ?";
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
            throw failed("Failed to find site merchandise for site " + siteId + " and merchandise " + merchandiseId, e);
        }
    }

    @Override
    public List<SiteMerchandise> findBySiteId(long siteId) {
        String sql = "SELECT id, site_id, merchandise_id FROM site_merchandise WHERE site_id = ? ORDER BY merchandise_id";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, siteId);
            try (ResultSet rs = ps.executeQuery()) {
                List<SiteMerchandise> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find site merchandise by site id: " + siteId, e);
        }
    }

    @Override
    public List<SiteMerchandise> findByMerchandiseId(long merchandiseId) {
        String sql = "SELECT id, site_id, merchandise_id FROM site_merchandise WHERE merchandise_id = ? ORDER BY site_id";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, merchandiseId);
            try (ResultSet rs = ps.executeQuery()) {
                List<SiteMerchandise> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find site merchandise by merchandise id: " + merchandiseId, e);
        }
    }

    @Override
    public SiteMerchandise save(SiteMerchandise siteMerchandise) {
        String sql = "INSERT INTO site_merchandise (site_id, merchandise_id) VALUES (?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, siteMerchandise.getSiteId());
            ps.setLong(2, siteMerchandise.getMerchandiseId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                siteMerchandise.setId(requireGeneratedId(keys));
            }
            return siteMerchandise;
        } catch (SQLException e) {
            throw failed("Failed to save site merchandise", e);
        }
    }

    @Override
    public void update(SiteMerchandise siteMerchandise) {
        String sql = "UPDATE site_merchandise SET site_id = ?, merchandise_id = ? WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, siteMerchandise.getSiteId());
            ps.setLong(2, siteMerchandise.getMerchandiseId());
            ps.setLong(3, siteMerchandise.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update site merchandise id: " + siteMerchandise.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM site_merchandise WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete site merchandise id: " + id, e);
        }
    }

    private List<SiteMerchandise> queryList(String sql) {
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<SiteMerchandise> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to query site merchandise", e);
        }
    }

    private static SiteMerchandise mapRow(ResultSet rs) throws SQLException {
        SiteMerchandise siteMerchandise = new SiteMerchandise();
        siteMerchandise.setId(rs.getLong("id"));
        siteMerchandise.setSiteId(rs.getLong("site_id"));
        siteMerchandise.setMerchandiseId(rs.getLong("merchandise_id"));
        return siteMerchandise;
    }
}
