package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Manufacturer;
import com.monolatte.kontur.model.Notes.Manufacturer_Usage;
import com.monolatte.kontur.model.SQL.DAOFactory;
import com.monolatte.kontur.model.SQL.ManufacturerDAO;
import com.monolatte.kontur.model.SQL.Manufacturer_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
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
    @FXML
    ListView<Manufacturer> manufacturerListView;
    @FXML
    ListView<Component> componentsListView;
    @FXML
    Button addEmptyButton;
    @FXML
    Button removeButton;
    @FXML
    Button addManufacturerButton;
    @FXML
    Button saveDataButton;
    @FXML
    Button pinButton;
    @FXML
    Button unpinButton;
    @FXML
    TextField idManufacturerTextField;
    @FXML
    TextField nameManufacturerTextField;
    @FXML
    TextArea descriptionManufacturerTextArea;

    private final ManufacturerDAO _manufacturerDAO = SQLTableManager.getInstance().getManufacturerDAO();
    private final Manufacturer_usageDAO _manufacturerUsageDAO = SQLTableManager.getInstance().getManufacturerUsageDAO();

    @FXML
    public void initialize() {
        this._refreshMainList();
    }

    @FXML
    public void onAddEmptyButtonClicked() {
        this._manufacturerDAO.addNote(new Manufacturer("new manufacture", "new manufacture"));
        this._refreshMainList();
    }

    @FXML
    public void onRemoveButtonClicked() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) { return; }
        this._manufacturerDAO.deleteNote(currentManufacturer.getId());
        this._refreshMainList();
    }

    @FXML
    public void onAddManufacturerButtonClicked() {
        this._manufacturerDAO.addNote(new Manufacturer(
                this.nameManufacturerTextField.getText(),
                this.descriptionManufacturerTextArea.getText()
        ));
        this._refreshMainList();
    }

    @FXML
    public void onSaveDataButton() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) { return; }

        currentManufacturer.setName(nameManufacturerTextField.getText());
        currentManufacturer.setDescription(descriptionManufacturerTextArea.getText());

        this._manufacturerDAO.updateNote(currentManufacturer);
        this._refreshMainList();
    }

    @FXML
    public void onPinButtonClicked() {
        var currentManufacturer = this.manufacturerListView.getSelectionModel().getSelectedItem();
        try {
            FXMLLoader popupLoader = new FXMLLoader(ManufacturersPanelController.class.getResource(
                    "/com/monolatte/kontur/SearchPopup.fxml"
            ));
            Parent root = popupLoader.load();
            SearchPopupController<Component> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.COMPONENT);
            popupController.initData();

            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);
            popupController.setStage(newStage);

            newStage.setTitle("Component Searcher");
            newStage.setResizable(false);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            Component result = popupController.getChosenObject();
            if (result != null) {
                this._manufacturerUsageDAO.addNote(new Manufacturer_Usage(
                        result.getId(),
                        currentManufacturer.getId()
                ));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this._refreshComponentLists();
    }

    @FXML
    public void onUnpinButtonClicked() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();

    }

    @FXML
    public void onManufacturerListViewMouseClicked() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) { return; }

        this.idManufacturerTextField.setText(String.valueOf(currentManufacturer.getId()));
        this.nameManufacturerTextField.setText(currentManufacturer.getName());
        this.descriptionManufacturerTextArea.setText(currentManufacturer.getDescription());
        this._refreshComponentLists();
    }

    @FXML
    public void onComponentsListViewMouseClicked() {

    }

    private ObservableList<Manufacturer> _updateMainList() {
        return FXCollections.observableList(this._manufacturerDAO.getAllNotes());
    }

    private void _refreshMainList() {
        var update = this._updateMainList();
        this.manufacturerListView.setItems(update);
    }

    private ObservableList<Component> _updateComponentList() {
        var currentManufacturer = this.manufacturerListView.getSelectionModel().getSelectedItem();
        return FXCollections.observableList(this._manufacturerUsageDAO.getComponentsByManufacturerId(
                currentManufacturer.getId()
        ));
    }

    private void _refreshComponentLists() {
        var update = this._updateComponentList();
        this.componentsListView.setItems(update);
    }
}
