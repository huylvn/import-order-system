package com.importorder.service;

import com.importorder.dao.AllocationResultDAO;
import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportRequestDAO;
import com.importorder.dao.ImportRequestItemDAO;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.dao.InventoryDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.dao.SiteMerchandiseDAO;
import com.importorder.model.AllocationResult;
import com.importorder.model.ImportRequest;
import com.importorder.model.ImportRequestItem;
import com.importorder.model.ImportSite;
import com.importorder.model.Inventory;
import com.importorder.model.Merchandise;
import com.importorder.model.SiteMerchandise;
import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.service.allocation.AllocationCandidate;
import com.importorder.service.allocation.AllocationResponse;
import com.importorder.service.allocation.AllocationResultRow;
import com.importorder.service.allocation.AllocationSorter;
import com.importorder.service.delivery.DeliveryOption;
import com.importorder.service.delivery.DeliveryOptionSelector;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Allocates import request lines to overseas sites by stock, delivery time, and priority rules.
 * Inventory is not decremented here; that happens when orders are confirmed.
 */
public class AllocationService {

    private static final Set<ImportRequestStatus> ALLOCATABLE_STATUSES = EnumSet.of(
            ImportRequestStatus.SUBMITTED,
            ImportRequestStatus.ALLOCATED,
            ImportRequestStatus.PARTIALLY_ALLOCATED,
            ImportRequestStatus.ALLOCATION_FAILED);

    private static final Set<ImportRequestStatus> VIEWABLE_STATUSES = EnumSet.copyOf(ALLOCATABLE_STATUSES);

    private final ImportRequestDAO importRequestDAO;
    private final ImportRequestItemDAO importRequestItemDAO;
    private final MerchandiseDAO merchandiseDAO;
    private final SiteMerchandiseDAO siteMerchandiseDAO;
    private final ImportSiteDAO importSiteDAO;
    private final InventoryDAO inventoryDAO;
    private final AllocationResultDAO allocationResultDAO;
    private final DeliveryOptionSelector deliveryOptionSelector;

    public AllocationService(DAOFactory daoFactory) {
        this(daoFactory, new DeliveryOptionSelector());
    }

    public AllocationService(DAOFactory daoFactory, DeliveryOptionSelector deliveryOptionSelector) {
        this.importRequestDAO = daoFactory.createImportRequestDAO();
        this.importRequestItemDAO = daoFactory.createImportRequestItemDAO();
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
        this.siteMerchandiseDAO = daoFactory.createSiteMerchandiseDAO();
        this.importSiteDAO = daoFactory.createImportSiteDAO();
        this.inventoryDAO = daoFactory.createInventoryDAO();
        this.allocationResultDAO = daoFactory.createAllocationResultDAO();
        this.deliveryOptionSelector = deliveryOptionSelector;
    }

