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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllocationServicePartialTest {

    @TempDir
    Path tempDir;

    private AllocationService allocationService;
    private ImportRequestService importRequestService;
    private SQLiteDAOFactory daoFactory;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:sqlite:" + tempDir.resolve("allocation_partial.db").toAbsolutePath();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(jdbcUrl);
        new DatabaseInitializer(connectionFactory).initialize();
        daoFactory = new SQLiteDAOFactory(connectionFactory);

        MerchandiseService merchandiseService = new MerchandiseService(daoFactory);
        ImportSiteService siteService = new ImportSiteService(daoFactory);
        SiteCatalogService catalogService = new SiteCatalogService(daoFactory);
        InventoryService inventoryService = new InventoryService(daoFactory);

        long merchandiseId = merchandiseService.create(
                new com.importorder.model.Merchandise("MP1", "Partial", "pcs", null)).getId();
        long siteId = siteService.create(
                new com.importorder.model.ImportSite("SP1", "Partial Site", 3, 2, null)).getId();
        catalogService.assignMerchandiseToSite(siteId, merchandiseId);
        inventoryService.createOrUpdateInventory(
                new com.importorder.model.Inventory(siteId, merchandiseId, 30, "pcs"));

        allocationService = new AllocationService(daoFactory);
        importRequestService = new ImportRequestService(daoFactory);
    }

    @Test
    void allocateImportRequest_partialFailure() {
        long merchandiseId = daoFactory.createMerchandiseDAO().findByCode("MP1").orElseThrow().getId();
        var request = importRequestService.createDraft();
        importRequestService.addItem(request.getId(), new ImportRequestItem(
                null, merchandiseId, 100, "pcs", LocalDate.now().plusDays(5).toString()));
        importRequestService.submitRequest(request.getId());

        var response = allocationService.allocateImportRequest(request.getId());

        assertEquals(ImportRequestStatus.PARTIALLY_ALLOCATED, response.getFinalStatus());
        assertTrue(response.hasFailure());
        assertEquals(30, response.getResults().stream()
                .filter(r -> r.getStatus() == com.importorder.model.enums.AllocationStatus.SUCCESS)
                .mapToInt(r -> r.getAllocatedQuantity())
                .sum());
    }
}
