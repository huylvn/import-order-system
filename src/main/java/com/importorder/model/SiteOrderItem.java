package com.importorder.model;

import com.importorder.model.enums.DeliveryMeans;

import java.util.Objects;

/**
 * Line on a site order with quantity and delivery method.
 */
public class SiteOrderItem {

    private Long id;
    private Long siteOrderId;
    private Long merchandiseId;
    private int quantityOrdered;
    private String unit;
    private DeliveryMeans deliveryMeans;

    public SiteOrderItem() {
    }

    public SiteOrderItem(Long id, Long siteOrderId, Long merchandiseId, int quantityOrdered,
                         String unit, DeliveryMeans deliveryMeans) {
        this.id = id;
        this.siteOrderId = siteOrderId;
        this.merchandiseId = merchandiseId;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.deliveryMeans = deliveryMeans;
    }

    public SiteOrderItem(Long siteOrderId, Long merchandiseId, int quantityOrdered,
                         String unit, DeliveryMeans deliveryMeans) {
        this(null, siteOrderId, merchandiseId, quantityOrdered, unit, deliveryMeans);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSiteOrderId() {
        return siteOrderId;
    }

    public void setSiteOrderId(Long siteOrderId) {
        this.siteOrderId = siteOrderId;
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

    public DeliveryMeans getDeliveryMeans() {
        return deliveryMeans;
    }

    public void setDeliveryMeans(DeliveryMeans deliveryMeans) {
        this.deliveryMeans = deliveryMeans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SiteOrderItem that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
