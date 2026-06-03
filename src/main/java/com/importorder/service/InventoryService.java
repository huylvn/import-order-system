package com.importorder.service;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.dao.InventoryDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.model.Inventory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Use cases for site inventory levels.
 */
public class InventoryService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final InventoryDAO inventoryDAO;
    private final ImportSiteDAO importSiteDAO;
    private final MerchandiseDAO merchandiseDAO;

    public InventoryService(DAOFactory daoFactory) {
        this.inventoryDAO = daoFactory.createInventoryDAO();
        this.importSiteDAO = daoFactory.createImportSiteDAO();
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
    }

    public List<Inventory> findAll() {
        return inventoryDAO.findAll();
    }

    public List<Inventory> findByMerchandiseId(long merchandiseId) {
        return inventoryDAO.findByMerchandiseId(merchandiseId);
    }

    public Inventory createOrUpdateInventory(Inventory inventory) {
        Objects.requireNonNull(inventory, "inventory must not be null");
        validateInventory(inventory);
        ensureSiteExists(inventory.getSiteId());
        ensureMerchandiseExists(inventory.getMerchandiseId());

        Optional<Inventory> existing = inventoryDAO.findBySiteIdAndMerchandiseId(
                inventory.getSiteId(), inventory.getMerchandiseId());

        if (existing.isPresent()) {
            Inventory current = existing.get();
            current.setInStockQuantity(inventory.getInStockQuantity());
            current.setUnit(inventory.getUnit());
            current.setLastUpdatedAt(currentTimestamp());
            inventoryDAO.update(current);
            return current;
        }

        inventory.setLastUpdatedAt(currentTimestamp());
        return inventoryDAO.save(inventory);
    }

    public void decreaseStock(long siteId, long merchandiseId, int quantity) {
        ServiceValidation.requirePositive(quantity, "quantity");
        inventoryDAO.decreaseStock(siteId, merchandiseId, quantity);
    }

    public void validateInventory(Inventory inventory) {
        if (inventory.getSiteId() == null) {
            throw new ValidationException("Site là bắt buộc");
        }
        if (inventory.getMerchandiseId() == null) {
            throw new ValidationException("Mặt hàng là bắt buộc");
        }
        ServiceValidation.requireNonBlank(inventory.getUnit(), "unit");
        ServiceValidation.requireNonNegative(inventory.getInStockQuantity(), "inStockQuantity");
    }

    private void ensureSiteExists(long siteId) {
        if (importSiteDAO.findById(siteId).isEmpty()) {
            throw new ValidationException("Không tìm thấy site nhập khẩu: " + siteId);
        }
    }

    private void ensureMerchandiseExists(long merchandiseId) {
        if (merchandiseDAO.findById(merchandiseId).isEmpty()) {
            throw new ValidationException("Không tìm thấy mặt hàng: " + merchandiseId);
        }
    }

    private static String currentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
}
