package com.importorder.dao;

import com.importorder.model.AllocationResult;

import java.util.List;
import java.util.Optional;

public interface AllocationResultDAO {

    List<AllocationResult> findAll();

    Optional<AllocationResult> findById(long id);

    List<AllocationResult> findByRequestId(long requestId);

    List<AllocationResult> findSuccessfulByRequestId(long requestId);

    AllocationResult save(AllocationResult allocationResult);

    void update(AllocationResult allocationResult);

    void deleteById(long id);

    void deleteByRequestId(long requestId);
}
