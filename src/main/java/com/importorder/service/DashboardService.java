package com.importorder.service;

import com.importorder.dao.AllocationResultDAO;
import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportRequestDAO;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.dao.InventoryDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.dao.SiteOrderDAO;
import com.importorder.model.ImportRequest;
import com.importorder.model.Inventory;
import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.service.dashboard.DashboardSummary;
import com.importorder.service.dashboard.InventoryShortageAlert;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only aggregates for the dashboard view.
 */
public class DashboardService {

    public static final int LOW_STOCK_THRESHOLD = 20;

    private final ImportRequestDAO importRequestDAO;
    private final SiteOrderDAO siteOrderDAO;
    private final InventoryDAO inventoryDAO;
    private final ImportSiteDAO importSiteDAO;
    private final MerchandiseDAO merchandiseDAO;
    private final AllocationResultDAO allocationResultDAO;

    public DashboardService(DAOFactory daoFactory) {
        this.importRequestDAO = daoFactory.createImportRequestDAO();
        this.siteOrderDAO = daoFactory.createSiteOrderDAO();
        this.inventoryDAO = daoFactory.createInventoryDAO();
        this.importSiteDAO = daoFactory.createImportSiteDAO();
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
        this.allocationResultDAO = daoFactory.createAllocationResultDAO();
    }

    public DashboardSummary getSummary() {
        List<ImportRequest> requests = importRequestDAO.findAll();

        int total = requests.size();
        int submitted = countByStatus(requests, ImportRequestStatus.SUBMITTED);
        int allocated = countByStatus(requests, ImportRequestStatus.ALLOCATED);
        int failed = countByStatus(requests, ImportRequestStatus.ALLOCATION_FAILED);
        int siteOrders = siteOrderDAO.findAll().size();
        List<InventoryShortageAlert> shortages = findLowStockAlerts();

        return new DashboardSummary(total, submitted, allocated, failed, siteOrders, shortages);
    }

    public int countFailedAllocationLines() {
        return (int) allocationResultDAO.findAll().stream()
                .filter(r -> r.getStatus() == AllocationStatus.FAILED)
                .count();
    }

    private static int countByStatus(List<ImportRequest> requests, ImportRequestStatus status) {
        return (int) requests.stream().filter(r -> r.getStatus() == status).count();
    }

    private List<InventoryShortageAlert> findLowStockAlerts() {
        List<InventoryShortageAlert> alerts = new ArrayList<>();
        for (Inventory inventory : inventoryDAO.findAll()) {
            if (inventory.getInStockQuantity() > LOW_STOCK_THRESHOLD) {
                continue;
            }
            String siteCode = importSiteDAO.findById(inventory.getSiteId())
                    .map(s -> s.getSiteCode())
                    .orElse("?");
            String merchandiseCode = merchandiseDAO.findById(inventory.getMerchandiseId())
                    .map(m -> m.getCode())
                    .orElse("?");
            alerts.add(new InventoryShortageAlert(
                    siteCode,
                    merchandiseCode,
                    inventory.getInStockQuantity(),
                    inventory.getUnit()));
        }
        return alerts;
    }
}
