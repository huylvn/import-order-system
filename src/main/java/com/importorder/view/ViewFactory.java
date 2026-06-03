package com.importorder.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Factory Method style factory for creating view nodes from FXML by {@link ViewType}.
 */
public class ViewFactory {

    private static final String FXML_BASE_PATH = "/fxml/";

    private final Callback<Class<?>, Object> controllerFactory;

    public ViewFactory() {
        this(null);
    }

    public ViewFactory(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    /**
     * Loads the FXML layout associated with the given view type.
     *
     * @param viewType target view
     * @return root node of the loaded view
     * @throws ViewLoadException if FXML cannot be loaded
     */
    public Parent createView(ViewType viewType) {
        Objects.requireNonNull(viewType, "viewType must not be null");

        URL resourceUrl = ViewFactory.class.getResource(FXML_BASE_PATH + viewType.getFxmlFileName());
        if (resourceUrl == null) {
            throw new ViewLoadException(
                    "FXML not found: " + FXML_BASE_PATH + viewType.getFxmlFileName());
        }

        FXMLLoader loader = new FXMLLoader(resourceUrl);
        if (controllerFactory != null) {
            loader.setControllerFactory(controllerFactory);
        }

        try {
            return loader.load();
        } catch (IOException e) {
            throw new ViewLoadException("Failed to load view: " + viewType.name(), e);
        }
    }
}
