package com.importorder.controller;

import com.importorder.model.ImportRequest;
import com.importorder.model.ImportRequestItem;
import com.importorder.model.Merchandise;
import com.importorder.model.enums.ImportRequestStatus;
import com.importorder.service.ImportRequestService;
import com.importorder.service.ValidationException;
import com.importorder.util.AlertHelper;
import com.importorder.util.UiText;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Import request UI workflow controller. It only coordinates UI events and delegates use cases to services.
 */
public class ImportRequestController {

    private static final String FORM_FXML = "/fxml/ImportRequestFormView.fxml";

    @FXML
    private StackPane importRequestRoot;
    @FXML
    private Parent listContent;
    @FXML
    private TableView<ImportRequest> requestTable;
    @FXML
    private TableColumn<ImportRequest, String> requestCodeColumn;
    @FXML
    private TableColumn<ImportRequest, String> requestDateColumn;
    @FXML
    private TableColumn<ImportRequest, String> statusColumn;
    @FXML
    private TableColumn<ImportRequest, String> createdAtColumn;
    @FXML
    private Button viewDetailButton;
    @FXML
    private Button submitRequestButton;

    @FXML
    private TextField requestCodeField;
    @FXML
    private DatePicker requestDatePicker;
    @FXML
    private ComboBox<Merchandise> merchandiseComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField unitField;
    @FXML
    private DatePicker desiredDeliveryDatePicker;
    @FXML
    private TableView<RequestItemRow> itemTable;
    @FXML
    private TableColumn<RequestItemRow, String> merchandiseColumn;
    @FXML
    private TableColumn<RequestItemRow, Number> quantityColumn;
    @FXML
    private TableColumn<RequestItemRow, String> unitColumn;
    @FXML
    private TableColumn<RequestItemRow, String> desiredDeliveryDateColumn;
    @FXML
    private Button addItemButton;
    @FXML
    private Button removeItemButton;
    @FXML
    private Button saveDraftButton;
    @FXML
    private Button submitFormButton;
    @FXML
    private Label formTitleLabel;

    private final ImportRequestService importRequestService;
    private final ObservableList<RequestItemRow> formItems = FXCollections.observableArrayList();
    private boolean listInitialized;
    private boolean formInitialized;
    private ImportRequest selectedRequest;

    public ImportRequestController(ImportRequestService importRequestService) {
        this.importRequestService = importRequestService;
    }

    @FXML
    private void initialize() {
        if (requestTable != null && !listInitialized) {
            initializeList();
        }
        if (itemTable != null && !formInitialized) {
            initializeForm();
        }
    }

    @FXML
    private void onCreateNewRequest() {
        loadForm(null);
    }

    @FXML
    private void onViewDetail() {
        ImportRequest request = requireSelectedRequest();
        if (request != null) {
            loadForm(request);
        }
    }

    @FXML
    private void onSubmitSelectedRequest() {
        ImportRequest request = requireSelectedRequest();
        if (request == null) {
            return;
        }
        runAction(() -> {
            importRequestService.submitRequest(request.getId());
            loadRequests();
            AlertHelper.showSuccess("Yêu cầu nhập hàng", "Đã gửi yêu cầu.");
        });
    }

    @FXML
    private void onRunAllocation() {
        AlertHelper.showWarning("Phân bổ", "Vui lòng chạy phân bổ tại màn hình Kết quả phân bổ.");
    }

    @FXML
    private void onAddItem() {
        Merchandise merchandise = merchandiseComboBox.getValue();
        if (merchandise == null) {
            AlertHelper.showError("Dữ liệu không hợp lệ", "Vui lòng chọn mặt hàng.");
            return;
        }
        LocalDate desiredDeliveryDate = desiredDeliveryDatePicker.getValue();
        if (desiredDeliveryDate == null) {
            AlertHelper.showError("Dữ liệu không hợp lệ", "Vui lòng chọn ngày giao mong muốn.");
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            AlertHelper.showError("Dữ liệu không hợp lệ", "Số lượng đặt phải là số hợp lệ.");
            return;
        }

        formItems.add(new RequestItemRow(merchandise, quantity, unitField.getText(), desiredDeliveryDate.toString()));
        clearItemInputs();
    }

