package com.importorder.model;

import java.util.Objects;

/**
 * Stock level of merchandise at an import site.
 */
public class Inventory {

    private Long id;
    private Long siteId;
    private Long merchandiseId;
    private int inStockQuantity;
    private String unit;
    private String lastUpdatedAt;

    public Inventory() {
    }

    public Inventory(Long id, Long siteId, Long merchandiseId, int inStockQuantity,
                     String unit, String lastUpdatedAt) {
        this.id = id;
        this.siteId = siteId;
        this.merchandiseId = merchandiseId;
        this.inStockQuantity = inStockQuantity;
        this.unit = unit;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Inventory(Long siteId, Long merchandiseId, int inStockQuantity, String unit) {
        this(null, siteId, merchandiseId, inStockQuantity, unit, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Long getMerchandiseId() {
        return merchandiseId;
    }

    public void setMerchandiseId(Long merchandiseId) {
        this.merchandiseId = merchandiseId;
    }

    public int getInStockQuantity() {
        return inStockQuantity;
    }

    public void setInStockQuantity(int inStockQuantity) {
        this.inStockQuantity = inStockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Inventory that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
