package com.importorder.controller;

import com.importorder.model.Merchandise;
import com.importorder.service.MerchandiseService;
import com.importorder.service.ValidationException;
import com.importorder.util.AlertHelper;
import com.importorder.util.ValidationHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Merchandise master data CRUD — delegates to {@link MerchandiseService}.
 */
public class MerchandiseController {

    @FXML
    private TableView<Merchandise> merchandiseTable;
    @FXML
    private TableColumn<Merchandise, String> codeColumn;
    @FXML
    private TableColumn<Merchandise, String> nameColumn;
    @FXML
    private TableColumn<Merchandise, String> unitColumn;
    @FXML
    private TableColumn<Merchandise, String> descriptionColumn;
    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField unitField;
    @FXML
    private TextField descriptionField;

    private final MerchandiseService merchandiseService;
    private Merchandise selected;

    public MerchandiseController(MerchandiseService merchandiseService) {
        this.merchandiseService = merchandiseService;
    }

    @FXML
    private void initialize() {
        codeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnit()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDescription() != null ? data.getValue().getDescription() : ""));

        merchandiseTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selectedItem) -> {
            selected = selectedItem;
            if (selectedItem != null) {
                codeField.setText(selectedItem.getCode());
                nameField.setText(selectedItem.getName());
                unitField.setText(selectedItem.getUnit());
                descriptionField.setText(selectedItem.getDescription());
            }
        });

        loadTable();
    }

    @FXML
    private void onAdd() {
        runAction(() -> {
            Merchandise merchandise = buildFromForm(null);
            merchandiseService.create(merchandise);
            clearForm();
            loadTable();
            AlertHelper.showSuccess("Mặt hàng", "Đã thêm mặt hàng.");
        });
    }

    @FXML
    private void onUpdate() {
        if (selected == null) {
            AlertHelper.showWarning("Mặt hàng", "Vui lòng chọn dòng cần cập nhật.");
            return;
        }
        runAction(() -> {
            Merchandise merchandise = buildFromForm(selected.getId());
            merchandiseService.update(merchandise);
            loadTable();
            AlertHelper.showSuccess("Mặt hàng", "Đã cập nhật mặt hàng.");
        });
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            AlertHelper.showWarning("Mặt hàng", "Vui lòng chọn dòng cần xóa.");
            return;
        }
        if (!AlertHelper.confirm("Xóa", "Xóa mặt hàng " + selected.getCode() + "?")) {
            return;
        }
        runAction(() -> {
            merchandiseService.delete(selected.getId());
            clearForm();
            selected = null;
            loadTable();
            AlertHelper.showSuccess("Mặt hàng", "Đã xóa mặt hàng.");
        });
    }

    @FXML
    private void onClear() {
        clearForm();
        merchandiseTable.getSelectionModel().clearSelection();
        selected = null;
    }

    @FXML
    private void onRefresh() {
        loadTable();
    }

    private Merchandise buildFromForm(Long id) {
        Merchandise merchandise = new Merchandise();
        merchandise.setId(id);
        merchandise.setCode(ValidationHelper.requireText(codeField, "mã"));
        merchandise.setName(ValidationHelper.requireText(nameField, "tên"));
        merchandise.setUnit(ValidationHelper.requireText(unitField, "đơn vị"));
        merchandise.setDescription(ValidationHelper.optionalText(descriptionField));
        return merchandise;
    }

    private void loadTable() {
        merchandiseTable.setItems(FXCollections.observableArrayList(merchandiseService.findAll()));
    }

    private void clearForm() {
        codeField.clear();
        nameField.clear();
        unitField.clear();
        descriptionField.clear();
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
