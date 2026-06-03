package com.importorder.service.siteorder;

/**
 * Read model for displaying confirmed site orders.
 */
public record SiteOrderRow(
        long orderId,
        long importRequestId,
        String importRequestCode,
        String siteCode,
        String siteName,
        String status,
        String createdAt,
        String sentAt) {

    @Override
    public String toString() {
        return "Đơn #" + orderId + " - " + siteCode;
    }
}
