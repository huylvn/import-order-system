package com.importorder.factory;

import com.importorder.model.ImportSite;
import com.importorder.service.delivery.DeliveryOption;
import com.importorder.service.delivery.AirDeliveryOption;

import java.util.Objects;

/**
 * Creates air delivery options from site air transit days.
 */
public class AirDeliveryOptionFactory extends DeliveryOptionFactory {

    @Override
    public DeliveryOption createDeliveryOption(ImportSite site) {
        Objects.requireNonNull(site, "site must not be null");
        return new AirDeliveryOption(site.getAirDeliveryDays());
    }
}
