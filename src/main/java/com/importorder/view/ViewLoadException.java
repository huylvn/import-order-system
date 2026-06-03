package com.importorder.view;

/**
 * Raised when an FXML view cannot be loaded.
 */
public class ViewLoadException extends RuntimeException {

    public ViewLoadException(String message) {
        super(message);
    }

    public ViewLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
