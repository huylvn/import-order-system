package com.importorder.controller;

import com.importorder.service.DashboardService;
import com.importorder.service.ValidationException;
import com.importorder.service.dashboard.DashboardSummary;
import com.importorder.service.dashboard.InventoryShortageAlert;
import com.importorder.util.AlertHelper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Dashboard statistics — delegates to {@link DashboardService}.
 */
public class DashboardController {

    @FXML
    private Label totalRequestsLabel;
    @FXML
    private Label submittedRequestsLabel;
    @FXML
    private Label allocatedRequestsLabel;
    @FXML
    private Label failedRequestsLabel;
    @FXML
    private Label totalSiteOrdersLabel;
    @FXML
    private TableView<InventoryShortageAlert> shortageTable;
    @FXML
    private TableColumn<InventoryShortageAlert, String> siteCodeColumn;
    @FXML
    private TableColumn<InventoryShortageAlert, String> merchandiseCodeColumn;
    @FXML
    private TableColumn<InventoryShortageAlert, Number> quantityColumn;
    @FXML
    private TableColumn<InventoryShortageAlert, String> unitColumn;

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @FXML
    private void initialize() {
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
        merchandiseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseCode()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().inStockQuantity()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        try {
            DashboardSummary summary = dashboardService.getSummary();
            totalRequestsLabel.setText(String.valueOf(summary.totalImportRequests()));
            submittedRequestsLabel.setText(String.valueOf(summary.submittedRequests()));
            allocatedRequestsLabel.setText(String.valueOf(summary.allocatedRequests()));
            failedRequestsLabel.setText(String.valueOf(summary.failedRequests()));
            totalSiteOrdersLabel.setText(String.valueOf(summary.totalSiteOrders()));
            shortageTable.setItems(FXCollections.observableArrayList(summary.inventoryShortageAlerts()));
        } catch (ValidationException ex) {
            AlertHelper.showError("Tổng quan", ex.getMessage());
        } catch (RuntimeException ex) {
            AlertHelper.showError("Tổng quan", ex.getMessage());
        }
    }
}
