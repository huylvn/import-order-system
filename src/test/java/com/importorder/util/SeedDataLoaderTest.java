package com.importorder.util;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.SQLiteDAOFactory;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseInitializer;
import com.importorder.model.enums.ImportRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedDataLoaderTest {

    @TempDir
    Path tempDir;

    private DAOFactory daoFactory;
    private SeedDataLoader seedDataLoader;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:sqlite:" + tempDir.resolve("seed_test.db").toAbsolutePath();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(jdbcUrl);
        new DatabaseInitializer(connectionFactory).initialize();
        daoFactory = new SQLiteDAOFactory(connectionFactory);
        seedDataLoader = new SeedDataLoader(daoFactory);
    }

    @Test
    void seedIfEmpty_loadsDemoDataOnce() {
        seedDataLoader.seedIfEmpty();

        assertEquals(4, daoFactory.createMerchandiseDAO().findAll().size());
        assertEquals(4, daoFactory.createImportSiteDAO().findAll().size());
        assertEquals(8, daoFactory.createSiteMerchandiseDAO().findAll().size());
        assertEquals(8, daoFactory.createInventoryDAO().findAll().size());
        assertEquals(1, daoFactory.createImportRequestDAO().findAll().size());
        assertEquals(3, daoFactory.createImportRequestItemDAO().findAll().size());

        var request = daoFactory.createImportRequestDAO().findAll().get(0);
        assertEquals("REQ-DEMO-001", request.getRequestCode());
        assertEquals(ImportRequestStatus.SUBMITTED, request.getStatus());

        seedDataLoader.seedIfEmpty();

        assertEquals(4, daoFactory.createMerchandiseDAO().findAll().size());
        assertEquals(1, daoFactory.createImportRequestDAO().findAll().size());
        assertTrue(daoFactory.createImportRequestItemDAO().findByRequestId(request.getId()).size() >= 3);
    }
}
