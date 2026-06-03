package com.importorder.context;

import com.importorder.controller.AllocationController;
import com.importorder.controller.DashboardController;
import com.importorder.controller.ImportRequestController;
import com.importorder.controller.ImportSiteController;
import com.importorder.controller.InventoryController;
import com.importorder.controller.MerchandiseController;
import com.importorder.controller.SiteCatalogController;
import com.importorder.controller.SiteOrderController;
import com.importorder.controller.WarehouseController;
import com.importorder.dao.DAOFactory;
import com.importorder.dao.SQLiteDAOFactory;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseInitializer;
import com.importorder.service.AllocationService;
import com.importorder.service.DashboardService;
import com.importorder.service.ImportRequestService;
import com.importorder.service.ImportSiteService;
import com.importorder.service.InventoryService;
import com.importorder.service.MerchandiseService;
import com.importorder.service.SiteCatalogService;
import com.importorder.service.SiteOrderService;
import com.importorder.service.WarehouseService;
import com.importorder.util.SeedDataLoader;

/**
 * Application-wide service registry and controller factory (GRASP: Pure Fabrication).
 */
public final class ApplicationContext {

    private static ApplicationContext instance;

    private final DAOFactory daoFactory;
    private final MerchandiseService merchandiseService;
    private final ImportSiteService importSiteService;
    private final SiteCatalogService siteCatalogService;
    private final InventoryService inventoryService;
    private final ImportRequestService importRequestService;
    private final AllocationService allocationService;
    private final SiteOrderService siteOrderService;
    private final WarehouseService warehouseService;
    private final DashboardService dashboardService;

    private ApplicationContext(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.merchandiseService = new MerchandiseService(daoFactory);
        this.importSiteService = new ImportSiteService(daoFactory);
        this.siteCatalogService = new SiteCatalogService(daoFactory);
        this.inventoryService = new InventoryService(daoFactory);
        this.importRequestService = new ImportRequestService(daoFactory);
        this.allocationService = new AllocationService(daoFactory);
        this.siteOrderService = new SiteOrderService(daoFactory);
        this.warehouseService = new WarehouseService(daoFactory);
        this.dashboardService = new DashboardService(daoFactory);
    }

    public static void initialize() {
        if (instance != null) {
            return;
        }
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory();
        new DatabaseInitializer(connectionFactory).initialize();
        DAOFactory daoFactory = new SQLiteDAOFactory(connectionFactory);
        new SeedDataLoader(daoFactory).seedIfEmpty();
        instance = new ApplicationContext(daoFactory);
    }

    public static ApplicationContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApplicationContext is not initialized");
        }
        return instance;
    }

    public Object createController(Class<?> controllerClass) {
        if (controllerClass == DashboardController.class) {
            return new DashboardController(dashboardService);
        }
        if (controllerClass == MerchandiseController.class) {
            return new MerchandiseController(merchandiseService);
        }
        if (controllerClass == ImportSiteController.class) {
            return new ImportSiteController(importSiteService);
        }
        if (controllerClass == SiteCatalogController.class) {
            return new SiteCatalogController(siteCatalogService, importSiteService, merchandiseService);
        }
        if (controllerClass == InventoryController.class) {
            return new InventoryController(inventoryService, importSiteService, merchandiseService);
        }
        if (controllerClass == ImportRequestController.class) {
            return new ImportRequestController(importRequestService);
        }
        if (controllerClass == AllocationController.class) {
            return new AllocationController(allocationService, siteOrderService);
        }
        if (controllerClass == SiteOrderController.class) {
            return new SiteOrderController(siteOrderService);
        }
        if (controllerClass == WarehouseController.class) {
            return new WarehouseController(warehouseService);
        }
        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create controller: " + controllerClass.getName(), e);
        }
    }

    public MerchandiseService getMerchandiseService() {
        return merchandiseService;
    }

    public ImportSiteService getImportSiteService() {
        return importSiteService;
    }

    public SiteCatalogService getSiteCatalogService() {
        return siteCatalogService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public ImportRequestService getImportRequestService() {
        return importRequestService;
    }

    public AllocationService getAllocationService() {
        return allocationService;
    }

    public SiteOrderService getSiteOrderService() {
        return siteOrderService;
    }

    public WarehouseService getWarehouseService() {
        return warehouseService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public DAOFactory getDaoFactory() {
        return daoFactory;
    }
}