    public AllocationResponse allocateImportRequest(long importRequestId) {
        ImportRequest request = importRequestDAO.findById(importRequestId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy yêu cầu nhập hàng: " + importRequestId));

        if (!ALLOCATABLE_STATUSES.contains(request.getStatus())) {
            throw new ValidationException(
                    "Không thể phân bổ yêu cầu ở trạng thái hiện tại");
        }

        allocationResultDAO.deleteByRequestId(importRequestId);

        List<ImportRequestItem> items = importRequestItemDAO.findByRequestId(importRequestId);
        List<AllocationResult> results = new ArrayList<>();

        for (ImportRequestItem item : items) {
            results.addAll(allocateItem(request, item));
        }

        ImportRequestStatus finalStatus = determineFinalStatus(results);
        for (AllocationResult result : results) {
            allocationResultDAO.save(result);
        }
        importRequestDAO.updateStatus(importRequestId, finalStatus);

        return new AllocationResponse(importRequestId, finalStatus, results);
    }

    public List<ImportRequest> findRequestsForAllocation() {
        return importRequestDAO.findAll().stream()
                .filter(request -> VIEWABLE_STATUSES.contains(request.getStatus()))
                .toList();
    }

    public ImportRequest findRequestById(long importRequestId) {
        return importRequestDAO.findById(importRequestId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy yêu cầu nhập hàng: " + importRequestId));
    }

    public List<AllocationResultRow> findResultRowsByRequestId(long importRequestId) {
        return allocationResultDAO.findByRequestId(importRequestId).stream()
                .map(this::toResultRow)
                .toList();
    }

    private List<AllocationResult> allocateItem(ImportRequest request, ImportRequestItem item) {
        Merchandise merchandise = merchandiseDAO.findById(item.getMerchandiseId())
                .orElseThrow(() -> new ValidationException(
                        "Không tìm thấy mặt hàng: " + item.getMerchandiseId()));

        int availableDays = calculateAvailableDays(request.getRequestDate(), item.getDesiredDeliveryDate());
        List<AllocationCandidate> candidates = buildCandidates(request, item, merchandise, availableDays);

        // Business rule: ship before air, then highest stock first to minimize number of sites used
        List<AllocationCandidate> sortedCandidates = AllocationSorter.sort(candidates);

        List<AllocationResult> itemResults = new ArrayList<>();
        int remainingQuantity = item.getQuantityOrdered();

        // Greedy allocation: take maximum from each candidate in priority order
        for (AllocationCandidate candidate : sortedCandidates) {
            if (remainingQuantity == 0) {
                break;
            }
            int allocatedQuantity = Math.min(remainingQuantity, candidate.getAvailableStock());
            itemResults.add(createSuccessResult(request, item, candidate, allocatedQuantity));
            remainingQuantity -= allocatedQuantity;
        }

        if (remainingQuantity > 0) {
            itemResults.add(createFailureResult(request, item, merchandise, remainingQuantity));
        }

        return itemResults;
    }

    private List<AllocationCandidate> buildCandidates(ImportRequest request, ImportRequestItem item,
                                                        Merchandise merchandise, int availableDays) {
        List<AllocationCandidate> candidates = new ArrayList<>();

        for (SiteMerchandise catalogEntry : siteMerchandiseDAO.findByMerchandiseId(merchandise.getId())) {
            Optional<ImportSite> siteOptional = importSiteDAO.findById(catalogEntry.getSiteId());
            if (siteOptional.isEmpty()) {
                continue;
            }
            ImportSite site = siteOptional.get();

            Optional<Inventory> inventoryOptional = inventoryDAO.findBySiteIdAndMerchandiseId(
                    site.getId(), merchandise.getId());
            if (inventoryOptional.isEmpty() || inventoryOptional.get().getInStockQuantity() <= 0) {
                continue;
            }
            Inventory inventory = inventoryOptional.get();

            Optional<DeliveryOption> deliveryOption = deliveryOptionSelector.select(site, availableDays);
            if (deliveryOption.isEmpty()) {
                continue;
            }

            DeliveryOption option = deliveryOption.get();
            candidates.add(new AllocationCandidate(
                    site,
                    merchandise,
                    inventory,
                    option.getDeliveryMeans(),
                    inventory.getInStockQuantity(),
                    option.getDeliveryDays()));
        }

        return candidates;
    }

    private static int calculateAvailableDays(String requestDateText, String desiredDeliveryDateText) {
        LocalDate requestDate = ServiceValidation.parseDate(requestDateText, "requestDate");
        LocalDate desiredDate = ServiceValidation.parseDate(desiredDeliveryDateText, "desiredDeliveryDate");
        return (int) ChronoUnit.DAYS.between(requestDate, desiredDate);
    }

    private AllocationResult createSuccessResult(ImportRequest request, ImportRequestItem item,
                                                 AllocationCandidate candidate, int allocatedQuantity) {
        AllocationResult result = new AllocationResult();
        result.setImportRequestId(request.getId());
        result.setRequestItemId(item.getId());
        result.setSiteId(candidate.getSiteId());
        result.setMerchandiseId(item.getMerchandiseId());
        result.setAllocatedQuantity(allocatedQuantity);
        result.setUnit(item.getUnit());
        result.setDeliveryMeans(candidate.getDeliveryMeans());
        result.setStatus(AllocationStatus.SUCCESS);
        return result;
    }

    private AllocationResult createFailureResult(ImportRequest request, ImportRequestItem item,
                                                 Merchandise merchandise, int remainingQuantity) {
        AllocationResult result = new AllocationResult();
        result.setImportRequestId(request.getId());
        result.setRequestItemId(item.getId());
        result.setSiteId(null);
        result.setMerchandiseId(item.getMerchandiseId());
        result.setAllocatedQuantity(0);
        result.setUnit(item.getUnit());
        result.setDeliveryMeans(null);
        result.setStatus(AllocationStatus.FAILED);
        result.setErrorMessage(String.format(
                "Không thể đáp ứng đủ số lượng yêu cầu cho mặt hàng %s. Số lượng còn thiếu: %d",
                merchandise.getCode(),
                remainingQuantity));
        return result;
    }

    private static ImportRequestStatus determineFinalStatus(List<AllocationResult> results) {
        long successCount = results.stream().filter(r -> r.getStatus() == AllocationStatus.SUCCESS).count();
        boolean hasFailure = results.stream().anyMatch(r -> r.getStatus() == AllocationStatus.FAILED);

        if (successCount == 0) {
            return ImportRequestStatus.ALLOCATION_FAILED;
        }
        if (hasFailure) {
            return ImportRequestStatus.PARTIALLY_ALLOCATED;
        }
        return ImportRequestStatus.ALLOCATED;
    }

    private AllocationResultRow toResultRow(AllocationResult result) {
        ImportRequestItem requestItem = importRequestItemDAO.findById(result.getRequestItemId())
                .orElseThrow(() -> new ValidationException(
                        "Không tìm thấy dòng yêu cầu nhập hàng: " + result.getRequestItemId()));
        Merchandise merchandise = merchandiseDAO.findById(result.getMerchandiseId())
                .orElseThrow(() -> new ValidationException(
                        "Không tìm thấy mặt hàng: " + result.getMerchandiseId()));
        Optional<ImportSite> siteOptional = result.getSiteId() != null
                ? importSiteDAO.findById(result.getSiteId())
                : Optional.empty();

        return new AllocationResultRow(
                merchandise.getCode(),
                requestItem.getQuantityOrdered(),
                siteOptional.map(ImportSite::getSiteCode).orElse(""),
                siteOptional.map(ImportSite::getSiteName).orElse(""),
                result.getAllocatedQuantity() != null ? result.getAllocatedQuantity() : 0,
                result.getUnit() != null ? result.getUnit() : requestItem.getUnit(),
                result.getDeliveryMeans(),
                result.getStatus(),
                result.getErrorMessage() != null ? result.getErrorMessage() : "");
    }
}
