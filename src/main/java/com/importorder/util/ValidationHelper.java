package com.importorder.util;

import com.importorder.service.ValidationException;
import javafx.scene.control.TextField;

/**
 * UI input parsing helpers that delegate validation rules to service-layer messages.
 */
public final class ValidationHelper {

    private ValidationHelper() {
    }

    public static String requireText(TextField field, String fieldName) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " là bắt buộc");
        }
        return value.trim();
    }

    public static String optionalText(TextField field) {
        if (field.getText() == null || field.getText().isBlank()) {
            return null;
        }
        return field.getText().trim();
    }

    public static int requireNonNegativeInt(TextField field, String fieldName) {
        return parseInt(field, fieldName, false, false);
    }

    public static int requirePositiveInt(TextField field, String fieldName) {
        return parseInt(field, fieldName, true, false);
    }

    private static int parseInt(TextField field, String fieldName, boolean positive, boolean allowNegative) {
        String text = requireText(field, fieldName);
        try {
            int value = Integer.parseInt(text);
            if (positive && value <= 0) {
                throw new ValidationException(fieldName + " phải lớn hơn 0");
            }
            if (!allowNegative && value < 0) {
                throw new ValidationException(fieldName + " không được âm");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " phải là số nguyên hợp lệ");
        }
    }
}
