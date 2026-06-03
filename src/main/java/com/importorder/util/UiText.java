package com.importorder.util;

import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.DeliveryMeans;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.model.enums.ReceivedStatus;

/**
 * Formats domain values for Vietnamese UI display without changing persisted enum values.
 */
public final class UiText {

    private UiText() {
    }

    public static String importRequestStatus(ImportRequestStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case DRAFT -> "Nháp";
            case SUBMITTED -> "Đã gửi";
            case ALLOCATED -> "Đã phân bổ";
            case PARTIALLY_ALLOCATED -> "Phân bổ một phần";
            case ALLOCATION_FAILED -> "Phân bổ lỗi";
            case ORDER_CREATED -> "Đã tạo đơn";
            case ORDER_SENT -> "Đã gửi đơn";
            case RECEIVED -> "Đã nhập kho";
        };
    }

    public static String deliveryMeans(DeliveryMeans deliveryMeans) {
        if (deliveryMeans == null) {
            return "";
        }
        return switch (deliveryMeans) {
            case SHIP_DELIVERY -> "Giao bằng tàu";
            case AIR_DELIVERY -> "Giao bằng máy bay";
        };
    }

    public static String allocationStatus(AllocationStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case SUCCESS -> "Thành công";
            case FAILED -> "Thất bại";
        };
    }

    public static String receivedStatus(ReceivedStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case MATCHED -> "Đủ hàng";
            case SHORTAGE -> "Thiếu hàng";
            case EXCESS -> "Dư hàng";
        };
    }

    public static String siteOrderStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        return switch (status.trim()) {
            case "SENT" -> "Đã gửi";
            case "RECEIVED" -> "Đã nhận";
            default -> status;
        };
    }
}
