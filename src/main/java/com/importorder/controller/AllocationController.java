package com.importorder.controller;

import com.importorder.model.ImportRequest;
import com.importorder.model.enums.AllocationStatus;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.service.AllocationService;
import com.importorder.service.SiteOrderService;
import com.importorder.service.ValidationException;
import com.importorder.service.allocation.AllocationResponse;
import com.importorder.service.allocation.AllocationResultRow;
import com.importorder.util.AlertHelper;
import com.importorder.util.UiText;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.EnumSet;
import java.util.Set;

/**
 * Allocation result and order confirmation controller. Business rules stay in services.
 */
public class AllocationController {

    private static final Set<ImportRequestStatus> CONFIRMABLE_STATUSES = EnumSet.of(
            ImportRequestStatus.ALLOCATED,
            ImportRequestStatus.PARTIALLY_ALLOCATED);

    @FXML
    private ComboBox<ImportRequest> requestComboBox;
    @FXML
    private Label requestCodeLabel;
    @FXML
    private Label requestDateLabel;
    @FXML
    private Label requestStatusLabel;
    @FXML
    private Button runAllocationButton;
    @FXML
    private Button confirmOrdersButton;
    @FXML
    private TableView<AllocationResultRow> allocationTable;
    @FXML
    private TableColumn<AllocationResultRow, String> merchandiseCodeColumn;
    @FXML
    private TableColumn<AllocationResultRow, Number> requestedQuantityColumn;
    @FXML
    private TableColumn<AllocationResultRow, String> siteCodeColumn;
    @FXML
    private TableColumn<AllocationResultRow, String> siteNameColumn;
    @FXML
    private TableColumn<AllocationResultRow, Number> allocatedQuantityColumn;
    @FXML
    private TableColumn<AllocationResultRow, String> unitColumn;
    @FXML
    private TableColumn<AllocationResultRow, String> deliveryMeansColumn;
    @FXML
    private TableColumn<AllocationResultRow, String> allocationStatusColumn;
    @FXML
    private TableColumn<AllocationResultRow, String> errorMessageColumn;

    private final AllocationService allocationService;
    private final SiteOrderService siteOrderService;
    private ImportRequest selectedRequest;

    public AllocationController(AllocationService allocationService, SiteOrderService siteOrderService) {
        this.allocationService = allocationService;
        this.siteOrderService = siteOrderService;
    }

