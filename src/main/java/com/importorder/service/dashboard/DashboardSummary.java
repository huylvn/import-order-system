package com.importorder.service.dashboard;

import java.util.List;

public record DashboardSummary(
        int totalImportRequests,
        int submittedRequests,
        int allocatedRequests,
        int failedRequests,
        int totalSiteOrders,
        List<InventoryShortageAlert> inventoryShortageAlerts) {
}
