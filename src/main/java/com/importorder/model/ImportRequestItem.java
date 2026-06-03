package com.importorder.model;

import java.util.Objects;

/**
 * Line item on an import request from Sales.
 */
public class ImportRequestItem {

    private Long id;
    private Long importRequestId;
    private Long merchandiseId;
    private int quantityOrdered;
    private String unit;
    private String desiredDeliveryDate;

    public ImportRequestItem() {
    }

    public ImportRequestItem(Long id, Long importRequestId, Long merchandiseId,
                             int quantityOrdered, String unit, String desiredDeliveryDate) {
        this.id = id;
        this.importRequestId = importRequestId;
        this.merchandiseId = merchandiseId;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.desiredDeliveryDate = desiredDeliveryDate;
    }

    public ImportRequestItem(Long importRequestId, Long merchandiseId, int quantityOrdered,
                             String unit, String desiredDeliveryDate) {
        this(null, importRequestId, merchandiseId, quantityOrdered, unit, desiredDeliveryDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getImportRequestId() {
        return importRequestId;
    }

    public void setImportRequestId(Long importRequestId) {
        this.importRequestId = importRequestId;
    }

    public Long getMerchandiseId() {
        return merchandiseId;
    }

    public void setMerchandiseId(Long merchandiseId) {
        this.merchandiseId = merchandiseId;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(int quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDesiredDeliveryDate() {
        return desiredDeliveryDate;
    }

    public void setDesiredDeliveryDate(String desiredDeliveryDate) {
        this.desiredDeliveryDate = desiredDeliveryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportRequestItem that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
