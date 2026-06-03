package com.importorder.dao;

import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseInitializer;
import com.importorder.model.Merchandise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchandiseDAOTest {

    @TempDir
    Path tempDir;

    private MerchandiseDAO merchandiseDAO;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:sqlite:" + tempDir.resolve("dao_test.db").toAbsolutePath();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(jdbcUrl);
        new DatabaseInitializer(connectionFactory).initialize();
        merchandiseDAO = new SQLiteDAOFactory(connectionFactory).createMerchandiseDAO();
    }

    @Test
    void saveFindUpdateDelete_roundTrip() {
        Merchandise saved = merchandiseDAO.save(
                new Merchandise("M001", "Widget", "pcs", "Test item"));

        Optional<Merchandise> byId = merchandiseDAO.findById(saved.getId());
        assertTrue(byId.isPresent());
        assertEquals("M001", byId.get().getCode());

        Optional<Merchandise> byCode = merchandiseDAO.findByCode("M001");
        assertTrue(byCode.isPresent());

        saved.setName("Widget Pro");
        merchandiseDAO.update(saved);

        assertEquals("Widget Pro", merchandiseDAO.findById(saved.getId()).orElseThrow().getName());
        assertEquals(1, merchandiseDAO.findAll().size());

        merchandiseDAO.deleteById(saved.getId());
        assertTrue(merchandiseDAO.findById(saved.getId()).isEmpty());
    }
}
