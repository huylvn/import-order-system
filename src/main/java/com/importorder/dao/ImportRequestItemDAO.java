package com.importorder.dao;

import com.importorder.model.ImportRequestItem;

import java.util.List;
import java.util.Optional;

public interface ImportRequestItemDAO {

    List<ImportRequestItem> findAll();

    Optional<ImportRequestItem> findById(long id);

    List<ImportRequestItem> findByRequestId(long requestId);

    ImportRequestItem save(ImportRequestItem item);

    void update(ImportRequestItem item);

    void deleteById(long id);
}