    @FXML
    private void onRemoveItem() {
        RequestItemRow selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            AlertHelper.showWarning("Yêu cầu nhập hàng", "Vui lòng chọn mặt hàng cần xóa.");
            return;
        }
        formItems.remove(selectedItem);
    }

    @FXML
    private void onSaveDraft() {
        saveCurrentForm(ImportRequestStatus.DRAFT);
    }

    @FXML
    private void onSubmitForm() {
        if (selectedRequest != null && selectedRequest.getStatus() == ImportRequestStatus.DRAFT) {
            runAction(() -> {
                importRequestService.submitRequest(selectedRequest.getId());
                returnToList();
                AlertHelper.showSuccess("Yêu cầu nhập hàng", "Đã gửi yêu cầu.");
            });
            return;
        }
        saveCurrentForm(ImportRequestStatus.SUBMITTED);
    }

    @FXML
    private void onBackToList() {
        returnToList();
    }

    private void initializeList() {
        listInitialized = true;
        requestCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestCode()));
        requestDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestDate()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                UiText.importRequestStatus(data.getValue().getStatus())));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt() : ""));

        requestTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean hasSelection = selected != null;
            viewDetailButton.setDisable(!hasSelection);
            submitRequestButton.setDisable(!hasSelection || selected.getStatus() != ImportRequestStatus.DRAFT);
        });
        loadRequests();
    }

    private void initializeForm() {
        formInitialized = true;
        merchandiseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMerchandiseText()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().quantityOrdered()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().unit()));
        desiredDeliveryDateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().desiredDeliveryDate()));
        itemTable.setItems(formItems);

        merchandiseComboBox.setItems(FXCollections.observableArrayList(importRequestService.findAllMerchandise()));
        merchandiseComboBox.valueProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                unitField.setText(selected.getUnit());
            }
        });
    }

    private void loadForm(ImportRequest request) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FORM_FXML));
            loader.setController(this);
            Parent form = loader.load();
            importRequestRoot.getChildren().setAll(form);
            selectedRequest = request;
            populateForm(request);
        } catch (IOException e) {
            AlertHelper.showError("Yêu cầu nhập hàng", "Không thể tải form yêu cầu nhập hàng: " + e.getMessage());
        }
    }

    private void populateForm(ImportRequest request) {
        formItems.clear();
        boolean detailMode = request != null;
        if (detailMode) {
            formTitleLabel.setText("Chi tiết yêu cầu nhập hàng");
            requestCodeField.setText(request.getRequestCode());
            requestDatePicker.setValue(LocalDate.parse(request.getRequestDate()));
            List<ImportRequestItem> items = importRequestService.findItemsByRequestId(request.getId());
            for (ImportRequestItem item : items) {
                findMerchandise(item.getMerchandiseId()).ifPresent(merchandise -> formItems.add(new RequestItemRow(
                        merchandise,
                        item.getQuantityOrdered(),
                        item.getUnit(),
                        item.getDesiredDeliveryDate())));
            }
        } else {
            formTitleLabel.setText("Tạo yêu cầu nhập hàng");
            requestCodeField.clear();
            requestDatePicker.setValue(LocalDate.now());
        }
        setEditControlsDisabled(detailMode);
        saveDraftButton.setDisable(detailMode);
        submitFormButton.setDisable(detailMode && request.getStatus() != ImportRequestStatus.DRAFT);
    }

    private java.util.Optional<Merchandise> findMerchandise(Long merchandiseId) {
        return merchandiseComboBox.getItems().stream()
                .filter(merchandise -> merchandise.getId().equals(merchandiseId))
                .findFirst();
    }

    private void setEditControlsDisabled(boolean disabled) {
        requestCodeField.setDisable(disabled);
        requestDatePicker.setDisable(disabled);
        merchandiseComboBox.setDisable(disabled);
        quantityField.setDisable(disabled);
        unitField.setDisable(disabled);
        desiredDeliveryDatePicker.setDisable(disabled);
        addItemButton.setDisable(disabled);
        removeItemButton.setDisable(disabled);
    }

    private void saveCurrentForm(ImportRequestStatus status) {
        runAction(() -> {
            ImportRequest savedRequest = importRequestService.createRequest(
                    requestCodeField.getText(),
                    requestDatePicker.getValue() != null ? requestDatePicker.getValue().toString() : null,
                    formItems.stream().map(RequestItemRow::toImportRequestItem).toList(),
                    status);
            returnToList();
            AlertHelper.showSuccess("Yêu cầu nhập hàng",
                    savedRequest.getStatus() == ImportRequestStatus.SUBMITTED
                            ? "Đã lưu và gửi yêu cầu."
                            : "Đã lưu nháp.");
        });
    }

    private ImportRequest requireSelectedRequest() {
        ImportRequest request = requestTable.getSelectionModel().getSelectedItem();
        if (request == null) {
            AlertHelper.showWarning("Yêu cầu nhập hàng", "Vui lòng chọn một yêu cầu trước.");
        }
        return request;
    }

    private void returnToList() {
        importRequestRoot.getChildren().setAll(listContent);
        selectedRequest = null;
        formInitialized = false;
        loadRequests();
    }

    private void loadRequests() {
        requestTable.setItems(FXCollections.observableArrayList(importRequestService.findAll()));
        viewDetailButton.setDisable(true);
        submitRequestButton.setDisable(true);
    }

    private void clearItemInputs() {
        merchandiseComboBox.getSelectionModel().clearSelection();
        quantityField.clear();
        unitField.clear();
        desiredDeliveryDatePicker.setValue(null);
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

    private record RequestItemRow(Merchandise merchandise, int quantityOrdered, String unit,
                                  String desiredDeliveryDate) {

        private String getMerchandiseText() {
            return merchandise.toString();
        }

        private ImportRequestItem toImportRequestItem() {
            ImportRequestItem item = new ImportRequestItem();
            item.setMerchandiseId(merchandise.getId());
            item.setQuantityOrdered(quantityOrdered);
            item.setUnit(unit);
            item.setDesiredDeliveryDate(desiredDeliveryDate);
            return item;
        }
    }
}
