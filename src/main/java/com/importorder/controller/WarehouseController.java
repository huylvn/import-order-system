package com.importorder.controller;

import com.importorder.model.ReceivedGoods;
import com.importorder.service.ValidationException;
import com.importorder.service.WarehouseService;
import com.importorder.service.siteorder.SiteOrderRow;
import com.importorder.service.warehouse.WarehouseReceiveItemRow;
import com.importorder.util.AlertHelper;
import com.importorder.util.UiText;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Warehouse receiving controller. It delegates receiving business rules to WarehouseService.
 */
public class WarehouseController {

    @FXML
    private ComboBox<SiteOrderRow> siteOrderComboBox;
    @FXML
    private Button receiveGoodsButton;
    @FXML
    private TableView<ReceiveItemEditRow> receiveItemTable;
    @FXML
    private TableColumn<ReceiveItemEditRow, String> merchandiseCodeColumn;
    @FXML
    private TableColumn<ReceiveItemEditRow, String> merchandiseNameColumn;
    @FXML
    private TableColumn<ReceiveItemEditRow, Number> quantityOrderedColumn;
    @FXML
    private TableColumn<ReceiveItemEditRow, String> unitColumn;
    @FXML
    private TableColumn<ReceiveItemEditRow, String> deliveryMeansColumn;
    @FXML
    private TableColumn<ReceiveItemEditRow, String> actualReceivedQuantityColumn;
    @FXML
    private TableColumn<ReceiveItemEditRow, String> noteColumn;
    @FXML
    private TableColumn<ReceiveItemEditRow, String> receivedStatusColumn;

