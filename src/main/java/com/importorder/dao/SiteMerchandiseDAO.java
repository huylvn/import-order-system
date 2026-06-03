package com.importorder.dao;

import com.importorder.model.SiteMerchandise;

import java.util.List;
import java.util.Optional;

public interface SiteMerchandiseDAO {

    List<SiteMerchandise> findAll();

    Optional<SiteMerchandise> findById(long id);

    Optional<SiteMerchandise> findBySiteIdAndMerchandiseId(long siteId, long merchandiseId);

    List<SiteMerchandise> findBySiteId(long siteId);

    List<SiteMerchandise> findByMerchandiseId(long merchandiseId);

    SiteMerchandise save(SiteMerchandise siteMerchandise);

    void update(SiteMerchandise siteMerchandise);

    void deleteById(long id);
}
