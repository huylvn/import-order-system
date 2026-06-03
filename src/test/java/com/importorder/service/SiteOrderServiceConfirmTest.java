package com.importorder.service;

import com.importorder.dao.SQLiteDAOFactory;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseInitializer;
import com.importorder.model.ImportRequestItem;
import com.importorder.model.enums.ImportRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SiteOrderServiceConfirmTest {

    @TempDir
    Path tempDir;

    private SQLiteDAOFactory daoFactory;
    private AllocationService allocationService;
    private SiteOrderService siteOrderService;
    private long siteIdA;
    private long siteIdB;
    private long merchandiseId;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:sqlite:" + tempDir.resolve("confirm_orders.db").toAbsolutePath();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(jdbcUrl);
        new DatabaseInitializer(connectionFactory).initialize();
        daoFactory = new SQLiteDAOFactory(connectionFactory);

        MerchandiseService merchandiseService = new MerchandiseService(daoFactory);
        ImportSiteService siteService = new ImportSiteService(daoFactory);
        SiteCatalogService catalogService = new SiteCatalogService(daoFactory);
        InventoryService inventoryService = new InventoryService(daoFactory);
        ImportRequestService importRequestService = new ImportRequestService(daoFactory);

        merchandiseId = merchandiseService.create(
                new com.importorder.model.Merchandise("MC1", "Confirm Test", "pcs", null)).getId();
        siteIdA = siteService.create(
                new com.importorder.model.ImportSite("SCA", "Site A", 3, 2, null)).getId();
        siteIdB = siteService.create(
                new com.importorder.model.ImportSite("SCB", "Site B", 3, 2, null)).getId();

        catalogService.assignMerchandiseToSite(siteIdA, merchandiseId);
        catalogService.assignMerchandiseToSite(siteIdB, merchandiseId);
        inventoryService.createOrUpdateInventory(
                new com.importorder.model.Inventory(siteIdA, merchandiseId, 40, "pcs"));
        inventoryService.createOrUpdateInventory(
                new com.importorder.model.Inventory(siteIdB, merchandiseId, 40, "pcs"));

        var request = importRequestService.createDraft();
        importRequestService.addItem(request.getId(), new ImportRequestItem(
                null, merchandiseId, 70, "pcs", LocalDate.now().plusDays(10).toString()));
        importRequestService.submitRequest(request.getId());

        allocationService = new AllocationService(daoFactory);
        allocationService.allocateImportRequest(request.getId());

        siteOrderService = new SiteOrderService(daoFactory);
        this.requestId = request.getId();
    }

    private long requestId;

    @Test
    void confirmOrders_createsOneSiteOrderPerSite() {
        long distinctSites = daoFactory.createAllocationResultDAO()
                .findSuccessfulByRequestId(requestId).stream()
                .map(r -> r.getSiteId())
                .distinct()
                .count();

        siteOrderService.confirmOrders(requestId);

        assertEquals(distinctSites, siteOrderService.findByImportRequestId(requestId).size());
        assertEquals(ImportRequestStatus.ORDER_SENT,
                daoFactory.createImportRequestDAO().findById(requestId).orElseThrow().getStatus());
    }

    @Test
    void confirmOrders_decreasesInventoryByAllocatedQuantity() {
        int allocatedFromA = daoFactory.createAllocationResultDAO()
                .findSuccessfulByRequestId(requestId).stream()
                .filter(r -> Long.valueOf(siteIdA).equals(r.getSiteId()))
                .mapToInt(r -> r.getAllocatedQuantity())
                .sum();
        int allocatedFromB = daoFactory.createAllocationResultDAO()
                .findSuccessfulByRequestId(requestId).stream()
                .filter(r -> Long.valueOf(siteIdB).equals(r.getSiteId()))
                .mapToInt(r -> r.getAllocatedQuantity())
                .sum();

        siteOrderService.confirmOrders(requestId);

        int stockA = daoFactory.createInventoryDAO()
                .findBySiteIdAndMerchandiseId(siteIdA, merchandiseId).orElseThrow().getInStockQuantity();
        int stockB = daoFactory.createInventoryDAO()
                .findBySiteIdAndMerchandiseId(siteIdB, merchandiseId).orElseThrow().getInStockQuantity();

        assertEquals(40 - allocatedFromA, stockA);
        assertEquals(40 - allocatedFromB, stockB);
        assertEquals(70, allocatedFromA + allocatedFromB);
    }

    @Test
    void confirmOrders_rejectsAlreadySent() {
        siteOrderService.confirmOrders(requestId);
        assertThrows(ValidationException.class, () -> siteOrderService.confirmOrders(requestId));
    }
}
