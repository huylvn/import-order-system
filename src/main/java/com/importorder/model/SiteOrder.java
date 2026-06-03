package com.importorder.model;

import java.util.Objects;

/**
 * Order placed to an overseas import site after allocation.
 */
public class SiteOrder {

    private Long id;
    private Long importRequestId;
    private Long siteId;
    private String status;
    private String createdAt;
    private String sentAt;

    public SiteOrder() {
    }

    public SiteOrder(Long id, Long importRequestId, Long siteId, String status,
                     String createdAt, String sentAt) {
        this.id = id;
        this.importRequestId = importRequestId;
        this.siteId = siteId;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public SiteOrder(Long importRequestId, Long siteId, String status) {
        this(null, importRequestId, siteId, status, null, null);
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

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        if (id == null) {
            return "Đơn đặt site";
        }
        return "Đơn đặt site #" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SiteOrder that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
