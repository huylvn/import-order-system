package com.importorder.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Shared validation helpers for services.
 */
final class ServiceValidation {

    private ServiceValidation() {
    }

    static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(displayName(fieldName) + " là bắt buộc");
        }
    }

    static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(displayName(fieldName) + " phải lớn hơn 0");
        }
    }

    static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(displayName(fieldName) + " không được âm");
        }
    }

    static LocalDate parseDate(String dateText, String fieldName) {
        requireNonBlank(dateText, fieldName);
        try {
            return LocalDate.parse(dateText.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(displayName(fieldName) + " phải là ngày hợp lệ (yyyy-MM-dd)");
        }
    }

    static void requireDeliveryDateOnOrAfterRequestDate(String requestDate, String desiredDeliveryDate) {
        LocalDate request = parseDate(requestDate, "requestDate");
        LocalDate desired = parseDate(desiredDeliveryDate, "desiredDeliveryDate");
        if (desired.isBefore(request)) {
            throw new ValidationException("Ngày giao mong muốn phải bằng hoặc sau ngày yêu cầu");
        }
    }

    private static String displayName(String fieldName) {
        return switch (fieldName) {
            case "requestCode" -> "Mã yêu cầu";
            case "requestDate" -> "Ngày yêu cầu";
            case "desiredDeliveryDate" -> "Ngày giao mong muốn";
            case "quantityOrdered" -> "Số lượng đặt";
            case "unit" -> "Đơn vị";
            case "quantity" -> "Số lượng";
            case "inStockQuantity" -> "Số lượng tồn";
            case "siteCode" -> "Mã site";
            case "siteName" -> "Tên site";
            case "shipDeliveryDays" -> "Số ngày giao bằng tàu";
            case "airDeliveryDays" -> "Số ngày giao bằng máy bay";
            case "actualReceivedQuantity" -> "Số lượng thực nhận";
            default -> fieldName;
        };
    }
}
