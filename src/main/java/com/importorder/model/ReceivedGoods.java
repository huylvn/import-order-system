package com.importorder.model;

import com.importorder.model.enums.ReceivedStatus;

import java.util.Objects;

/**
 * Warehouse receipt record for a site order line.
 */
public class ReceivedGoods {

    private Long id;
    private Long siteOrderItemId;
    private int actualReceivedQuantity;
    private String receivedAt;
    private ReceivedStatus status;
    private String note;

    public ReceivedGoods() {
    }

    public ReceivedGoods(Long id, Long siteOrderItemId, int actualReceivedQuantity,
                         String receivedAt, ReceivedStatus status, String note) {
        this.id = id;
        this.siteOrderItemId = siteOrderItemId;
        this.actualReceivedQuantity = actualReceivedQuantity;
        this.receivedAt = receivedAt;
        this.status = status;
        this.note = note;
    }

    public ReceivedGoods(Long siteOrderItemId, int actualReceivedQuantity,
                         ReceivedStatus status) {
        this(null, siteOrderItemId, actualReceivedQuantity, null, status, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSiteOrderItemId() {
        return siteOrderItemId;
    }

    public void setSiteOrderItemId(Long siteOrderItemId) {
        this.siteOrderItemId = siteOrderItemId;
    }

    public int getActualReceivedQuantity() {
        return actualReceivedQuantity;
    }

    public void setActualReceivedQuantity(int actualReceivedQuantity) {
        this.actualReceivedQuantity = actualReceivedQuantity;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public ReceivedStatus getStatus() {
        return status;
    }

    public void setStatus(ReceivedStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReceivedGoods that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
