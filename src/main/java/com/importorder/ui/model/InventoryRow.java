package com.importorder.ui.model;

/**
 * Table row for inventory screen with resolved site and merchandise codes.
 */
public class InventoryRow {

    private final Long id;
    private final Long siteId;
    private final String siteCode;
    private final Long merchandiseId;
    private final String merchandiseCode;
    private final int inStockQuantity;
    private final String unit;
    private final String lastUpdatedAt;

    public InventoryRow(Long id, Long siteId, String siteCode, Long merchandiseId, String merchandiseCode,
                        int inStockQuantity, String unit, String lastUpdatedAt) {
        this.id = id;
        this.siteId = siteId;
        this.siteCode = siteCode;
        this.merchandiseId = merchandiseId;
        this.merchandiseCode = merchandiseCode;
        this.inStockQuantity = inStockQuantity;
        this.unit = unit;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public Long getMerchandiseId() {
        return merchandiseId;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public int getInStockQuantity() {
        return inStockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
