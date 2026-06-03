package com.importorder.service.dashboard;

public record InventoryShortageAlert(
        String siteCode,
        String merchandiseCode,
        int inStockQuantity,
        String unit) {
}
