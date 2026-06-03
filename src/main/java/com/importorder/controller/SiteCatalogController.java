package com.importorder.controller;

import com.importorder.model.ImportSite;
import com.importorder.model.Merchandise;
import com.importorder.service.ImportSiteService;
import com.importorder.service.MerchandiseService;
import com.importorder.service.SiteCatalogService;
import com.importorder.service.ValidationException;
import com.importorder.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Site merchandise catalog — delegates to {@link SiteCatalogService}.
 */
public class SiteCatalogController {

    @FXML
    private ComboBox<ImportSite> siteComboBox;
    @FXML
    private ComboBox<Merchandise> merchandiseComboBox;
    @FXML
    private TableView<Merchandise> catalogTable;
    @FXML
    private TableColumn<Merchandise, String> codeColumn;
    @FXML
    private TableColumn<Merchandise, String> nameColumn;
    @FXML
    private TableColumn<Merchandise, String> unitColumn;

    private final SiteCatalogService siteCatalogService;
    private final ImportSiteService importSiteService;
    private final MerchandiseService merchandiseService;

    public SiteCatalogController(SiteCatalogService siteCatalogService,
                                 ImportSiteService importSiteService,
                                 MerchandiseService merchandiseService) {
        this.siteCatalogService = siteCatalogService;
        this.importSiteService = importSiteService;
        this.merchandiseService = merchandiseService;
    }

    @FXML
    private void initialize() {
        codeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnit()));

        siteComboBox.setItems(FXCollections.observableArrayList(importSiteService.findAll()));
        merchandiseComboBox.setItems(FXCollections.observableArrayList(merchandiseService.findAll()));

        siteComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, site) -> loadCatalog());

        if (!siteComboBox.getItems().isEmpty()) {
            siteComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onAssign() {
        ImportSite site = siteComboBox.getSelectionModel().getSelectedItem();
        Merchandise merchandise = merchandiseComboBox.getSelectionModel().getSelectedItem();
        if (site == null || merchandise == null) {
            AlertHelper.showWarning("Danh mục site", "Vui lòng chọn cả site và mặt hàng.");
            return;
        }
        runAction(() -> {
            siteCatalogService.assignMerchandiseToSite(site.getId(), merchandise.getId());
            loadCatalog();
            AlertHelper.showSuccess("Danh mục site", "Đã gán mặt hàng cho site.");
        });
    }

    @FXML
    private void onRemove() {
        ImportSite site = siteComboBox.getSelectionModel().getSelectedItem();
        Merchandise selected = catalogTable.getSelectionModel().getSelectedItem();
        if (site == null || selected == null) {
            AlertHelper.showWarning("Danh mục site", "Vui lòng chọn site và dòng danh mục cần xóa.");
            return;
        }
        if (!AlertHelper.confirm("Xóa", "Xóa " + selected.getCode() + " khỏi " + site.getSiteCode() + "?")) {
            return;
        }
        runAction(() -> {
            siteCatalogService.removeMerchandiseFromSite(site.getId(), selected.getId());
            loadCatalog();
            AlertHelper.showSuccess("Danh mục site", "Đã xóa mặt hàng khỏi site.");
        });
    }

    @FXML
    private void onRefresh() {
        siteComboBox.setItems(FXCollections.observableArrayList(importSiteService.findAll()));
        merchandiseComboBox.setItems(FXCollections.observableArrayList(merchandiseService.findAll()));
        loadCatalog();
    }

    private void loadCatalog() {
        ImportSite site = siteComboBox.getSelectionModel().getSelectedItem();
        if (site == null) {
            catalogTable.setItems(FXCollections.observableArrayList());
            return;
        }
        catalogTable.setItems(FXCollections.observableArrayList(
                siteCatalogService.findMerchandiseBySite(site.getId())));
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
