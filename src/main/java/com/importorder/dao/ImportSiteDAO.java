package com.importorder.dao;

import com.importorder.model.ImportSite;

import java.util.List;
import java.util.Optional;

public interface ImportSiteDAO {

    List<ImportSite> findAll();

    Optional<ImportSite> findById(long id);

    Optional<ImportSite> findByCode(String siteCode);

    ImportSite save(ImportSite importSite);

    void update(ImportSite importSite);

    void deleteById(long id);
}