    @FXML
    private void initialize() {
        merchandiseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().merchandiseCode()));
        requestedQuantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().requestedQuantity()));
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteCode()));
        siteNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().siteName()));
        allocatedQuantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().allocatedQuantity()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        deliveryMeansColumn.setCellValueFactory(data -> new SimpleStringProperty(
                UiText.deliveryMeans(data.getValue().deliveryMeans())));
        allocationStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                UiText.allocationStatus(data.getValue().status())));
        errorMessageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().errorMessage()));

        allocationStatusColumn.setCellFactory(column -> styledStatusCell());
        deliveryMeansColumn.setCellFactory(column -> deliveryMeansCell());
        requestComboBox.valueProperty().addListener((obs, old, request) -> selectRequest(request));

        loadRequests();
    }

    @FXML
    private void onRunAllocation() {
        if (!ensureRequestSelected()) {
            return;
        }
        runAction(() -> {
            AllocationResponse response = allocationService.allocateImportRequest(selectedRequest.getId());
            selectedRequest = allocationService.findRequestById(selectedRequest.getId());
            refreshSelectedRequestView();
            loadResults();
            loadRequestsKeepingSelection();
            if (response.hasFailure()) {
                AlertHelper.showWarning(
                        "Phân bổ",
                        "Phân bổ hoàn tất nhưng có dòng thất bại. Vui lòng xem cột Thông báo lỗi.");
            } else {
                AlertHelper.showSuccess("Phân bổ", "Phân bổ thành công.");
            }
        });
    }

    @FXML
    private void onConfirmOrders() {
        if (!ensureRequestSelected()) {
            return;
        }
        if (!CONFIRMABLE_STATUSES.contains(selectedRequest.getStatus())) {
            AlertHelper.showWarning("Xác nhận đơn đặt",
                    "Chỉ yêu cầu đã phân bổ hoặc phân bổ một phần mới có thể xác nhận đơn đặt.");
            return;
        }
        boolean partial = selectedRequest.getStatus() == ImportRequestStatus.PARTIALLY_ALLOCATED;
        runAction(() -> {
            siteOrderService.confirmOrders(selectedRequest.getId());
            selectedRequest = allocationService.findRequestById(selectedRequest.getId());
            refreshSelectedRequestView();
            if (partial) {
                AlertHelper.showWarning(
                        "Xác nhận đơn đặt",
                        "Chỉ các dòng phân bổ thành công được xác nhận. Yêu cầu này chỉ được đáp ứng một phần.");
            } else {
                AlertHelper.showSuccess("Xác nhận đơn đặt", "Đã xác nhận đơn đặt site.");
            }
        });
    }

    @FXML
    private void onRefresh() {
        loadRequestsKeepingSelection();
        if (selectedRequest != null) {
            selectedRequest = allocationService.findRequestById(selectedRequest.getId());
            refreshSelectedRequestView();
            loadResults();
        }
    }

    private void loadRequests() {
        requestComboBox.setItems(FXCollections.observableArrayList(allocationService.findRequestsForAllocation()));
        clearRequestView();
    }

    private void loadRequestsKeepingSelection() {
        Long selectedId = selectedRequest != null ? selectedRequest.getId() : null;
        requestComboBox.setItems(FXCollections.observableArrayList(allocationService.findRequestsForAllocation()));
        if (selectedId != null) {
            requestComboBox.getItems().stream()
                    .filter(request -> request.getId().equals(selectedId))
                    .findFirst()
                    .ifPresentOrElse(
                            request -> requestComboBox.getSelectionModel().select(request),
                            () -> requestComboBox.getSelectionModel().clearSelection());
        }
    }

    private void selectRequest(ImportRequest request) {
        selectedRequest = request;
        if (request == null) {
            clearRequestView();
            return;
        }
        refreshSelectedRequestView();
        loadResults();
    }

    private void refreshSelectedRequestView() {
        requestCodeLabel.setText(selectedRequest.getRequestCode());
        requestDateLabel.setText(selectedRequest.getRequestDate());
        requestStatusLabel.setText(UiText.importRequestStatus(selectedRequest.getStatus()));
        runAllocationButton.setDisable(false);
        confirmOrdersButton.setDisable(!CONFIRMABLE_STATUSES.contains(selectedRequest.getStatus()));
    }

    private void loadResults() {
        allocationTable.setItems(FXCollections.observableArrayList(
                allocationService.findResultRowsByRequestId(selectedRequest.getId())));
    }

    private void clearRequestView() {
        selectedRequest = null;
        requestCodeLabel.setText("-");
        requestDateLabel.setText("-");
        requestStatusLabel.setText("-");
        runAllocationButton.setDisable(true);
        confirmOrdersButton.setDisable(true);
        allocationTable.setItems(FXCollections.emptyObservableList());
    }

    private boolean ensureRequestSelected() {
        if (selectedRequest == null) {
            AlertHelper.showWarning("Phân bổ", "Vui lòng chọn yêu cầu nhập hàng trước.");
            return false;
        }
        return true;
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

    private static TableCell<AllocationResultRow, String> styledStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("status-success", "status-failed");
                if (!empty && UiText.allocationStatus(AllocationStatus.SUCCESS).equals(item)) {
                    getStyleClass().add("status-success");
                }
                if (!empty && UiText.allocationStatus(AllocationStatus.FAILED).equals(item)) {
                    getStyleClass().add("status-failed");
                }
            }
        };
    }

    private static TableCell<AllocationResultRow, String> deliveryMeansCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("delivery-ship", "delivery-air");
                if (!empty && UiText.deliveryMeans(com.importorder.model.enums.DeliveryMeans.SHIP_DELIVERY).equals(item)) {
                    getStyleClass().add("delivery-ship");
                }
                if (!empty && UiText.deliveryMeans(com.importorder.model.enums.DeliveryMeans.AIR_DELIVERY).equals(item)) {
                    getStyleClass().add("delivery-air");
                }
            }
        };
    }
}
