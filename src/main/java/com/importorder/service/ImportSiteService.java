package com.importorder.service;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.model.ImportSite;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Use cases for overseas import sites.
 */
public class ImportSiteService {

    private final ImportSiteDAO importSiteDAO;

    public ImportSiteService(DAOFactory daoFactory) {
        this.importSiteDAO = daoFactory.createImportSiteDAO();
    }

    public List<ImportSite> findAll() {
        return importSiteDAO.findAll();
    }

    public Optional<ImportSite> findById(long id) {
        return importSiteDAO.findById(id);
    }

    public ImportSite create(ImportSite importSite) {
        Objects.requireNonNull(importSite, "importSite must not be null");
        validateImportSite(importSite);
        ensureCodeAvailable(importSite.getSiteCode(), null);
        return importSiteDAO.save(importSite);
    }

    public void update(ImportSite importSite) {
        Objects.requireNonNull(importSite, "importSite must not be null");
        if (importSite.getId() == null) {
            throw new ValidationException("Cần có ID site để cập nhật");
        }
        validateImportSite(importSite);
        ensureCodeAvailable(importSite.getSiteCode(), importSite.getId());
        importSiteDAO.update(importSite);
    }

    public void delete(long id) {
        importSiteDAO.deleteById(id);
    }

    public void validateImportSite(ImportSite importSite) {
        ServiceValidation.requireNonBlank(importSite.getSiteCode(), "siteCode");
        ServiceValidation.requireNonBlank(importSite.getSiteName(), "siteName");
        ServiceValidation.requireNonNegative(importSite.getShipDeliveryDays(), "shipDeliveryDays");
        ServiceValidation.requireNonNegative(importSite.getAirDeliveryDays(), "airDeliveryDays");
        if (importSite.getShipDeliveryDays() == 0 && importSite.getAirDeliveryDays() == 0) {
            throw new ValidationException("Ít nhất một số ngày vận chuyển phải lớn hơn 0");
        }
    }

    private void ensureCodeAvailable(String siteCode, Long excludeId) {
        Optional<ImportSite> existing = importSiteDAO.findByCode(siteCode.trim());
        if (existing.isPresent() && (excludeId == null || !excludeId.equals(existing.get().getId()))) {
            throw new ValidationException("Mã site đã tồn tại: " + siteCode);
        }
    }
}
