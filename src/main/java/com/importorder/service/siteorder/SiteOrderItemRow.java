package com.importorder.service.siteorder;

import com.importorder.model.enums.DeliveryMeans;

/**
 * Read model for displaying items inside a site order.
 */
public record SiteOrderItemRow(
        long siteOrderItemId,
        String merchandiseCode,
        String merchandiseName,
        int quantityOrdered,
        String unit,
        DeliveryMeans deliveryMeans) {
}
