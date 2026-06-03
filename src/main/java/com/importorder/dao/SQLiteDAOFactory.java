package com.importorder.dao;

import com.importorder.dao.impl.SQLiteAllocationResultDAO;
import com.importorder.dao.impl.SQLiteImportRequestDAO;
import com.importorder.dao.impl.SQLiteImportRequestItemDAO;
import com.importorder.dao.impl.SQLiteImportSiteDAO;
import com.importorder.dao.impl.SQLiteInventoryDAO;
import com.importorder.dao.impl.SQLiteMerchandiseDAO;
import com.importorder.dao.impl.SQLiteReceivedGoodsDAO;
import com.importorder.dao.impl.SQLiteSiteMerchandiseDAO;
import com.importorder.dao.impl.SQLiteSiteOrderDAO;
import com.importorder.dao.impl.SQLiteSiteOrderItemDAO;
import com.importorder.database.DatabaseConnectionFactory;

import java.util.Objects;

/**
 * Concrete factory that produces SQLite-backed DAO instances.
 */
public class SQLiteDAOFactory extends DAOFactory {

    private final DatabaseConnectionFactory connectionFactory;

    public SQLiteDAOFactory(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory must not be null");
    }

    @Override
    public DatabaseConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    @Override
    public MerchandiseDAO createMerchandiseDAO() {
        return new SQLiteMerchandiseDAO(connectionFactory);
    }

    @Override
    public ImportSiteDAO createImportSiteDAO() {
        return new SQLiteImportSiteDAO(connectionFactory);
    }

    @Override
    public SiteMerchandiseDAO createSiteMerchandiseDAO() {
        return new SQLiteSiteMerchandiseDAO(connectionFactory);
    }

    @Override
    public InventoryDAO createInventoryDAO() {
        return new SQLiteInventoryDAO(connectionFactory);
    }

    @Override
    public ImportRequestDAO createImportRequestDAO() {
        return new SQLiteImportRequestDAO(connectionFactory);
    }

    @Override
    public ImportRequestItemDAO createImportRequestItemDAO() {
        return new SQLiteImportRequestItemDAO(connectionFactory);
    }

    @Override
    public AllocationResultDAO createAllocationResultDAO() {
        return new SQLiteAllocationResultDAO(connectionFactory);
    }

    @Override
    public SiteOrderDAO createSiteOrderDAO() {
        return new SQLiteSiteOrderDAO(connectionFactory);
    }

    @Override
    public SiteOrderItemDAO createSiteOrderItemDAO() {
        return new SQLiteSiteOrderItemDAO(connectionFactory);
    }

    @Override
    public ReceivedGoodsDAO createReceivedGoodsDAO() {
        return new SQLiteReceivedGoodsDAO(connectionFactory);
    }
}
