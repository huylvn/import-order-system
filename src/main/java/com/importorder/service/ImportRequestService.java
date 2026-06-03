package com.importorder.service;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportRequestDAO;
import com.importorder.dao.ImportRequestItemDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.model.ImportRequest;
import com.importorder.model.ImportRequestItem;
import com.importorder.model.Merchandise;
import com.importorder.model.enums.ImportRequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Use cases for sales import requests.
 */
public class ImportRequestService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ImportRequestDAO importRequestDAO;
    private final ImportRequestItemDAO importRequestItemDAO;
    private final MerchandiseDAO merchandiseDAO;

    public ImportRequestService(DAOFactory daoFactory) {
        this.importRequestDAO = daoFactory.createImportRequestDAO();
        this.importRequestItemDAO = daoFactory.createImportRequestItemDAO();
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
    }

    public ImportRequest createDraft() {
        LocalDate today = LocalDate.now();
        ImportRequest request = new ImportRequest(
                generateRequestCode(),
                today.toString(),
                ImportRequestStatus.DRAFT);
        request.setCreatedAt(LocalDateTime.now().format(TIMESTAMP_FORMAT));
        return importRequestDAO.save(request);
    }

    public ImportRequest createRequest(String requestCode, String requestDate,
                                       List<ImportRequestItem> items,
                                       ImportRequestStatus status) {
        ServiceValidation.requireNonBlank(requestCode, "requestCode");
        ServiceValidation.parseDate(requestDate, "requestDate");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (status != ImportRequestStatus.DRAFT && status != ImportRequestStatus.SUBMITTED) {
            throw new ValidationException("Yêu cầu nhập hàng chỉ có thể được tạo ở trạng thái Nháp hoặc Đã gửi");
        }
        if (items.isEmpty()) {
            throw new ValidationException("Yêu cầu nhập hàng phải có ít nhất một mặt hàng");
        }

        ImportRequest request = new ImportRequest(
                requestCode.trim(),
                requestDate.trim(),
                ImportRequestStatus.DRAFT);
        request.setCreatedAt(LocalDateTime.now().format(TIMESTAMP_FORMAT));

        for (ImportRequestItem item : items) {
            validateRequestItem(request, item);
            ensureMerchandiseExists(item.getMerchandiseId());
        }

        ImportRequest savedRequest = importRequestDAO.save(request);
        for (ImportRequestItem item : items) {
            item.setImportRequestId(savedRequest.getId());
            importRequestItemDAO.save(item);
        }

        if (status == ImportRequestStatus.SUBMITTED) {
            importRequestDAO.updateStatus(savedRequest.getId(), ImportRequestStatus.SUBMITTED);
            savedRequest.setStatus(ImportRequestStatus.SUBMITTED);
        }
        return savedRequest;
    }

    public ImportRequestItem addItem(long requestId, ImportRequestItem item) {
        Objects.requireNonNull(item, "item must not be null");
        ImportRequest request = requireDraftRequest(requestId);
        validateRequestItem(request, item);
        ensureMerchandiseExists(item.getMerchandiseId());

        item.setImportRequestId(request.getId());
        return importRequestItemDAO.save(item);
    }

    public ImportRequest submitRequest(long requestId) {
        ImportRequest request = requireDraftRequest(requestId);
        List<ImportRequestItem> items = importRequestItemDAO.findByRequestId(requestId);
        if (items.isEmpty()) {
            throw new ValidationException("Không thể gửi yêu cầu nhập hàng khi chưa có mặt hàng");
        }
        importRequestDAO.updateStatus(requestId, ImportRequestStatus.SUBMITTED);
        request.setStatus(ImportRequestStatus.SUBMITTED);
        return request;
    }

    public List<ImportRequest> findAll() {
        return importRequestDAO.findAll();
    }

    public Optional<ImportRequest> findById(long id) {
        return importRequestDAO.findById(id);
    }

    public List<ImportRequestItem> findItemsByRequestId(long requestId) {
        return importRequestItemDAO.findByRequestId(requestId);
    }

    public List<Merchandise> findAllMerchandise() {
        return merchandiseDAO.findAll();
    }

    public void validateRequestItem(ImportRequest request, ImportRequestItem item) {
        Objects.requireNonNull(request, "request must not be null");
        if (item.getMerchandiseId() == null) {
            throw new ValidationException("Mặt hàng là bắt buộc");
        }
        ServiceValidation.requirePositive(item.getQuantityOrdered(), "quantityOrdered");
        ServiceValidation.requireNonBlank(item.getUnit(), "unit");
        ServiceValidation.requireDeliveryDateOnOrAfterRequestDate(
                request.getRequestDate(), item.getDesiredDeliveryDate());
    }

    private ImportRequest requireDraftRequest(long requestId) {
        ImportRequest request = importRequestDAO.findById(requestId)
                .orElseThrow(() -> new ValidationException("Import request not found: " + requestId));
        if (request.getStatus() != ImportRequestStatus.DRAFT) {
            throw new ValidationException("Chỉ yêu cầu ở trạng thái Nháp mới có thể chỉnh sửa");
        }
        return request;
    }

    private void ensureMerchandiseExists(long merchandiseId) {
        if (merchandiseDAO.findById(merchandiseId).isEmpty()) {
            throw new ValidationException("Không tìm thấy mặt hàng: " + merchandiseId);
        }
    }

    private static String generateRequestCode() {
        return "REQ-" + LocalDateTime.now().format(CODE_DATE_FORMAT);
    }
}
