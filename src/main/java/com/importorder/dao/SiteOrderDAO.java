package com.importorder.dao;

import com.importorder.model.SiteOrder;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface SiteOrderDAO {

    List<SiteOrder> findAll();

    Optional<SiteOrder> findById(long id);

    List<SiteOrder> findByImportRequestId(long importRequestId);

    SiteOrder save(SiteOrder siteOrder);

    SiteOrder save(Connection connection, SiteOrder siteOrder);

    void update(SiteOrder siteOrder);

    void deleteById(long id);
}
