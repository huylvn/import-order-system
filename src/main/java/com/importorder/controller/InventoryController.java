package com.importorder.controller;

import com.importorder.model.ImportSite;
import com.importorder.model.Inventory;
import com.importorder.model.Merchandise;
import com.importorder.service.ImportSiteService;
import com.importorder.service.InventoryService;
import com.importorder.service.MerchandiseService;
import com.importorder.service.ValidationException;
import com.importorder.ui.model.InventoryRow;
import com.importorder.util.AlertHelper;
import com.importorder.util.ValidationHelper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory management — delegates to {@link InventoryService}.
 */
public class InventoryController {

    @FXML
    private TableView<InventoryRow> inventoryTable;
    @FXML
    private TableColumn<InventoryRow, String> siteCodeColumn;
    @FXML
    private TableColumn<InventoryRow, String> merchandiseCodeColumn;
    @FXML
    private TableColumn<InventoryRow, Number> quantityColumn;
    @FXML
    private TableColumn<InventoryRow, String> unitColumn;
    @FXML
    private TableColumn<InventoryRow, String> lastUpdatedColumn;
    @FXML
    private ComboBox<ImportSite> siteComboBox;
    @FXML
    private ComboBox<Merchandise> merchandiseComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField unitField;

    private final InventoryService inventoryService;
    private final ImportSiteService importSiteService;
    private final MerchandiseService merchandiseService;

    public InventoryController(InventoryService inventoryService,
                                 ImportSiteService importSiteService,
                                 MerchandiseService merchandiseService) {
        this.inventoryService = inventoryService;
        this.importSiteService = importSiteService;
        this.merchandiseService = merchandiseService;
    }

    @FXML
    private void initialize() {
        siteCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSiteCode()));
        merchandiseCodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMerchandiseCode()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInStockQuantity()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnit()));
        lastUpdatedColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLastUpdatedAt() != null ? data.getValue().getLastUpdatedAt() : ""));

        siteComboBox.setItems(FXCollections.observableArrayList(importSiteService.findAll()));
        merchandiseComboBox.setItems(FXCollections.observableArrayList(merchandiseService.findAll()));

        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> {
            if (row == null) {
                return;
            }
            selectComboById(siteComboBox, row.getSiteId());
            selectComboById(merchandiseComboBox, row.getMerchandiseId());
            quantityField.setText(String.valueOf(row.getInStockQuantity()));
            unitField.setText(row.getUnit());
        });

        merchandiseComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> {
            if (item != null && (unitField.getText() == null || unitField.getText().isBlank())) {
                unitField.setText(item.getUnit());
            }
        });

        loadTable();
    }

    @FXML
    private void onSave() {
        ImportSite site = siteComboBox.getSelectionModel().getSelectedItem();
        Merchandise merchandise = merchandiseComboBox.getSelectionModel().getSelectedItem();
        if (site == null || merchandise == null) {
            AlertHelper.showWarning("Tồn kho", "Vui lòng chọn site và mặt hàng.");
            return;
        }
        runAction(() -> {
            Inventory inventory = new Inventory(
                    site.getId(),
                    merchandise.getId(),
                    ValidationHelper.requireNonNegativeInt(quantityField, "số lượng tồn"),
                    ValidationHelper.requireText(unitField, "đơn vị"));
            inventoryService.createOrUpdateInventory(inventory);
            loadTable();
            AlertHelper.showSuccess("Tồn kho", "Đã lưu tồn kho.");
        });
    }

    @FXML
    private void onClear() {
        quantityField.clear();
        unitField.clear();
        inventoryTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onRefresh() {
        siteComboBox.setItems(FXCollections.observableArrayList(importSiteService.findAll()));
        merchandiseComboBox.setItems(FXCollections.observableArrayList(merchandiseService.findAll()));
        loadTable();
    }

    private void loadTable() {
        List<InventoryRow> rows = new ArrayList<>();
        for (Inventory inventory : inventoryService.findAll()) {
            String siteCode = importSiteService.findById(inventory.getSiteId())
                    .map(ImportSite::getSiteCode)
                    .orElse("?");
            String merchandiseCode = merchandiseService.findById(inventory.getMerchandiseId())
                    .map(Merchandise::getCode)
                    .orElse("?");
            rows.add(new InventoryRow(
                    inventory.getId(),
                    inventory.getSiteId(),
                    siteCode,
                    inventory.getMerchandiseId(),
                    merchandiseCode,
                    inventory.getInStockQuantity(),
                    inventory.getUnit(),
                    inventory.getLastUpdatedAt()));
        }
        inventoryTable.setItems(FXCollections.observableArrayList(rows));
    }

    private static void selectComboById(ComboBox<? extends Object> combo, Long id) {
        for (int i = 0; i < combo.getItems().size(); i++) {
            Object item = combo.getItems().get(i);
            Long itemId = null;
            if (item instanceof ImportSite site) {
                itemId = site.getId();
            } else if (item instanceof Merchandise merchandise) {
                itemId = merchandise.getId();
            }
            if (id != null && id.equals(itemId)) {
                combo.getSelectionModel().select(i);
                return;
            }
        }
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
