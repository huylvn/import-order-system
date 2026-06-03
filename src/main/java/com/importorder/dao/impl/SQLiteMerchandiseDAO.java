package com.importorder.dao.impl;

import com.importorder.dao.MerchandiseDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.Merchandise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteMerchandiseDAO extends JdbcDaoSupport implements MerchandiseDAO {

    public SQLiteMerchandiseDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<Merchandise> findAll() {
        String sql = "SELECT id, code, name, unit, description FROM merchandise ORDER BY code";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Merchandise> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to find all merchandise", e);
        }
    }

    @Override
    public Optional<Merchandise> findById(long id) {
        String sql = "SELECT id, code, name, unit, description FROM merchandise WHERE id = ?";
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
            throw failed("Failed to find merchandise by id: " + id, e);
        }
    }

    @Override
    public Optional<Merchandise> findByCode(String code) {
        String sql = "SELECT id, code, name, unit, description FROM merchandise WHERE code = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw failed("Failed to find merchandise by code: " + code, e);
        }
    }

    @Override
    public Merchandise save(Merchandise merchandise) {
        String sql = "INSERT INTO merchandise (code, name, unit, description) VALUES (?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, merchandise.getCode());
            ps.setString(2, merchandise.getName());
            ps.setString(3, merchandise.getUnit());
            ps.setString(4, merchandise.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                merchandise.setId(requireGeneratedId(keys));
            }
            return merchandise;
        } catch (SQLException e) {
            throw failed("Failed to save merchandise", e);
        }
    }

    @Override
    public void update(Merchandise merchandise) {
        String sql = "UPDATE merchandise SET code = ?, name = ?, unit = ?, description = ? WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, merchandise.getCode());
            ps.setString(2, merchandise.getName());
            ps.setString(3, merchandise.getUnit());
            ps.setString(4, merchandise.getDescription());
            ps.setLong(5, merchandise.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update merchandise id: " + merchandise.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM merchandise WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete merchandise id: " + id, e);
        }
    }

    private static Merchandise mapRow(ResultSet rs) throws SQLException {
        Merchandise merchandise = new Merchandise();
        merchandise.setId(rs.getLong("id"));
        merchandise.setCode(rs.getString("code"));
        merchandise.setName(rs.getString("name"));
        merchandise.setUnit(rs.getString("unit"));
        merchandise.setDescription(rs.getString("description"));
        return merchandise;
    }
}
