package com.importorder.service;

import com.importorder.dao.SQLiteDAOFactory;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseInitializer;
import com.importorder.model.ImportRequest;
import com.importorder.model.ImportRequestItem;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.service.allocation.AllocationResponse;
import com.importorder.util.SeedDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllocationServiceTest {

    @TempDir
    Path tempDir;

    private AllocationService allocationService;
    private ImportRequestService importRequestService;
    private SQLiteDAOFactory daoFactory;
    private long demoRequestId;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:sqlite:" + tempDir.resolve("allocation_test.db").toAbsolutePath();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(jdbcUrl);
        new DatabaseInitializer(connectionFactory).initialize();
        daoFactory = new SQLiteDAOFactory(connectionFactory);
        new SeedDataLoader(daoFactory).seedIfEmpty();

        allocationService = new AllocationService(daoFactory);
        importRequestService = new ImportRequestService(daoFactory);

        demoRequestId = importRequestService.findAll().stream()
                .filter(r -> "REQ-DEMO-001".equals(r.getRequestCode()))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @Test
    void allocateImportRequest_demoData_allocatesM001Fully() {
        AllocationResponse response = allocationService.allocateImportRequest(demoRequestId);

        assertEquals(ImportRequestStatus.ALLOCATED, response.getFinalStatus());
        assertTrue(response.totalSuccessCount() > 0);
        assertEquals(0, response.totalFailureCount());

        int m001Allocated = response.getResults().stream()
                .filter(r -> r.getMerchandiseId() == merchandiseId("M001"))
                .filter(r -> r.getStatus() == com.importorder.model.enums.AllocationStatus.SUCCESS)
                .mapToInt(r -> r.getAllocatedQuantity())
                .sum();
        assertEquals(100, m001Allocated);
    }

    @Test
    void allocateImportRequest_rejectsNonSubmitted() {
        ImportRequest draft = importRequestService.createDraft();
        assertThrows(ValidationException.class,
                () -> allocationService.allocateImportRequest(draft.getId()));
    }

    @Test
    void allocateImportRequest_canRunAgain() {
        allocationService.allocateImportRequest(demoRequestId);
        AllocationResponse secondRun = allocationService.allocateImportRequest(demoRequestId);

        assertEquals(ImportRequestStatus.ALLOCATED, secondRun.getFinalStatus());
        assertEquals(secondRun.getResults().size(),
                daoFactory.createAllocationResultDAO().findByRequestId(demoRequestId).size());
    }

    @Test
    void allocateImportRequest_simpleFullAllocation() {
        var merchandiseService = new MerchandiseService(daoFactory);
        var siteService = new ImportSiteService(daoFactory);
        var catalogService = new SiteCatalogService(daoFactory);
        var inventoryService = new InventoryService(daoFactory);

        long merchandiseId = merchandiseService.create(new com.importorder.model.Merchandise("MX1", "X", "pcs", null)).getId();
        long siteId = siteService.create(new com.importorder.model.ImportSite("SX1", "Site X", 5, 2, null)).getId();
        catalogService.assignMerchandiseToSite(siteId, merchandiseId);
        inventoryService.createOrUpdateInventory(
                new com.importorder.model.Inventory(siteId, merchandiseId, 200, "pcs"));

        ImportRequest request = importRequestService.createDraft();
        importRequestService.addItem(request.getId(), new ImportRequestItem(
                null, merchandiseId, 50, "pcs", LocalDate.now().plusDays(10).toString()));
        importRequestService.submitRequest(request.getId());

        AllocationResponse response = allocationService.allocateImportRequest(request.getId());

        assertEquals(ImportRequestStatus.ALLOCATED, response.getFinalStatus());
        assertEquals(0, response.totalFailureCount());
        assertEquals(50, response.getResults().get(0).getAllocatedQuantity());
    }

    private long merchandiseId(String code) {
        return daoFactory.createMerchandiseDAO().findByCode(code).orElseThrow().getId();
    }
}
