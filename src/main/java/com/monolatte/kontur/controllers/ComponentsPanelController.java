package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Enums.ComponentColumns;
import com.monolatte.kontur.model.Notes.Enums.ComponentsType;
import com.monolatte.kontur.model.Notes.Manufacturer;
import com.monolatte.kontur.model.SQL.ComponentsDAO;
import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.SQL.Manufacturer_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import com.monolatte.kontur.model.Notifyers.ErrorNotifyer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ComponentsPanelController {
    @FXML ListView<Component> compList;
    @FXML ListView<Manufacturer> manufacturerListView;
    @FXML Button addEmptyButton;
    @FXML Button removeButton;
    @FXML Button openInTableButton;
    @FXML Button searchButton;
    @FXML Button resetSearchButton;
    @FXML Button openManufacturerButton;
    @FXML ChoiceBox<ComponentColumns> columnSorterChoiceBox;
    @FXML TextField searchTextField;
    @FXML TextField idTextField;
    @FXML TextField nameCompTextField;
    @FXML ChoiceBox<ComponentsType> typeChoiceBox;
    @FXML TextField costTextField;
    @FXML TextField quantityTextField;
    @FXML TextArea compInfoTextArea;
    @FXML TextField datasheetPathTextField;
    @FXML Button openDatasheetButton;
    @FXML Button addCompButton;
    @FXML Button saveDataButton;

    private final ComponentsDAO _componentsDAO = SQLTableManager.getInstance().getComponentsManager();
    private final Manufacturer_usageDAO _manufacturerUsageDAO = SQLTableManager.getInstance().getManufacturerUsageDAO();

    @FXML
    public void initialize() {
        this._refreshList();
        this.columnSorterChoiceBox.getItems().addAll(ComponentColumns.values());
        this.typeChoiceBox.getItems().addAll(ComponentsType.values());
    }

    @FXML
    public void _onAddEmptyButtonClicked() {
        try {
            this._componentsDAO.addNote(new Component(
                    "Empty Name", "Empty", "Enter your specification here!",
                    "Enter your datasheet link here!", 100, 1
            ));
            this._refreshList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to add empty note", e.getMessage()).apprise();
        }
    }

    @FXML
    public void _onRemoveButtonClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        try {
            this._componentsDAO.deleteNote(currentItem.getId());
            this._refreshList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to remove component", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onOpenInTableButton() {
        try {
            var popupLoader = new FXMLLoader(ComponentsPanelController.class.getResource("/com/monolatte/kontur/ComponentTablePopup.fxml"));
            Parent root = popupLoader.load();
            Stage popupStage = new Stage();
            Scene popupScene = new Scene(root);
            popupScene.getStylesheets().add(getClass().getResource("/com/monolatte/style/application.css").toExternalForm());
            popupStage.setScene(popupScene);
            popupStage.setTitle("Component table");
            popupStage.setResizable(false);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            new ErrorNotifyer("UI Error", "Could not load Component Table", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onOpenManufacturerButtonClicked() {
        try {
            var popupLoader = new FXMLLoader(ComponentsPanelController.class.getResource("/com/monolatte/kontur/ManufacturersPopup.fxml"));
            Parent root = popupLoader.load();
            Stage popupStage = new Stage();
            Scene popupScene = new Scene(root);
            popupStage.setScene(popupScene);
            popupStage.setTitle("Manufacturer window");
            popupStage.setResizable(false);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            new ErrorNotifyer("UI Error", "Could not load Manufacturers window", e.getMessage()).apprise();
        }
    }

    @FXML
    public void _onOpenDatasheetButtonClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        var pdfFile = new File(currentItem.getDatasheet_link());
        try {
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    new ErrorNotifyer("System Error", "Desktop not supported", "Cannot open file on this system.").apprise();
                }
            } else {
                new ErrorNotifyer("File Not Found", "Invalid Path", "Path: " + currentItem.getDatasheet_link()).apprise();
            }
        } catch (IOException | IllegalArgumentException e) {
            new ErrorNotifyer("Execution Error", "Failed to open datasheet", e.getMessage()).apprise();
        }
    }

    @FXML
    public void _onAddCompButtonClicked() {
        try {
            var currentType = this.typeChoiceBox.getValue();
            this._componentsDAO.addNote(new Component(
                    this.nameCompTextField.getText(),
                    currentType != null ? currentType.getDescription() : "Unknown",
                    this.compInfoTextArea.getText(),
                    this.datasheetPathTextField.getText(),
                    Float.parseFloat(this.costTextField.getText().replace(',', '.')),
                    Integer.parseInt(this.quantityTextField.getText())
            ));
            this._refreshList();
        } catch (NumberFormatException e) {
            new ErrorNotifyer("Input Error", "Invalid Number Format", "Check Price and Quantity fields.").apprise();
        } catch (Exception e) {
            new ErrorNotifyer("Data Error", "Failed to add component", e.getMessage()).apprise();
        }
    }

    @FXML
    public void _onSaveDataButtonClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        try {
            var currentType = this.typeChoiceBox.getValue();
            currentItem.setName(this.nameCompTextField.getText());
            currentItem.setType(currentType != null ? currentType.getDescription() : currentItem.getType());
            currentItem.setSpecification(this.compInfoTextArea.getText());
            currentItem.setDatasheet_link(this.datasheetPathTextField.getText());
            currentItem.setPrice(Float.parseFloat(this.costTextField.getText().replace(',', '.')));
            currentItem.setQuantity(Integer.parseInt(this.quantityTextField.getText()));

            this._componentsDAO.updateNote(currentItem);
            this._refreshList();
        } catch (NumberFormatException e) {
            new ErrorNotifyer("Input Error", "Invalid Number Format", "Check Price and Quantity fields.").apprise();
        } catch (Exception e) {
            new ErrorNotifyer("Update Error", "Failed to save changes", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onSearchButtonClicked() {
        try {
            var description = this.columnSorterChoiceBox.getValue() != null
                    ? this.columnSorterChoiceBox.getValue().getDescription()
                    : "";
            var foundedItems = FXCollections.observableList(this._componentsDAO.search(description, this.searchTextField.getText()));
            this.compList.setItems(foundedItems);
        } catch (Exception e) {
            new ErrorNotifyer("Search Error", "Search failed", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onResetSearchButton() {
        this.searchTextField.setText("");
        this._refreshList();
    }

    @FXML
    public void onCompListMouseClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        this.idTextField.setText(String.valueOf(currentItem.getId()));
        this.nameCompTextField.setText(currentItem.getName());
        this.typeChoiceBox.setValue(ComponentsType.getByDescription(currentItem.getType()));
        this.costTextField.setText(String.valueOf(currentItem.getPrice()));
        this.quantityTextField.setText(String.valueOf(currentItem.getQuantity()));
        this.compInfoTextArea.setText(currentItem.getSpecification());
        this.datasheetPathTextField.setText(currentItem.getDatasheet_link());
        this._refreshManufacturerList();
    }

    private ObservableList<Component> _updateList() {
        return FXCollections.observableList(this._componentsDAO.getAllNotes());
    }

    private void _refreshList() {
        this.compList.setItems(this._updateList());
    }

    private ObservableList<Manufacturer> _updateManufacturerList() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) return FXCollections.observableArrayList();
        return FXCollections.observableList(this._manufacturerUsageDAO.getManufacturersByComponentId(currentItem.getId()));
    }

    private void _refreshManufacturerList() {
        this.manufacturerListView.setItems(this._updateManufacturerList());
    }
}