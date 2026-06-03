package com.importorder.service;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportRequestDAO;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.dao.ReceivedGoodsDAO;
import com.importorder.dao.SiteOrderDAO;
import com.importorder.dao.SiteOrderItemDAO;
import com.importorder.model.ImportRequest;
import com.importorder.model.ImportSite;
import com.importorder.model.Merchandise;
import com.importorder.model.ReceivedGoods;
import com.importorder.model.SiteOrder;
import com.importorder.model.SiteOrderItem;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.model.enums.ReceivedStatus;
import com.importorder.service.siteorder.SiteOrderRow;
import com.importorder.service.warehouse.WarehouseReceiveItemRow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**
 * Use cases for warehouse goods receiving.
 */
public class WarehouseService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String SITE_ORDER_STATUS_SENT = "SENT";
    private static final String SITE_ORDER_STATUS_RECEIVED = "RECEIVED";

    private final ImportRequestDAO importRequestDAO;
    private final ImportSiteDAO importSiteDAO;
    private final MerchandiseDAO merchandiseDAO;
    private final SiteOrderDAO siteOrderDAO;
    private final ReceivedGoodsDAO receivedGoodsDAO;
    private final SiteOrderItemDAO siteOrderItemDAO;

    public WarehouseService(DAOFactory daoFactory) {
        this.importRequestDAO = daoFactory.createImportRequestDAO();
        this.importSiteDAO = daoFactory.createImportSiteDAO();
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
        this.siteOrderDAO = daoFactory.createSiteOrderDAO();
        this.receivedGoodsDAO = daoFactory.createReceivedGoodsDAO();
        this.siteOrderItemDAO = daoFactory.createSiteOrderItemDAO();
    }

    public List<SiteOrderRow> findSentSiteOrders() {
        return siteOrderDAO.findAll().stream()
                .filter(siteOrder -> SITE_ORDER_STATUS_SENT.equals(siteOrder.getStatus()))
                .map(this::toSiteOrderRow)
                .toList();
    }

    public List<WarehouseReceiveItemRow> findReceiveItemRows(long siteOrderId) {
        return siteOrderItemDAO.findBySiteOrderId(siteOrderId).stream()
                .map(this::toReceiveItemRow)
                .toList();
    }

    public ReceivedGoods receiveGoods(long siteOrderItemId, String actualReceivedQuantityText, String note) {
        ServiceValidation.requireNonBlank(actualReceivedQuantityText, "actualReceivedQuantity");
        try {
            return receiveGoods(siteOrderItemId, Integer.parseInt(actualReceivedQuantityText.trim()), note);
        } catch (NumberFormatException e) {
            throw new ValidationException("Số lượng thực nhận phải là số hợp lệ");
        }
    }

    public ReceivedGoods receiveGoods(long siteOrderItemId, int actualReceivedQuantity, String note) {
        ServiceValidation.requireNonNegative(actualReceivedQuantity, "actualReceivedQuantity");

        SiteOrderItem orderItem = siteOrderItemDAO.findById(siteOrderItemId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy dòng đơn đặt site: " + siteOrderItemId));

        ReceivedStatus status = determineReceivedStatus(orderItem.getQuantityOrdered(), actualReceivedQuantity);
        String receivedAt = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        ReceivedGoods receivedGoods = receivedGoodsDAO.findBySiteOrderItemId(siteOrderItemId)
                .orElseGet(() -> new ReceivedGoods(siteOrderItemId, actualReceivedQuantity, status));

        receivedGoods.setActualReceivedQuantity(actualReceivedQuantity);
        receivedGoods.setStatus(status);
        receivedGoods.setReceivedAt(receivedAt);
        receivedGoods.setNote(note);

        if (receivedGoods.getId() == null) {
            ReceivedGoods saved = receivedGoodsDAO.save(receivedGoods);
            updateCompletionStatuses(orderItem.getSiteOrderId());
            return saved;
        }
        receivedGoodsDAO.update(receivedGoods);
        updateCompletionStatuses(orderItem.getSiteOrderId());
        return receivedGoods;
    }

    static ReceivedStatus determineReceivedStatus(int orderedQuantity, int actualReceivedQuantity) {
        if (actualReceivedQuantity == orderedQuantity) {
            return ReceivedStatus.MATCHED;
        }
        if (actualReceivedQuantity < orderedQuantity) {
            return ReceivedStatus.SHORTAGE;
        }
        return ReceivedStatus.EXCESS;
    }

    private void updateCompletionStatuses(long siteOrderId) {
        SiteOrder siteOrder = siteOrderDAO.findById(siteOrderId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy đơn đặt site: " + siteOrderId));
        if (allItemsReceived(siteOrderId) && !SITE_ORDER_STATUS_RECEIVED.equals(siteOrder.getStatus())) {
            siteOrder.setStatus(SITE_ORDER_STATUS_RECEIVED);
            siteOrderDAO.update(siteOrder);
        }

        if (allSiteOrdersReceived(siteOrder.getImportRequestId())) {
            importRequestDAO.updateStatus(siteOrder.getImportRequestId(), ImportRequestStatus.RECEIVED);
        }
    }

    private boolean allItemsReceived(long siteOrderId) {
        List<SiteOrderItem> items = siteOrderItemDAO.findBySiteOrderId(siteOrderId);
        return !items.isEmpty() && items.stream()
                .allMatch(item -> receivedGoodsDAO.findBySiteOrderItemId(item.getId()).isPresent());
    }

    private boolean allSiteOrdersReceived(long importRequestId) {
        List<SiteOrder> siteOrders = siteOrderDAO.findByImportRequestId(importRequestId);
        return !siteOrders.isEmpty() && siteOrders.stream()
                .allMatch(siteOrder -> SITE_ORDER_STATUS_RECEIVED.equals(siteOrder.getStatus()));
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

    private WarehouseReceiveItemRow toReceiveItemRow(SiteOrderItem item) {
        Merchandise merchandise = merchandiseDAO.findById(item.getMerchandiseId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy mặt hàng: " + item.getMerchandiseId()));
        ReceivedGoods receivedGoods = receivedGoodsDAO.findBySiteOrderItemId(item.getId()).orElse(null);
        return new WarehouseReceiveItemRow(
                item.getId(),
                merchandise.getCode(),
                merchandise.getName(),
                item.getQuantityOrdered(),
                item.getUnit(),
                item.getDeliveryMeans(),
                receivedGoods != null ? receivedGoods.getActualReceivedQuantity() : null,
                receivedGoods != null && receivedGoods.getNote() != null ? receivedGoods.getNote() : "",
                receivedGoods != null ? receivedGoods.getStatus() : null);
    }
}
