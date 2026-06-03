package com.importorder.model.enums;

/**
 * Transportation method for an allocated or ordered quantity.
 */
public enum DeliveryMeans {

    SHIP_DELIVERY,
    AIR_DELIVERY;

    public static DeliveryMeans fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return DeliveryMeans.valueOf(value.trim());
    }
}
