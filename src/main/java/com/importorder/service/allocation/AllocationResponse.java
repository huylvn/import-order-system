package com.importorder.service.allocation;

import com.importorder.model.AllocationResult;
import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.ImportRequestStatus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Outcome of allocating an import request across overseas sites.
 */
public class AllocationResponse {

    private final long importRequestId;
    private final ImportRequestStatus finalStatus;
    private final List<AllocationResult> results;

    public AllocationResponse(long importRequestId, ImportRequestStatus finalStatus,
                              List<AllocationResult> results) {
        this.importRequestId = importRequestId;
        this.finalStatus = Objects.requireNonNull(finalStatus, "finalStatus must not be null");
        this.results = List.copyOf(Objects.requireNonNull(results, "results must not be null"));
    }

    public long getImportRequestId() {
        return importRequestId;
    }

    public ImportRequestStatus getFinalStatus() {
        return finalStatus;
    }

    public List<AllocationResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    public boolean hasFailure() {
        return results.stream().anyMatch(r -> r.getStatus() == AllocationStatus.FAILED);
    }

    public int totalSuccessCount() {
        return (int) results.stream().filter(r -> r.getStatus() == AllocationStatus.SUCCESS).count();
    }

    public int totalFailureCount() {
        return (int) results.stream().filter(r -> r.getStatus() == AllocationStatus.FAILED).count();
    }
}
