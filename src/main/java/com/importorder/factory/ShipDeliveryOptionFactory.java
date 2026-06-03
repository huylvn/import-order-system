package com.importorder.factory;

import com.importorder.model.ImportSite;
import com.importorder.service.delivery.DeliveryOption;
import com.importorder.service.delivery.ShipDeliveryOption;

import java.util.Objects;

/**
 * Creates ship delivery options from site ship transit days.
 */
public class ShipDeliveryOptionFactory extends DeliveryOptionFactory {

    @Override
    public DeliveryOption createDeliveryOption(ImportSite site) {
        Objects.requireNonNull(site, "site must not be null");
        return new ShipDeliveryOption(site.getShipDeliveryDays());
    }
}
