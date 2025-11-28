package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Manufacturer;
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

    @FXML
    public void onAddEmptyButtonClicked() {

    }

    @FXML
    public void onRemoveButtonClicked() {

    }

    @FXML
    public void onAddManufacturerButtonClicked() {

    }

    @FXML
    public void onSaveDataButton() {

    }

    @FXML
    public void onManufacturerListViewMouseClicked() {

    }

    @FXML
    public void onComponentsListViewMouseClicked() {

    }
}
