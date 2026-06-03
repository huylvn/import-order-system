package com.importorder.service;

/**
 * Raised when business validation fails in the service layer.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
