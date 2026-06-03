package com.importorder.dao.impl;

import com.importorder.dao.ImportSiteDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.ImportSite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteImportSiteDAO extends JdbcDaoSupport implements ImportSiteDAO {

    public SQLiteImportSiteDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<ImportSite> findAll() {
        String sql = """
                SELECT id, site_code, site_name, ship_delivery_days, air_delivery_days, other_information
                FROM import_site ORDER BY site_code
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ImportSite> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to find all import sites", e);
        }
    }

    @Override
    public Optional<ImportSite> findById(long id) {
        String sql = """
                SELECT id, site_code, site_name, ship_delivery_days, air_delivery_days, other_information
                FROM import_site WHERE id = ?
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
            throw failed("Failed to find import site by id: " + id, e);
        }
    }

    @Override
    public Optional<ImportSite> findByCode(String siteCode) {
        String sql = """
                SELECT id, site_code, site_name, ship_delivery_days, air_delivery_days, other_information
                FROM import_site WHERE site_code = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, siteCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw failed("Failed to find import site by code: " + siteCode, e);
        }
    }

    @Override
    public ImportSite save(ImportSite importSite) {
        String sql = """
                INSERT INTO import_site (site_code, site_name, ship_delivery_days, air_delivery_days, other_information)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, importSite.getSiteCode());
            ps.setString(2, importSite.getSiteName());
            ps.setInt(3, importSite.getShipDeliveryDays());
            ps.setInt(4, importSite.getAirDeliveryDays());
            ps.setString(5, importSite.getOtherInformation());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                importSite.setId(requireGeneratedId(keys));
            }
            return importSite;
        } catch (SQLException e) {
            throw failed("Failed to save import site", e);
        }
    }

    @Override
    public void update(ImportSite importSite) {
        String sql = """
                UPDATE import_site
                SET site_code = ?, site_name = ?, ship_delivery_days = ?, air_delivery_days = ?, other_information = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, importSite.getSiteCode());
            ps.setString(2, importSite.getSiteName());
            ps.setInt(3, importSite.getShipDeliveryDays());
            ps.setInt(4, importSite.getAirDeliveryDays());
            ps.setString(5, importSite.getOtherInformation());
            ps.setLong(6, importSite.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update import site id: " + importSite.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM import_site WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete import site id: " + id, e);
        }
    }

    private static ImportSite mapRow(ResultSet rs) throws SQLException {
        ImportSite site = new ImportSite();
        site.setId(rs.getLong("id"));
        site.setSiteCode(rs.getString("site_code"));
        site.setSiteName(rs.getString("site_name"));
        site.setShipDeliveryDays(rs.getInt("ship_delivery_days"));
        site.setAirDeliveryDays(rs.getInt("air_delivery_days"));
        site.setOtherInformation(rs.getString("other_information"));
        return site;
    }
}
