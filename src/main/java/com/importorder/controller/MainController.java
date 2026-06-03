package com.importorder.controller;

import com.importorder.context.ApplicationContext;
import com.importorder.view.ViewFactory;
import com.importorder.view.ViewType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * Main shell controller: sidebar navigation and content area swapping.
 * Business logic is not handled here — only UI navigation skeleton.
 */
public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label statusLabel;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button merchandiseButton;
    @FXML
    private Button importSiteButton;
    @FXML
    private Button siteCatalogButton;
    @FXML
    private Button inventoryButton;
    @FXML
    private Button importRequestButton;
    @FXML
    private Button allocationResultButton;
    @FXML
    private Button siteOrderButton;
    @FXML
    private Button warehouseReceivingButton;

    private final ViewFactory viewFactory = new ViewFactory(
            ApplicationContext.getInstance()::createController);

    @FXML
    private void initialize() {
        showView(ViewType.DASHBOARD);
    }

    @FXML
    private void onDashboard() {
        showView(ViewType.DASHBOARD);
    }

    @FXML
    private void onMerchandise() {
        showView(ViewType.MERCHANDISE);
    }

    @FXML
    private void onImportSite() {
        showView(ViewType.IMPORT_SITE);
    }

    @FXML
    private void onSiteCatalog() {
        showView(ViewType.SITE_CATALOG);
    }

    @FXML
    private void onInventory() {
        showView(ViewType.INVENTORY);
    }

    @FXML
    private void onImportRequest() {
        showView(ViewType.IMPORT_REQUEST);
    }

    @FXML
    private void onAllocationResult() {
        showView(ViewType.ALLOCATION_RESULT);
    }

    @FXML
    private void onSiteOrder() {
        showView(ViewType.SITE_ORDER);
    }

    @FXML
    private void onWarehouseReceiving() {
        showView(ViewType.WAREHOUSE_RECEIVING);
    }

    /**
     * Loads the given view into the central content area.
     */
    public void showView(ViewType viewType) {
        Node view = viewFactory.createView(viewType);
        contentArea.getChildren().setAll(view);
        if (statusLabel != null) {
            statusLabel.setText(viewType.getDisplayName());
        }
        updateActiveNavigation(viewType);
    }

    private void updateActiveNavigation(ViewType activeViewType) {
        for (Button button : navigationButtons()) {
            button.getStyleClass().remove("nav-button-active");
        }
        Button activeButton = buttonFor(activeViewType);
        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    private List<Button> navigationButtons() {
        return List.of(
                dashboardButton,
                merchandiseButton,
                importSiteButton,
                siteCatalogButton,
                inventoryButton,
                importRequestButton,
                allocationResultButton,
                siteOrderButton,
                warehouseReceivingButton);
    }

    private Button buttonFor(ViewType viewType) {
        return switch (viewType) {
            case DASHBOARD -> dashboardButton;
            case MERCHANDISE -> merchandiseButton;
            case IMPORT_SITE -> importSiteButton;
            case SITE_CATALOG -> siteCatalogButton;
            case INVENTORY -> inventoryButton;
            case IMPORT_REQUEST -> importRequestButton;
            case ALLOCATION_RESULT -> allocationResultButton;
            case SITE_ORDER -> siteOrderButton;
            case WAREHOUSE_RECEIVING -> warehouseReceivingButton;
        };
    }
}