    private final WarehouseService warehouseService;
    private SiteOrderRow selectedSiteOrder;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @FXML
    private void initialize() {
        receiveItemTable.setEditable(true);
        merchandiseCodeColumn.setCellValueFactory(data -> data.getValue().merchandiseCodeProperty());
        merchandiseNameColumn.setCellValueFactory(data -> data.getValue().merchandiseNameProperty());
        quantityOrderedColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().quantityOrdered()));
        unitColumn.setCellValueFactory(data -> data.getValue().unitProperty());
        deliveryMeansColumn.setCellValueFactory(data -> data.getValue().deliveryMeansProperty());
        actualReceivedQuantityColumn.setCellValueFactory(data -> data.getValue().actualReceivedQuantityProperty());
        noteColumn.setCellValueFactory(data -> data.getValue().noteProperty());
        receivedStatusColumn.setCellValueFactory(data -> data.getValue().receivedStatusProperty());

        actualReceivedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        actualReceivedQuantityColumn.setOnEditCommit(event ->
                event.getRowValue().setActualReceivedQuantity(event.getNewValue()));
        noteColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        noteColumn.setOnEditCommit(event -> event.getRowValue().setNote(event.getNewValue()));
        receivedStatusColumn.setCellFactory(column -> receivedStatusCell());

        siteOrderComboBox.valueProperty().addListener((obs, old, selected) -> selectSiteOrder(selected));
        loadSentOrders();
    }

    @FXML
    private void onReceiveGoods() {
        if (selectedSiteOrder == null) {
            AlertHelper.showWarning("Nhập kho", "Vui lòng chọn đơn đặt site trước.");
            return;
        }
        runAction(() -> {
            List<ReceivedGoods> receivedGoods = new ArrayList<>();
            for (ReceiveItemEditRow row : receiveItemTable.getItems()) {
                receivedGoods.add(warehouseService.receiveGoods(
                        row.siteOrderItemId(),
                        row.actualReceivedQuantity(),
                        row.note()));
            }
            loadItems(selectedSiteOrder.orderId());
            loadSentOrdersKeepingSelection();
            AlertHelper.showSuccess(
                    "Nhập kho",
                    "Đã ghi nhận nhập kho cho " + receivedGoods.size() + " mặt hàng.");
        });
    }

    @FXML
    private void onRefresh() {
        loadSentOrdersKeepingSelection();
        if (selectedSiteOrder != null) {
            loadItems(selectedSiteOrder.orderId());
        }
    }

    private void loadSentOrders() {
        siteOrderComboBox.setItems(FXCollections.observableArrayList(warehouseService.findSentSiteOrders()));
        receiveGoodsButton.setDisable(true);
        receiveItemTable.setItems(FXCollections.emptyObservableList());
    }

    private void loadSentOrdersKeepingSelection() {
        Long selectedId = selectedSiteOrder != null ? selectedSiteOrder.orderId() : null;
        siteOrderComboBox.setItems(FXCollections.observableArrayList(warehouseService.findSentSiteOrders()));
        if (selectedId != null) {
            siteOrderComboBox.getItems().stream()
                    .filter(order -> order.orderId() == selectedId)
                    .findFirst()
                    .ifPresentOrElse(
                            order -> siteOrderComboBox.getSelectionModel().select(order),
                            () -> {
                                selectedSiteOrder = null;
                                siteOrderComboBox.getSelectionModel().clearSelection();
                                receiveGoodsButton.setDisable(true);
                            });
        }
    }

    private void selectSiteOrder(SiteOrderRow siteOrder) {
        selectedSiteOrder = siteOrder;
        receiveGoodsButton.setDisable(siteOrder == null);
        if (siteOrder == null) {
            receiveItemTable.setItems(FXCollections.emptyObservableList());
        } else {
            loadItems(siteOrder.orderId());
        }
    }

    private void loadItems(long siteOrderId) {
        receiveItemTable.setItems(FXCollections.observableArrayList(
                warehouseService.findReceiveItemRows(siteOrderId).stream()
                        .map(ReceiveItemEditRow::new)
                        .toList()));
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

    private static TableCell<ReceiveItemEditRow, String> receivedStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("status-success", "status-failed", "status-warning");
                if (!empty && UiText.receivedStatus(com.importorder.model.enums.ReceivedStatus.MATCHED).equals(item)) {
                    getStyleClass().add("status-success");
                }
                if (!empty && UiText.receivedStatus(com.importorder.model.enums.ReceivedStatus.SHORTAGE).equals(item)) {
                    getStyleClass().add("status-failed");
                }
                if (!empty && UiText.receivedStatus(com.importorder.model.enums.ReceivedStatus.EXCESS).equals(item)) {
                    getStyleClass().add("status-warning");
                }
            }
        };
    }

    public static final class ReceiveItemEditRow {

        private final long siteOrderItemId;
        private final int quantityOrdered;
        private final SimpleStringProperty merchandiseCode;
        private final SimpleStringProperty merchandiseName;
        private final SimpleStringProperty unit;
        private final SimpleStringProperty deliveryMeans;
        private final SimpleStringProperty actualReceivedQuantity;
        private final SimpleStringProperty note;
        private final SimpleStringProperty receivedStatus;

        private ReceiveItemEditRow(WarehouseReceiveItemRow row) {
            this.siteOrderItemId = row.siteOrderItemId();
            this.quantityOrdered = row.quantityOrdered();
            this.merchandiseCode = new SimpleStringProperty(row.merchandiseCode());
            this.merchandiseName = new SimpleStringProperty(row.merchandiseName());
            this.unit = new SimpleStringProperty(row.unit());
            this.deliveryMeans = new SimpleStringProperty(UiText.deliveryMeans(row.deliveryMeans()));
            this.actualReceivedQuantity = new SimpleStringProperty(
                    row.actualReceivedQuantity() != null ? row.actualReceivedQuantity().toString() : "");
            this.note = new SimpleStringProperty(row.note() != null ? row.note() : "");
            this.receivedStatus = new SimpleStringProperty(
                    UiText.receivedStatus(row.receivedStatus()));
        }

        private long siteOrderItemId() {
            return siteOrderItemId;
        }

        private int quantityOrdered() {
            return quantityOrdered;
        }

        private String actualReceivedQuantity() {
            return actualReceivedQuantity.get();
        }

        private void setActualReceivedQuantity(String value) {
            actualReceivedQuantity.set(value != null ? value : "");
        }

        private String note() {
            return note.get();
        }

        private void setNote(String value) {
            note.set(value != null ? value : "");
        }

        private SimpleStringProperty merchandiseCodeProperty() {
            return merchandiseCode;
        }

        private SimpleStringProperty merchandiseNameProperty() {
            return merchandiseName;
        }

        private SimpleStringProperty unitProperty() {
            return unit;
        }

        private SimpleStringProperty deliveryMeansProperty() {
            return deliveryMeans;
        }

        private SimpleStringProperty actualReceivedQuantityProperty() {
            return actualReceivedQuantity;
        }

        private SimpleStringProperty noteProperty() {
            return note;
        }

        private SimpleStringProperty receivedStatusProperty() {
            return receivedStatus;
        }
    }
}
