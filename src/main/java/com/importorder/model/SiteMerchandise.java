package com.importorder.model;

import java.util.Objects;

/**
 * Association between an import site and merchandise it trades.
 */
public class SiteMerchandise {

    private Long id;
    private Long siteId;
    private Long merchandiseId;

    public SiteMerchandise() {
    }

    public SiteMerchandise(Long id, Long siteId, Long merchandiseId) {
        this.id = id;
        this.siteId = siteId;
        this.merchandiseId = merchandiseId;
    }

    public SiteMerchandise(Long siteId, Long merchandiseId) {
        this(null, siteId, merchandiseId);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SiteMerchandise that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
