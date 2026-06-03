package com.importorder.service;

import com.importorder.dao.SQLiteDAOFactory;
import com.importorder.database.DatabaseConnectionFactory;
import com.importorder.database.DatabaseInitializer;
import com.importorder.model.ImportRequestItem;
import com.importorder.model.Merchandise;
import com.importorder.model.enums.ImportRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportRequestServiceTest {

    @TempDir
    Path tempDir;

    private ImportRequestService importRequestService;
    private long merchandiseId;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:sqlite:" + tempDir.resolve("request_service.db").toAbsolutePath();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(jdbcUrl);
        new DatabaseInitializer(connectionFactory).initialize();
        var daoFactory = new SQLiteDAOFactory(connectionFactory);
        importRequestService = new ImportRequestService(daoFactory);
        MerchandiseService merchandiseService = new MerchandiseService(daoFactory);
        merchandiseId = merchandiseService.create(new Merchandise("M100", "Test", "pcs", null)).getId();
    }

    @Test
    void submitRequest_changesDraftToSubmitted() {
        var request = importRequestService.createDraft();
        importRequestService.addItem(request.getId(), new ImportRequestItem(
                null,
                merchandiseId,
                10,
                "pcs",
                LocalDate.now().plusDays(5).toString()));

        var submitted = importRequestService.submitRequest(request.getId());

        assertEquals(ImportRequestStatus.SUBMITTED, submitted.getStatus());
        assertEquals(ImportRequestStatus.SUBMITTED,
                importRequestService.findById(request.getId()).orElseThrow().getStatus());
    }

    @Test
    void addItem_rejectsDeliveryDateBeforeRequestDate() {
        var request = importRequestService.createDraft();

        assertThrows(ValidationException.class, () -> importRequestService.addItem(
                request.getId(),
                new ImportRequestItem(null, merchandiseId, 5, "pcs", LocalDate.now().minusDays(1).toString())));
    }

    @Test
    void submitRequest_rejectsNonDraft() {
        var request = importRequestService.createDraft();
        importRequestService.addItem(request.getId(), new ImportRequestItem(
                null, merchandiseId, 1, "pcs", LocalDate.now().toString()));
        importRequestService.submitRequest(request.getId());

        assertThrows(ValidationException.class, () -> importRequestService.submitRequest(request.getId()));
    }
}
