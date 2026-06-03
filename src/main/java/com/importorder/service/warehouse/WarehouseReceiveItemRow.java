package com.importorder.service.warehouse;

import com.importorder.model.enums.DeliveryMeans;
import com.importorder.model.enums.ReceivedStatus;

/**
 * Read model for warehouse receiving lines.
 */
public record WarehouseReceiveItemRow(
        long siteOrderItemId,
        String merchandiseCode,
        String merchandiseName,
        int quantityOrdered,
        String unit,
        DeliveryMeans deliveryMeans,
        Integer actualReceivedQuantity,
        String note,
        ReceivedStatus receivedStatus) {
}
