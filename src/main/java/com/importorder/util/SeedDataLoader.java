package com.importorder.util;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportRequestDAO;
import com.importorder.dao.ImportRequestItemDAO;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.dao.InventoryDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.dao.SiteMerchandiseDAO;
import com.importorder.model.ImportRequest;
import com.importorder.model.ImportRequestItem;
import com.importorder.model.ImportSite;
import com.importorder.model.Inventory;
import com.importorder.model.Merchandise;
import com.importorder.model.SiteMerchandise;
import com.importorder.model.enums.ImportRequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads demo data on first application run when the database is empty.
 */
public class SeedDataLoader {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String DEMO_REQUEST_CODE = "REQ-DEMO-001";

    private final DAOFactory daoFactory;

    public SeedDataLoader(DAOFactory daoFactory) {
        this.daoFactory = Objects.requireNonNull(daoFactory, "daoFactory must not be null");
    }

    /**
     * Seeds demo data only when the merchandise table has no rows.
     */
    public void seedIfEmpty() {
        MerchandiseDAO merchandiseDAO = daoFactory.createMerchandiseDAO();
        if (!merchandiseDAO.findAll().isEmpty()) {
            return;
        }
        seedAll();
    }

    private void seedAll() {
        Map<String, Long> merchandiseIds = seedMerchandise();
        Map<String, Long> siteIds = seedImportSites();
        seedSiteCatalog(siteIds, merchandiseIds);
        seedInventory(siteIds, merchandiseIds);
        seedDemoImportRequest(merchandiseIds);
    }

    private Map<String, Long> seedMerchandise() {
        MerchandiseDAO dao = daoFactory.createMerchandiseDAO();
        Map<String, Long> ids = new HashMap<>();

        ids.put("M001", dao.save(new Merchandise("M001", "Laptop", "pcs", null)).getId());
        ids.put("M002", dao.save(new Merchandise("M002", "Monitor", "pcs", null)).getId());
        ids.put("M003", dao.save(new Merchandise("M003", "Keyboard", "pcs", null)).getId());
        ids.put("M004", dao.save(new Merchandise("M004", "Mouse", "pcs", null)).getId());

        return ids;
    }

    private Map<String, Long> seedImportSites() {
        ImportSiteDAO dao = daoFactory.createImportSiteDAO();
        Map<String, Long> ids = new HashMap<>();

        ids.put("S001", dao.save(new ImportSite("S001", "Tokyo Import Site", 14, 5, null)).getId());
        ids.put("S002", dao.save(new ImportSite("S002", "Singapore Import Site", 10, 3, null)).getId());
        ids.put("S003", dao.save(new ImportSite("S003", "Germany Import Site", 25, 7, null)).getId());
        ids.put("S004", dao.save(new ImportSite("S004", "US Import Site", 30, 8, null)).getId());

        return ids;
    }

    private void seedSiteCatalog(Map<String, Long> siteIds, Map<String, Long> merchandiseIds) {
        SiteMerchandiseDAO dao = daoFactory.createSiteMerchandiseDAO();

        linkCatalog(dao, siteIds, merchandiseIds, "S001", List.of("M001", "M002"));
        linkCatalog(dao, siteIds, merchandiseIds, "S002", List.of("M001", "M003"));
        linkCatalog(dao, siteIds, merchandiseIds, "S003", List.of("M001", "M004"));
        linkCatalog(dao, siteIds, merchandiseIds, "S004", List.of("M002", "M003"));
    }

    private static void linkCatalog(SiteMerchandiseDAO dao, Map<String, Long> siteIds,
                                    Map<String, Long> merchandiseIds, String siteCode,
                                    List<String> merchandiseCodes) {
        long siteId = siteIds.get(siteCode);
        for (String merchandiseCode : merchandiseCodes) {
            dao.save(new SiteMerchandise(siteId, merchandiseIds.get(merchandiseCode)));
        }
    }

    private void seedInventory(Map<String, Long> siteIds, Map<String, Long> merchandiseIds) {
        InventoryDAO dao = daoFactory.createInventoryDAO();

        saveInventory(dao, siteIds, merchandiseIds, "S001", "M001", 80, "pcs");
        saveInventory(dao, siteIds, merchandiseIds, "S001", "M002", 20, "pcs");
        saveInventory(dao, siteIds, merchandiseIds, "S002", "M001", 50, "pcs");
        saveInventory(dao, siteIds, merchandiseIds, "S002", "M003", 100, "pcs");
        saveInventory(dao, siteIds, merchandiseIds, "S003", "M001", 200, "pcs");
        saveInventory(dao, siteIds, merchandiseIds, "S003", "M004", 300, "pcs");
        saveInventory(dao, siteIds, merchandiseIds, "S004", "M002", 150, "pcs");
        saveInventory(dao, siteIds, merchandiseIds, "S004", "M003", 20, "pcs");
    }

    private static void saveInventory(InventoryDAO dao, Map<String, Long> siteIds,
                                      Map<String, Long> merchandiseIds, String siteCode,
                                      String merchandiseCode, int quantity, String unit) {
        Inventory inventory = new Inventory(
                siteIds.get(siteCode),
                merchandiseIds.get(merchandiseCode),
                quantity,
                unit);
        inventory.setLastUpdatedAt(LocalDateTime.now().format(TIMESTAMP_FORMAT));
        dao.save(inventory);
    }

    private void seedDemoImportRequest(Map<String, Long> merchandiseIds) {
        ImportRequestDAO requestDAO = daoFactory.createImportRequestDAO();
        ImportRequestItemDAO itemDAO = daoFactory.createImportRequestItemDAO();

        LocalDate requestDate = LocalDate.now();
        String requestDateText = requestDate.toString();

        ImportRequest request = new ImportRequest(
                DEMO_REQUEST_CODE,
                requestDateText,
                ImportRequestStatus.SUBMITTED);
        request.setCreatedAt(LocalDateTime.now().format(TIMESTAMP_FORMAT));
        requestDAO.save(request);

        itemDAO.save(new ImportRequestItem(
                request.getId(),
                merchandiseIds.get("M001"),
                100,
                "pcs",
                requestDate.plusDays(15).toString()));

        itemDAO.save(new ImportRequestItem(
                request.getId(),
                merchandiseIds.get("M002"),
                100,
                "pcs",
                requestDate.plusDays(12).toString()));

        itemDAO.save(new ImportRequestItem(
                request.getId(),
                merchandiseIds.get("M003"),
                80,
                "pcs",
                requestDate.plusDays(4).toString()));
    }
}
