package com.importorder.service.allocation;

import com.importorder.model.enums.DeliveryMeans;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts allocation candidates according to business priority rules.
 */
public final class AllocationSorter {

    private static final Comparator<AllocationCandidate> COMPARATOR = Comparator
            // 1. Prefer ship delivery over air delivery
            .comparing((AllocationCandidate c) -> c.getDeliveryMeans() != DeliveryMeans.SHIP_DELIVERY)
            // 2. Prefer sites with higher available stock (greedy, fewer sites)
            .thenComparing(AllocationCandidate::getAvailableStock, Comparator.reverseOrder());

    private AllocationSorter() {
    }

    /**
     * Returns a new list sorted by ship-before-air, then descending stock.
     */
    public static List<AllocationCandidate> sort(List<AllocationCandidate> candidates) {
        List<AllocationCandidate> sorted = new ArrayList<>(candidates);
        sorted.sort(COMPARATOR);
        return sorted;
    }
}
