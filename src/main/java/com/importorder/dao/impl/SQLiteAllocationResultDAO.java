package com.importorder.dao.impl;

import com.importorder.dao.AllocationResultDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.model.AllocationResult;
import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.DeliveryMeans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteAllocationResultDAO extends JdbcDaoSupport implements AllocationResultDAO {

    private static final String SELECT_COLUMNS = """
            id, import_request_id, request_item_id, site_id, merchandise_id,
            allocated_quantity, unit, delivery_means, status, error_message
            """;

    public SQLiteAllocationResultDAO(DatabaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<AllocationResult> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM allocation_result ORDER BY import_request_id, id";
        return queryList(sql);
    }

    @Override
    public Optional<AllocationResult> findById(long id) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM allocation_result WHERE id = ?";
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
            throw failed("Failed to find allocation result by id: " + id, e);
        }
    }

    @Override
    public List<AllocationResult> findByRequestId(long requestId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM allocation_result WHERE import_request_id = ? ORDER BY id";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                List<AllocationResult> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find allocation results for request id: " + requestId, e);
        }
    }

    @Override
    public List<AllocationResult> findSuccessfulByRequestId(long requestId) {
        String sql = "SELECT " + SELECT_COLUMNS
                + " FROM allocation_result WHERE import_request_id = ? AND status = ? ORDER BY id";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, requestId);
            ps.setString(2, AllocationStatus.SUCCESS.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<AllocationResult> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw failed("Failed to find successful allocation results for request id: " + requestId, e);
        }
    }

    @Override
    public AllocationResult save(AllocationResult allocationResult) {
        String sql = """
                INSERT INTO allocation_result
                (import_request_id, request_item_id, site_id, merchandise_id, allocated_quantity,
                 unit, delivery_means, status, error_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAllocationResult(ps, allocationResult);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                allocationResult.setId(requireGeneratedId(keys));
            }
            return allocationResult;
        } catch (SQLException e) {
            throw failed("Failed to save allocation result", e);
        }
    }

    @Override
    public void update(AllocationResult allocationResult) {
        String sql = """
                UPDATE allocation_result
                SET import_request_id = ?, request_item_id = ?, site_id = ?, merchandise_id = ?,
                    allocated_quantity = ?, unit = ?, delivery_means = ?, status = ?, error_message = ?
                WHERE id = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindAllocationResult(ps, allocationResult);
            ps.setLong(10, allocationResult.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to update allocation result id: " + allocationResult.getId(), e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM allocation_result WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete allocation result id: " + id, e);
        }
    }

    @Override
    public void deleteByRequestId(long requestId) {
        String sql = "DELETE FROM allocation_result WHERE import_request_id = ?";
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failed("Failed to delete allocation results for request id: " + requestId, e);
        }
    }

    private List<AllocationResult> queryList(String sql) {
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<AllocationResult> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw failed("Failed to query allocation results", e);
        }
    }

    private static void bindAllocationResult(PreparedStatement ps, AllocationResult result) throws SQLException {
        ps.setLong(1, result.getImportRequestId());
        ps.setLong(2, result.getRequestItemId());
        if (result.getSiteId() != null) {
            ps.setLong(3, result.getSiteId());
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);
        }
        ps.setLong(4, result.getMerchandiseId());
        if (result.getAllocatedQuantity() != null) {
            ps.setInt(5, result.getAllocatedQuantity());
        } else {
            ps.setNull(5, java.sql.Types.INTEGER);
        }
        ps.setString(6, result.getUnit());
        if (result.getDeliveryMeans() != null) {
            ps.setString(7, result.getDeliveryMeans().name());
        } else {
            ps.setNull(7, java.sql.Types.VARCHAR);
        }
        ps.setString(8, result.getStatus().name());
        ps.setString(9, result.getErrorMessage());
    }

    private static AllocationResult mapRow(ResultSet rs) throws SQLException {
        AllocationResult result = new AllocationResult();
        result.setId(rs.getLong("id"));
        result.setImportRequestId(rs.getLong("import_request_id"));
        result.setRequestItemId(rs.getLong("request_item_id"));
        result.setSiteId(getNullableLong(rs, "site_id"));
        result.setMerchandiseId(rs.getLong("merchandise_id"));
        result.setAllocatedQuantity(getNullableInteger(rs, "allocated_quantity"));
        result.setUnit(rs.getString("unit"));
        String deliveryMeans = rs.getString("delivery_means");
        result.setDeliveryMeans(deliveryMeans != null ? DeliveryMeans.fromDbValue(deliveryMeans) : null);
        result.setStatus(AllocationStatus.fromDbValue(rs.getString("status")));
        result.setErrorMessage(rs.getString("error_message"));
        return result;
    }
}
