package com.importorder.service.allocation;

import com.importorder.model.ImportSite;
import com.importorder.model.Inventory;
import com.importorder.model.Merchandise;
import com.importorder.model.enums.DeliveryMeans;

import java.util.Objects;

/**
 * A site that can supply merchandise for allocation, including delivery feasibility.
 */
public class AllocationCandidate {

    private final ImportSite site;
    private final Merchandise merchandise;
    private final Inventory inventory;
    private final DeliveryMeans deliveryMeans;
    private final int availableStock;
    private final int deliveryDays;

    public AllocationCandidate(ImportSite site, Merchandise merchandise, Inventory inventory,
                               DeliveryMeans deliveryMeans, int availableStock, int deliveryDays) {
        this.site = Objects.requireNonNull(site, "site must not be null");
        this.merchandise = Objects.requireNonNull(merchandise, "merchandise must not be null");
        this.inventory = Objects.requireNonNull(inventory, "inventory must not be null");
        this.deliveryMeans = Objects.requireNonNull(deliveryMeans, "deliveryMeans must not be null");
        this.availableStock = availableStock;
        this.deliveryDays = deliveryDays;
    }

    public ImportSite getSite() {
        return site;
    }

    public Merchandise getMerchandise() {
        return merchandise;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public DeliveryMeans getDeliveryMeans() {
        return deliveryMeans;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }

    public long getSiteId() {
        return site.getId();
    }
}
