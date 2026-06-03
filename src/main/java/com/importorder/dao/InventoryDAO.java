package com.importorder.dao;

import com.importorder.model.Inventory;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface InventoryDAO {

    List<Inventory> findAll();

    Optional<Inventory> findById(long id);

    Optional<Inventory> findBySiteIdAndMerchandiseId(long siteId, long merchandiseId);

    List<Inventory> findByMerchandiseId(long merchandiseId);

    Inventory save(Inventory inventory);

    void update(Inventory inventory);

    void deleteById(long id);

    void decreaseStock(long siteId, long merchandiseId, int quantity);

    void decreaseStock(Connection connection, long siteId, long merchandiseId, int quantity);
}
