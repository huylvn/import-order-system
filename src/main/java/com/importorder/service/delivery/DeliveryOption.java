package com.importorder.service.delivery;

import com.importorder.model.enums.DeliveryMeans;

/**
 * Represents a delivery method with transit time and feasibility check.
 */
public interface DeliveryOption {

    /**
     * @param availableDays days available until the desired delivery date
     * @return true if this option can deliver within the available days
     */
    boolean canDeliverWithin(int availableDays);

    DeliveryMeans getDeliveryMeans();

    int getDeliveryDays();
}
