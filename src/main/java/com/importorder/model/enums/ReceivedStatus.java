package com.importorder.model.enums;

/**
 * Comparison result between ordered and received quantity.
 */
public enum ReceivedStatus {

    MATCHED,
    SHORTAGE,
    EXCESS;

    public static ReceivedStatus fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ReceivedStatus.valueOf(value.trim());
    }
}
