package com.importorder.model.enums;

/**
 * Lifecycle status of an import request.
 */
public enum ImportRequestStatus {

    DRAFT,
    SUBMITTED,
    ALLOCATED,
    PARTIALLY_ALLOCATED,
    ALLOCATION_FAILED,
    ORDER_CREATED,
    ORDER_SENT,
    RECEIVED;

    public static ImportRequestStatus fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ImportRequestStatus.valueOf(value.trim());
    }
}
