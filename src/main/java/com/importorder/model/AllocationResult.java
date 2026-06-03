package com.importorder.model;

import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.DeliveryMeans;

import java.util.Objects;

/**
 * Site allocation outcome for a single import request line (possibly partial per site).
 */
public class AllocationResult {

    private Long id;
    private Long importRequestId;
    private Long requestItemId;
    private Long siteId;
    private Long merchandiseId;
    private Integer allocatedQuantity;
    private String unit;
    private DeliveryMeans deliveryMeans;
    private AllocationStatus status;
    private String errorMessage;

    public AllocationResult() {
    }

    public AllocationResult(Long id, Long importRequestId, Long requestItemId, Long siteId,
                            Long merchandiseId, Integer allocatedQuantity, String unit,
                            DeliveryMeans deliveryMeans, AllocationStatus status,
                            String errorMessage) {
        this.id = id;
        this.importRequestId = importRequestId;
        this.requestItemId = requestItemId;
        this.siteId = siteId;
        this.merchandiseId = merchandiseId;
        this.allocatedQuantity = allocatedQuantity;
        this.unit = unit;
        this.deliveryMeans = deliveryMeans;
        this.status = status;
        this.errorMessage = errorMessage;
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

    public Long getRequestItemId() {
        return requestItemId;
    }

    public void setRequestItemId(Long requestItemId) {
        this.requestItemId = requestItemId;
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

    public Integer getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public void setAllocatedQuantity(Integer allocatedQuantity) {
        this.allocatedQuantity = allocatedQuantity;
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

    public AllocationStatus getStatus() {
        return status;
    }

    public void setStatus(AllocationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AllocationResult that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
