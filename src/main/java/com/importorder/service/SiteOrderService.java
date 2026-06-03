package com.importorder.service;

import com.importorder.dao.AllocationResultDAO;
import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportRequestDAO;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.dao.InventoryDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.dao.SiteOrderDAO;
import com.importorder.dao.SiteOrderItemDAO;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.TransactionRunner;
import com.importorder.model.AllocationResult;
import com.importorder.model.ImportRequest;
import com.importorder.model.ImportSite;
import com.importorder.model.Merchandise;
import com.importorder.model.SiteOrder;
import com.importorder.model.SiteOrderItem;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.service.siteorder.SiteOrderItemRow;
import com.importorder.service.siteorder.SiteOrderRow;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use cases for overseas site orders after allocation is confirmed.
 */
public class SiteOrderService {

    public static final String SITE_ORDER_STATUS_SENT = "SENT";
    public static final String SITE_ORDER_STATUS_RECEIVED = "RECEIVED";

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Set<ImportRequestStatus> CONFIRMABLE_STATUSES = EnumSet.of(
            ImportRequestStatus.ALLOCATED,
            ImportRequestStatus.PARTIALLY_ALLOCATED);

    private final ImportRequestDAO importRequestDAO;
    private final ImportSiteDAO importSiteDAO;
    private final MerchandiseDAO merchandiseDAO;
    private final AllocationResultDAO allocationResultDAO;
    private final SiteOrderDAO siteOrderDAO;
    private final SiteOrderItemDAO siteOrderItemDAO;
    private final InventoryDAO inventoryDAO;
    private final DatabaseConnectionFactory connectionFactory;

    public SiteOrderService(DAOFactory daoFactory) {
        this.importRequestDAO = daoFactory.createImportRequestDAO();
        this.importSiteDAO = daoFactory.createImportSiteDAO();
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
        this.allocationResultDAO = daoFactory.createAllocationResultDAO();
        this.siteOrderDAO = daoFactory.createSiteOrderDAO();
        this.siteOrderItemDAO = daoFactory.createSiteOrderItemDAO();
        this.inventoryDAO = daoFactory.createInventoryDAO();
        this.connectionFactory = daoFactory.getConnectionFactory();
    }

    public List<SiteOrder> findByImportRequestId(long importRequestId) {
        return siteOrderDAO.findByImportRequestId(importRequestId);
    }

    public List<SiteOrderItem> findItemsBySiteOrderId(long siteOrderId) {
        return siteOrderItemDAO.findBySiteOrderId(siteOrderId);
    }

    public List<SiteOrderRow> findAllRows() {
        return siteOrderDAO.findAll().stream()
                .map(this::toSiteOrderRow)
                .toList();
    }

    public List<SiteOrderItemRow> findItemRowsBySiteOrderId(long siteOrderId) {
        return siteOrderItemDAO.findBySiteOrderId(siteOrderId).stream()
                .map(this::toSiteOrderItemRow)
                .toList();
    }

    public boolean canConfirmOrders(ImportRequestStatus status) {
        return CONFIRMABLE_STATUSES.contains(status);
    }

    /**
     * Creates site orders from successful allocations and decreases inventory atomically.
     */
    public void confirmOrders(long importRequestId) {
        ImportRequest request = importRequestDAO.findById(importRequestId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy yêu cầu nhập hàng: " + importRequestId));

        if (request.getStatus() == ImportRequestStatus.ORDER_SENT) {
            throw new ValidationException("Đơn đặt site đã được xác nhận cho yêu cầu này");
        }

        if (!CONFIRMABLE_STATUSES.contains(request.getStatus())) {
            throw new ValidationException(
                    "Chỉ yêu cầu đã phân bổ hoặc phân bổ một phần mới có thể xác nhận đơn đặt");
        }

        List<AllocationResult> successfulAllocations =
                allocationResultDAO.findSuccessfulByRequestId(importRequestId);
        if (successfulAllocations.isEmpty()) {
            throw new ValidationException("Không có kết quả phân bổ thành công để xác nhận");
        }

        Map<Long, List<AllocationResult>> allocationsBySite = successfulAllocations.stream()
                .collect(Collectors.groupingBy(AllocationResult::getSiteId));

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        TransactionRunner.runInTransaction(connectionFactory, connection ->
                confirmOrdersInTransaction(connection, importRequestId, allocationsBySite, timestamp));
    }

    private void confirmOrdersInTransaction(Connection connection, long importRequestId,
                                            Map<Long, List<AllocationResult>> allocationsBySite,
                                            String timestamp) {
        for (Map.Entry<Long, List<AllocationResult>> entry : allocationsBySite.entrySet()) {
            long siteId = entry.getKey();
            List<AllocationResult> siteAllocations = entry.getValue();

            SiteOrder siteOrder = new SiteOrder(importRequestId, siteId, SITE_ORDER_STATUS_SENT);
            siteOrder.setCreatedAt(timestamp);
            siteOrder.setSentAt(timestamp);
            siteOrderDAO.save(connection, siteOrder);

            for (AllocationResult allocation : siteAllocations) {
                SiteOrderItem item = new SiteOrderItem(
                        siteOrder.getId(),
                        allocation.getMerchandiseId(),
                        allocation.getAllocatedQuantity(),
                        allocation.getUnit(),
                        allocation.getDeliveryMeans());
                siteOrderItemDAO.save(connection, item);

                inventoryDAO.decreaseStock(
                        connection,
                        siteId,
                        allocation.getMerchandiseId(),
                        allocation.getAllocatedQuantity());
            }
        }

        importRequestDAO.updateStatus(connection, importRequestId, ImportRequestStatus.ORDER_SENT);
    }

    private SiteOrderRow toSiteOrderRow(SiteOrder siteOrder) {
        ImportRequest request = importRequestDAO.findById(siteOrder.getImportRequestId())
                .orElseThrow(() -> new ValidationException(
                        "Không tìm thấy yêu cầu nhập hàng: " + siteOrder.getImportRequestId()));
        ImportSite site = importSiteDAO.findById(siteOrder.getSiteId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy site nhập khẩu: " + siteOrder.getSiteId()));
        return new SiteOrderRow(
                siteOrder.getId(),
                siteOrder.getImportRequestId(),
                request.getRequestCode(),
                site.getSiteCode(),
                site.getSiteName(),
                siteOrder.getStatus(),
                siteOrder.getCreatedAt() != null ? siteOrder.getCreatedAt() : "",
                siteOrder.getSentAt() != null ? siteOrder.getSentAt() : "");
    }

    private SiteOrderItemRow toSiteOrderItemRow(SiteOrderItem item) {
        Merchandise merchandise = merchandiseDAO.findById(item.getMerchandiseId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy mặt hàng: " + item.getMerchandiseId()));
        return new SiteOrderItemRow(
                item.getId(),
                merchandise.getCode(),
                merchandise.getName(),
                item.getQuantityOrdered(),
                item.getUnit(),
                item.getDeliveryMeans());
    }
}
