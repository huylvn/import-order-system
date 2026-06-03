package com.importorder.service.delivery;

import com.importorder.model.enums.DeliveryMeans;

import java.util.Objects;

/**
 * Ship-based delivery option.
 */
public class ShipDeliveryOption implements DeliveryOption {

    private final int deliveryDays;

    public ShipDeliveryOption(int deliveryDays) {
        if (deliveryDays < 0) {
            throw new IllegalArgumentException("deliveryDays must not be negative");
        }
        this.deliveryDays = deliveryDays;
    }

    @Override
    public boolean canDeliverWithin(int availableDays) {
        return deliveryDays <= availableDays;
    }

    @Override
    public DeliveryMeans getDeliveryMeans() {
        return DeliveryMeans.SHIP_DELIVERY;
    }

    @Override
    public int getDeliveryDays() {
        return deliveryDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShipDeliveryOption that)) {
            return false;
        }
        return deliveryDays == that.deliveryDays;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryDays);
    }
}
