package com.importorder.dao;

import com.importorder.model.ImportRequest;
import com.importorder.model.enums.ImportRequestStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ImportRequestDAO {

    List<ImportRequest> findAll();

    Optional<ImportRequest> findById(long id);

    ImportRequest save(ImportRequest importRequest);

    void update(ImportRequest importRequest);

    void deleteById(long id);

    void updateStatus(long requestId, ImportRequestStatus status);

    void updateStatus(Connection connection, long requestId, ImportRequestStatus status);
}
