package com.importorder.controller;

import com.importorder.model.ImportSite;
import com.importorder.service.ImportSiteService;
import com.importorder.service.ValidationException;
import com.importorder.util.AlertHelper;
import com.importorder.util.ValidationHelper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Import site master data CRUD — delegates to {@link ImportSiteService}.
 */
public class ImportSiteController {

    @FXML
    private TableView<ImportSite> siteTable;
    @FXML
    private TableColumn<ImportSite, String> siteCodeColumn;
    @FXML
    private TableColumn<ImportSite, String> siteNameColumn;
    @FXML
    private TableColumn<ImportSite, Number> shipDaysColumn;
    @FXML
    private TableColumn<ImportSite, Number> airDaysColumn;
    @FXML
    private TableColumn<ImportSite, String> otherInfoColumn;
    @FXML
    private TextField siteCodeField;
    @FXML
    private TextField siteNameField;
    @FXML
    private TextField shipDaysField;
    @FXML
    private TextField airDaysField;
    @FXML
    private TextField otherInfoField;

    private final ImportSiteService importSiteService;
    private ImportSite selected;

    public ImportSiteController(ImportSiteService importSiteService) {
        this.importSiteService = importSiteService;
    }

    @FXML
    private void initialize() {
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSiteCode()));
        siteNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSiteName()));
        shipDaysColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getShipDeliveryDays()));
        airDaysColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAirDeliveryDays()));
        otherInfoColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOtherInformation() != null ? data.getValue().getOtherInformation() : ""));

        siteTable.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> {
            selected = item;
            if (item != null) {
                siteCodeField.setText(item.getSiteCode());
                siteNameField.setText(item.getSiteName());
                shipDaysField.setText(String.valueOf(item.getShipDeliveryDays()));
                airDaysField.setText(String.valueOf(item.getAirDeliveryDays()));
                otherInfoField.setText(item.getOtherInformation());
            }
        });

        loadTable();
    }

    @FXML
    private void onAdd() {
        runAction(() -> {
            importSiteService.create(buildFromForm(null));
            clearForm();
            loadTable();
            AlertHelper.showSuccess("Site nhập khẩu", "Đã thêm site.");
        });
    }

    @FXML
    private void onUpdate() {
        if (selected == null) {
            AlertHelper.showWarning("Site nhập khẩu", "Vui lòng chọn dòng cần cập nhật.");
            return;
        }
        runAction(() -> {
            importSiteService.update(buildFromForm(selected.getId()));
            loadTable();
            AlertHelper.showSuccess("Site nhập khẩu", "Đã cập nhật site.");
        });
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            AlertHelper.showWarning("Site nhập khẩu", "Vui lòng chọn dòng cần xóa.");
            return;
        }
        if (!AlertHelper.confirm("Xóa", "Xóa site " + selected.getSiteCode() + "?")) {
            return;
        }
        runAction(() -> {
            importSiteService.delete(selected.getId());
            clearForm();
            selected = null;
            loadTable();
            AlertHelper.showSuccess("Site nhập khẩu", "Đã xóa site.");
        });
    }

    @FXML
    private void onClear() {
        clearForm();
        siteTable.getSelectionModel().clearSelection();
        selected = null;
    }

    @FXML
    private void onRefresh() {
        loadTable();
    }

    private ImportSite buildFromForm(Long id) {
        ImportSite site = new ImportSite();
        site.setId(id);
        site.setSiteCode(ValidationHelper.requireText(siteCodeField, "mã site"));
        site.setSiteName(ValidationHelper.requireText(siteNameField, "tên site"));
        site.setShipDeliveryDays(ValidationHelper.requireNonNegativeInt(shipDaysField, "số ngày giao bằng tàu"));
        site.setAirDeliveryDays(ValidationHelper.requireNonNegativeInt(airDaysField, "số ngày giao bằng máy bay"));
        site.setOtherInformation(ValidationHelper.optionalText(otherInfoField));
        return site;
    }

    private void loadTable() {
        siteTable.setItems(FXCollections.observableArrayList(importSiteService.findAll()));
    }

    private void clearForm() {
        siteCodeField.clear();
        siteNameField.clear();
        shipDaysField.clear();
        airDaysField.clear();
        otherInfoField.clear();
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
