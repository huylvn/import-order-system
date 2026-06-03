package com.importorder.view;

/**
 * Identifies application views for navigation and FXML loading.
 */
public enum ViewType {

    DASHBOARD("DashboardView.fxml", "Tổng quan"),
    MERCHANDISE("MerchandiseView.fxml", "Mặt hàng"),
    IMPORT_SITE("ImportSiteView.fxml", "Site nhập khẩu"),
    SITE_CATALOG("SiteCatalogView.fxml", "Danh mục site"),
    INVENTORY("InventoryView.fxml", "Tồn kho"),
    IMPORT_REQUEST("ImportRequestListView.fxml", "Yêu cầu nhập hàng"),
    ALLOCATION_RESULT("AllocationResultView.fxml", "Kết quả phân bổ"),
    SITE_ORDER("SiteOrderView.fxml", "Đơn đặt site"),
    WAREHOUSE_RECEIVING("WarehouseReceivingView.fxml", "Nhập kho");

    private final String fxmlFileName;
    private final String displayName;

    ViewType(String fxmlFileName, String displayName) {
        this.fxmlFileName = fxmlFileName;
        this.displayName = displayName;
    }

    public String getFxmlFileName() {
        return fxmlFileName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
