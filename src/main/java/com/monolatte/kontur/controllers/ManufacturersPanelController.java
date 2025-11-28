package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Manufacturer;
import com.monolatte.kontur.model.Notes.Manufacturer_Usage;
import com.monolatte.kontur.model.Notes.User;
import com.monolatte.kontur.model.SQL.ManufacturerDAO;
import com.monolatte.kontur.model.SQL.Manufacturer_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    TextField idManufacturerTextField;
    @FXML
    TextField nameManufacturerTextField;
    @FXML
    TextArea descriptionManufacturerTextArea;

    private final ManufacturerDAO _manufacturerDAO = SQLTableManager.getInstance().getManufacturerDAO();
    private final Manufacturer_usageDAO _manufacturerUsageDAO = SQLTableManager.getInstance().getManufacturerUsageDAO();

    @FXML
    public void initialize() {
        this._refreshList();
    }

    @FXML
    public void onAddEmptyButtonClicked() {
        this._manufacturerDAO.addNote(new Manufacturer("new manufacture", "new manufacture"));
        this._refreshList();
    }

    @FXML
    public void onRemoveButtonClicked() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) { return; }
        this._manufacturerDAO.deleteNote(currentManufacturer.getId());
        this._refreshList();
    }

    @FXML
    public void onAddManufacturerButtonClicked() {
        this._manufacturerDAO.addNote(new Manufacturer(
                this.nameManufacturerTextField.getText(),
                this.descriptionManufacturerTextArea.getText()
        ));
        this._refreshList();
    }

    @FXML
    public void onSaveDataButton() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) { return; }

        currentManufacturer.setName(nameManufacturerTextField.getText());
        currentManufacturer.setDescription(descriptionManufacturerTextArea.getText());

        this._manufacturerDAO.updateNote(currentManufacturer);
        this._refreshList();
    }

    @FXML
    public void onManufacturerListViewMouseClicked() {
        var currentManufacturer = manufacturerListView.getSelectionModel().getSelectedItem();
        if (currentManufacturer == null) { return; }

        this.idManufacturerTextField.setText(String.valueOf(currentManufacturer.getId()));
        this.nameManufacturerTextField.setText(currentManufacturer.getName());
        this.descriptionManufacturerTextArea.setText(currentManufacturer.getDescription());
    }

    @FXML
    public void onComponentsListViewMouseClicked() {

    }

    private ObservableList<Manufacturer> _updateList() {
        return FXCollections.observableList(this._manufacturerDAO.getAllNotes());
    }

    private void _refreshList() {
        var update = this._updateList();
        this.manufacturerListView.setItems(update);
    }
}
