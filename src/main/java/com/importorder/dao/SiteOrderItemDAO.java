package com.importorder.dao;

import com.importorder.model.SiteOrderItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface SiteOrderItemDAO {

    List<SiteOrderItem> findAll();

    Optional<SiteOrderItem> findById(long id);

    List<SiteOrderItem> findBySiteOrderId(long siteOrderId);

    SiteOrderItem save(SiteOrderItem siteOrderItem);

    SiteOrderItem save(Connection connection, SiteOrderItem siteOrderItem);

    void update(SiteOrderItem siteOrderItem);

    void deleteById(long id);
}
