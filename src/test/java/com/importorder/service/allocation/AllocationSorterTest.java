package com.importorder.service.allocation;

import com.importorder.model.ImportSite;
import com.importorder.model.Inventory;
import com.importorder.model.Merchandise;
import com.importorder.model.enums.DeliveryMeans;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllocationSorterTest {

    @Test
    void sort_shipBeforeAirThenStockDescending() {
        ImportSite siteA = new ImportSite(1L, "S1", "Site 1", 10, 3, null);
        ImportSite siteB = new ImportSite(2L, "S2", "Site 2", 10, 3, null);
        Merchandise merchandise = new Merchandise(1L, "M1", "Item", "pcs", null);
        Inventory invA = new Inventory(1L, 1L, 1L, 50, "pcs", null);
        Inventory invB = new Inventory(2L, 2L, 1L, 100, "pcs", null);

        List<AllocationCandidate> sorted = AllocationSorter.sort(List.of(
                new AllocationCandidate(siteB, merchandise, invB, DeliveryMeans.AIR_DELIVERY, 100, 3),
                new AllocationCandidate(siteA, merchandise, invA, DeliveryMeans.SHIP_DELIVERY, 50, 10),
                new AllocationCandidate(siteB, merchandise, invB, DeliveryMeans.SHIP_DELIVERY, 100, 10)));

        assertEquals(DeliveryMeans.SHIP_DELIVERY, sorted.get(0).getDeliveryMeans());
        assertEquals(100, sorted.get(0).getAvailableStock());
        assertEquals(DeliveryMeans.SHIP_DELIVERY, sorted.get(1).getDeliveryMeans());
        assertEquals(50, sorted.get(1).getAvailableStock());
        assertEquals(DeliveryMeans.AIR_DELIVERY, sorted.get(2).getDeliveryMeans());
    }
}
