package com.importorder.model;

import java.util.Objects;

/**
 * Overseas import site with ship and air transit times.
 */
public class ImportSite {

    private Long id;
    private String siteCode;
    private String siteName;
    private int shipDeliveryDays;
    private int airDeliveryDays;
    private String otherInformation;

    public ImportSite() {
    }

    public ImportSite(Long id, String siteCode, String siteName, int shipDeliveryDays,
                      int airDeliveryDays, String otherInformation) {
        this.id = id;
        this.siteCode = siteCode;
        this.siteName = siteName;
        this.shipDeliveryDays = shipDeliveryDays;
        this.airDeliveryDays = airDeliveryDays;
        this.otherInformation = otherInformation;
    }

    public ImportSite(String siteCode, String siteName, int shipDeliveryDays,
                      int airDeliveryDays, String otherInformation) {
        this(null, siteCode, siteName, shipDeliveryDays, airDeliveryDays, otherInformation);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public int getShipDeliveryDays() {
        return shipDeliveryDays;
    }

    public void setShipDeliveryDays(int shipDeliveryDays) {
        this.shipDeliveryDays = shipDeliveryDays;
    }

    public int getAirDeliveryDays() {
        return airDeliveryDays;
    }

    public void setAirDeliveryDays(int airDeliveryDays) {
        this.airDeliveryDays = airDeliveryDays;
    }

    public String getOtherInformation() {
        return otherInformation;
    }

    public void setOtherInformation(String otherInformation) {
        this.otherInformation = otherInformation;
    }

    @Override
    public String toString() {
        if (siteCode == null && siteName == null) {
            return "";
        }
        if (siteName == null) {
            return siteCode;
        }
        if (siteCode == null) {
            return siteName;
        }
        return siteCode + " - " + siteName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportSite that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
