package com.importorder.service;

import com.importorder.model.enums.ReceivedStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarehouseServiceTest {

    @Test
    void determineReceivedStatus() {
        assertEquals(ReceivedStatus.MATCHED, WarehouseService.determineReceivedStatus(100, 100));
        assertEquals(ReceivedStatus.SHORTAGE, WarehouseService.determineReceivedStatus(100, 80));
        assertEquals(ReceivedStatus.EXCESS, WarehouseService.determineReceivedStatus(100, 120));
    }
}
