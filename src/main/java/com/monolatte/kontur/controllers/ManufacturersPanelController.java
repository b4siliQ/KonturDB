package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Enums.ComponentColumns;
import com.monolatte.kontur.model.Notes.Manufacturer;
import com.monolatte.kontur.model.Notes.Manufacturer_Usage;
import com.monolatte.kontur.model.SQL.DAOFactory;
import com.monolatte.kontur.model.SQL.ManufacturerDAO;
import com.monolatte.kontur.model.SQL.Manufacturer_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import com.monolatte.kontur.model.Notifyers.ErrorNotifyer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ManufacturersPanelController {
    @FXML ListView<Manufacturer> manufacturerListView;
    @FXML ListView<Component> componentsListView;
    @FXML Button addEmptyButton;
    @FXML Button removeButton;
    @FXML Button addManufacturerButton;
    @FXML Button saveDataButton;
    @FXML Button pinButton;
    @FXML Button unpinButton;
    @FXML TextField idManufacturerTextField;
    @FXML TextField nameManufacturerTextField;
    @FXML TextArea descriptionManufacturerTextArea;

    private final ManufacturerDAO _manufacturerDAO = SQLTableManager.getInstance().getManufacturerDAO();
    private final Manufacturer_usageDAO _manufacturerUsageDAO = SQLTableManager.getInstance().getManufacturerUsageDAO();

    @FXML
    public void initialize() {
        this._refreshMainList();
    }

    @FXML
    public void onAddEmptyButtonClicked() {
        try {
            this._manufacturerDAO.addNote(new Manufacturer("new manufacture", "new manufacture"));
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to add empty manufacturer", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onRemoveButtonClicked() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) return;
        try {
            this._manufacturerDAO.deleteNote(currentManufacturer.getId());
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to remove manufacturer", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onAddManufacturerButtonClicked() {
        try {
            this._manufacturerDAO.addNote(new Manufacturer(
                    this.nameManufacturerTextField.getText(),
                    this.descriptionManufacturerTextArea.getText()
            ));
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Data Error", "Failed to create manufacturer", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onSaveDataButton() {
        var currentManufacturer = this.manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) return;

        try {
            currentManufacturer.setName(nameManufacturerTextField.getText());
            currentManufacturer.setDescription(descriptionManufacturerTextArea.getText());
            this._manufacturerDAO.updateNote(currentManufacturer);
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Update Error", "Failed to update manufacturer", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onPinButtonClicked() {
        var currentManufacturer = this.manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) {
            new ErrorNotifyer("Selection Error", "No Manufacturer Selected", "Please select a manufacturer first.").apprise();
            return;
        }

        try {
            FXMLLoader popupLoader = new FXMLLoader(getClass().getResource("/com/monolatte/kontur/SearchPopup.fxml"));
            Parent root = popupLoader.load();
            SearchPopupController<Component> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.COMPONENT);
            popupController.initData(ComponentColumns.values());

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            popupController.setStage(newStage);
            newStage.setTitle("Component Searcher");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                this._manufacturerUsageDAO.addNote(new Manufacturer_Usage(result.getId(), currentManufacturer.getId()));
                this._refreshComponentLists();
            }
        } catch (IOException e) {
            new ErrorNotifyer("UI Error", "Could not load Search Popup", e.getMessage()).apprise();
        } catch (Exception e) {
            new ErrorNotifyer("Link Error", "Failed to pin component", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onUnpinButtonClicked() {
        var currentManufacturer = this.manufacturerListView.getSelectionModel().getSelectedItem();
        var currentComponent = this.componentsListView.getSelectionModel().getSelectedItem();

        if (currentManufacturer == null || currentComponent == null) {
            new ErrorNotifyer("Selection Error", "Missing Selection", "Select both manufacturer and component to unpin.").apprise();
            return;
        }

        try {
            this._manufacturerUsageDAO.removeComponentByManufacturerId(currentComponent.getId(), currentManufacturer.getId());
            this._refreshComponentLists();
        } catch (Exception e) {
            new ErrorNotifyer("Link Error", "Failed to unpin component", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onManufacturerListViewMouseClicked() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) return;

        this.idManufacturerTextField.setText(String.valueOf(currentManufacturer.getId()));
        this.nameManufacturerTextField.setText(currentManufacturer.getName());
        this.descriptionManufacturerTextArea.setText(currentManufacturer.getDescription());
        this._refreshComponentLists();
    }

    @FXML
    public void onComponentsListViewMouseClicked() {
        // Логика взаимодействия со списком компонентов (если нужна)
    }

    private void _refreshMainList() {
        try {
            this.manufacturerListView.setItems(FXCollections.observableList(this._manufacturerDAO.getAllNotes()));
        } catch (Exception e) {
            new ErrorNotifyer("Fetch Error", "Failed to refresh list", e.getMessage()).apprise();
        }
    }

    private void _refreshComponentLists() {
        var currentManufacturer = this.manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) {
            this.componentsListView.getItems().clear();
            return;
        }
        try {
            this.componentsListView.setItems(FXCollections.observableList(this._manufacturerUsageDAO.getComponentsByManufacturerId(currentManufacturer.getId())));
        } catch (Exception e) {
            new ErrorNotifyer("Fetch Error", "Failed to refresh components", e.getMessage()).apprise();
        }
    }
}