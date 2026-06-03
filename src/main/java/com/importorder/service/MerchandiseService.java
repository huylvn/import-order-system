package com.importorder.service;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.model.Merchandise;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Use cases for merchandise master data.
 */
public class MerchandiseService {

    private final MerchandiseDAO merchandiseDAO;

    public MerchandiseService(DAOFactory daoFactory) {
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
    }

    public List<Merchandise> findAll() {
        return merchandiseDAO.findAll();
    }

    public Optional<Merchandise> findById(long id) {
        return merchandiseDAO.findById(id);
    }

    public Merchandise create(Merchandise merchandise) {
        Objects.requireNonNull(merchandise, "merchandise must not be null");
        validateMerchandise(merchandise);
        ensureCodeAvailable(merchandise.getCode(), null);
        return merchandiseDAO.save(merchandise);
    }

    public void update(Merchandise merchandise) {
        Objects.requireNonNull(merchandise, "merchandise must not be null");
        if (merchandise.getId() == null) {
            throw new ValidationException("Cần có ID mặt hàng để cập nhật");
        }
        validateMerchandise(merchandise);
        ensureCodeAvailable(merchandise.getCode(), merchandise.getId());
        merchandiseDAO.update(merchandise);
    }

    public void delete(long id) {
        merchandiseDAO.deleteById(id);
    }

    public void validateMerchandise(Merchandise merchandise) {
        ServiceValidation.requireNonBlank(merchandise.getCode(), "code");
        ServiceValidation.requireNonBlank(merchandise.getName(), "name");
        ServiceValidation.requireNonBlank(merchandise.getUnit(), "unit");
    }

    private void ensureCodeAvailable(String code, Long excludeId) {
        Optional<Merchandise> existing = merchandiseDAO.findByCode(code.trim());
        if (existing.isPresent() && (excludeId == null || !excludeId.equals(existing.get().getId()))) {
            throw new ValidationException("Mã mặt hàng đã tồn tại: " + code);
        }
    }
}
