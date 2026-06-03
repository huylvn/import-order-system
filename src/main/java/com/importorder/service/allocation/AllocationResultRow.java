package com.importorder.service.allocation;

import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.DeliveryMeans;

/**
 * Read model for displaying allocation results in JavaFX tables.
 */
public record AllocationResultRow(
        String merchandiseCode,
        int requestedQuantity,
        String siteCode,
        String siteName,
        int allocatedQuantity,
        String unit,
        DeliveryMeans deliveryMeans,
        AllocationStatus status,
        String errorMessage) {
}
