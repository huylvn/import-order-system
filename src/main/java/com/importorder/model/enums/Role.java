package com.importorder.model.enums;

/**
 * Application user role (for future authorization).
 */
public enum Role {

    ADMIN,
    SALES,
    OVERSEAS_ORDER,
    WAREHOUSE;

    public static Role fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Role.valueOf(value.trim());
    }
}
