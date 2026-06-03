package com.importorder.service.delivery;

import com.importorder.model.ImportSite;
import com.importorder.model.enums.DeliveryMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryOptionSelectorTest {

    private DeliveryOptionSelector selector;

    @BeforeEach
    void setUp() {
        selector = new DeliveryOptionSelector();
    }

    @Test
    void select_prefersShipWhenBothFeasible() {
        ImportSite site = new ImportSite("S001", "Tokyo", 14, 5, null);

        var option = selector.select(site, 20);

        assertTrue(option.isPresent());
        assertEquals(DeliveryMeans.SHIP_DELIVERY, option.get().getDeliveryMeans());
        assertEquals(14, option.get().getDeliveryDays());
    }

    @Test
    void select_usesAirWhenShipTooSlow() {
        ImportSite site = new ImportSite("S001", "Tokyo", 14, 5, null);

        var option = selector.select(site, 10);

        assertTrue(option.isPresent());
        assertEquals(DeliveryMeans.AIR_DELIVERY, option.get().getDeliveryMeans());
        assertEquals(5, option.get().getDeliveryDays());
    }

    @Test
    void select_emptyWhenNeitherFeasible() {
        ImportSite site = new ImportSite("S001", "Tokyo", 14, 5, null);

        assertTrue(selector.select(site, 3).isEmpty());
    }

    @Test
    void canDeliverWithin_boundaryInclusive() {
        assertTrue(new ShipDeliveryOption(14).canDeliverWithin(14));
        assertTrue(new AirDeliveryOption(5).canDeliverWithin(5));
    }
}
