package com.importorder.dao.impl;

import com.importorder.dao.ReceivedGoodsDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.ReceivedGoods;
import com.importorder.model.enums.ReceivedStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteReceivedGoodsDAO extends JdbcDaoSupport implements ReceivedGoodsDAO {

    public SQLiteReceivedGoodsDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<ReceivedGoods> findAll() {
        String sql = """
                SELECT id, site_order_item_id, actual_received_quantity, received_at, status, note
                FROM received_goods ORDER BY id DESC
                """;
        return queryList(sql);
    }

    @Override
    public Optional<ReceivedGoods> findById(long id) {
        String sql = """
                SELECT id, site_order_item_id, actual_received_quantity, received_at, status, note
                FROM received_goods WHERE id = ?
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
            throw failed("Failed to find received goods by id: " + id, e);
        }
    }

    @Override
    public Optional<ReceivedGoods> findBySiteOrderItemId(long siteOrderItemId) {
        String sql = """
                SELECT id, site_order_item_id, actual_received_quantity, received_at, status, note
                FROM received_goods WHERE site_order_item_id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, siteOrderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw failed("Failed to find received goods for site order item id: " + siteOrderItemId, e);
        }
    }

    @Override
    public ReceivedGoods save(ReceivedGoods receivedGoods) {
        String sql = """
                INSERT INTO received_goods (site_order_item_id, actual_received_quantity, received_at, status, note)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, receivedGoods.getSiteOrderItemId());
            ps.setInt(2, receivedGoods.getActualReceivedQuantity());
            ps.setString(3, receivedGoods.getReceivedAt());
            ps.setString(4, receivedGoods.getStatus().name());
            ps.setString(5, receivedGoods.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                receivedGoods.setId(requireGeneratedId(keys));
            }
            return receivedGoods;
        } catch (SQLException e) {
            throw failed("Failed to save received goods", e);
        }
    }

    @Override
    public void update(ReceivedGoods receivedGoods) {
        String sql = """
                UPDATE received_goods
                SET site_order_item_id = ?, actual_received_quantity = ?, received_at = ?, status = ?, note = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, receivedGoods.getSiteOrderItemId());
            ps.setInt(2, receivedGoods.getActualReceivedQuantity());
            ps.setString(3, receivedGoods.getReceivedAt());
            ps.setString(4, receivedGoods.getStatus().name());
            ps.setString(5, receivedGoods.getNote());
            ps.setLong(6, receivedGoods.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update received goods id: " + receivedGoods.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM received_goods WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete received goods id: " + id, e);
        }
    }

    private List<ReceivedGoods> queryList(String sql) {
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ReceivedGoods> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to query received goods", e);
        }
    }

    private static ReceivedGoods mapRow(ResultSet rs) throws SQLException {
        ReceivedGoods receivedGoods = new ReceivedGoods();
        receivedGoods.setId(rs.getLong("id"));
        receivedGoods.setSiteOrderItemId(rs.getLong("site_order_item_id"));
        receivedGoods.setActualReceivedQuantity(rs.getInt("actual_received_quantity"));
        receivedGoods.setReceivedAt(rs.getString("received_at"));
        receivedGoods.setStatus(ReceivedStatus.fromDbValue(rs.getString("status")));
        receivedGoods.setNote(rs.getString("note"));
        return receivedGoods;
    }
}
