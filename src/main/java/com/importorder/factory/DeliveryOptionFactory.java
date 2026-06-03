package com.importorder.factory;

import com.importorder.model.ImportSite;
import com.importorder.service.delivery.DeliveryOption;

/**
 * Factory Method: subclasses decide which concrete {@link DeliveryOption} to create for a site.
 */
public abstract class DeliveryOptionFactory {

    public abstract DeliveryOption createDeliveryOption(ImportSite site);
}
