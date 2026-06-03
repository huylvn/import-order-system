package com.importorder.dao;

import com.importorder.database.DatabaseConnectionFactory;

/**
 * Abstract factory for creating DAO implementations (Factory Method pattern).
 */
public abstract class DAOFactory {

    public abstract DatabaseConnectionFactory getConnectionFactory();

    public abstract MerchandiseDAO createMerchandiseDAO();

    public abstract ImportSiteDAO createImportSiteDAO();

    public abstract SiteMerchandiseDAO createSiteMerchandiseDAO();

    public abstract InventoryDAO createInventoryDAO();

    public abstract ImportRequestDAO createImportRequestDAO();

    public abstract ImportRequestItemDAO createImportRequestItemDAO();

    public abstract AllocationResultDAO createAllocationResultDAO();

    public abstract SiteOrderDAO createSiteOrderDAO();

    public abstract SiteOrderItemDAO createSiteOrderItemDAO();

    public abstract ReceivedGoodsDAO createReceivedGoodsDAO();
}
