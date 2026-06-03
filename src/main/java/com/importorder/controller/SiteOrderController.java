package com.importorder.controller;

import com.importorder.service.SiteOrderService;
import com.importorder.service.ValidationException;
import com.importorder.service.siteorder.SiteOrderItemRow;
import com.importorder.service.siteorder.SiteOrderRow;
import com.importorder.util.AlertHelper;
import com.importorder.util.UiText;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Site order list controller. It delegates data retrieval to SiteOrderService.
 */
public class SiteOrderController {

    @FXML
    private TableView<SiteOrderRow> siteOrderTable;
    @FXML
    private TableColumn<SiteOrderRow, Number> orderIdColumn;
    @FXML
    private TableColumn<SiteOrderRow, String> importRequestCodeColumn;
    @FXML
    private TableColumn<SiteOrderRow, String> siteCodeColumn;
    @FXML
    private TableColumn<SiteOrderRow, String> siteNameColumn;
    @FXML
    private TableColumn<SiteOrderRow, String> statusColumn;
    @FXML
    private TableColumn<SiteOrderRow, String> createdAtColumn;
    @FXML
    private TableColumn<SiteOrderRow, String> sentAtColumn;
    @FXML
    private TableView<SiteOrderItemRow> orderItemTable;
    @FXML
    private TableColumn<SiteOrderItemRow, String> merchandiseCodeColumn;
    @FXML
    private TableColumn<SiteOrderItemRow, String> merchandiseNameColumn;
    @FXML
    private TableColumn<SiteOrderItemRow, Number> quantityOrderedColumn;
    @FXML
    private TableColumn<SiteOrderItemRow, String> unitColumn;
    @FXML
    private TableColumn<SiteOrderItemRow, String> deliveryMeansColumn;

    private final SiteOrderService siteOrderService;

    public SiteOrderController(SiteOrderService siteOrderService) {
        this.siteOrderService = siteOrderService;
    }

    @FXML
    private void initialize() {
        orderIdColumn.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().orderId()));
        importRequestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().importRequestCode()));
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
        siteNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                UiText.siteOrderStatus(data.getValue().status())));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt()));
        sentAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().sentAt()));

        merchandiseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseCode()));
        merchandiseNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseName()));
        quantityOrderedColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().quantityOrdered()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        deliveryMeansColumn.setCellValueFactory(data -> new SimpleStringProperty(
                UiText.deliveryMeans(data.getValue().deliveryMeans())));

        siteOrderTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                orderItemTable.setItems(FXCollections.emptyObservableList());
            } else {
                loadItems(selected.orderId());
            }
        });
        loadOrders();
    }

    @FXML
    private void onRefresh() {
        loadOrders();
    }

    private void loadOrders() {
        runAction(() -> {
            siteOrderTable.setItems(FXCollections.observableArrayList(siteOrderService.findAllRows()));
            orderItemTable.setItems(FXCollections.emptyObservableList());
        });
    }

    private void loadItems(long siteOrderId) {
        runAction(() -> orderItemTable.setItems(FXCollections.observableArrayList(
                siteOrderService.findItemRowsBySiteOrderId(siteOrderId))));
    }

    private void runAction(Runnable action) {
        try {
            action.run();
        } catch (ValidationException ex) {
            AlertHelper.showError("Dữ liệu không hợp lệ", ex.getMessage());
        } catch (RuntimeException ex) {
            AlertHelper.showError("Lỗi", ex.getMessage());
        }
    }
}
