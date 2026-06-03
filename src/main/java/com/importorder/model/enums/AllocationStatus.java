package com.importorder.model.enums;

/**
 * Outcome of site allocation for a request line.
 */
public enum AllocationStatus {

    SUCCESS,
    FAILED;

    public static AllocationStatus fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return AllocationStatus.valueOf(value.trim());
    }
}
