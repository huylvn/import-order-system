package com.importorder.dao;

import com.importorder.model.Merchandise;

import java.util.List;
import java.util.Optional;

public interface MerchandiseDAO {

    List<Merchandise> findAll();

    Optional<Merchandise> findById(long id);

    Optional<Merchandise> findByCode(String code);

    Merchandise save(Merchandise merchandise);

    void update(Merchandise merchandise);

    void deleteById(long id);
}
