package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.SQLManager.ComponentsManager;
import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.SQLManager.SQLSuperManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ComponentsPanelController {
    @FXML
    ListView<Component> compList;
    @FXML
    Button addEmptyButton;
    @FXML
    Button removeButton;
    @FXML
    TextField idTextField;
    @FXML
    TextField nameCompTextField;
    @FXML
    TextField typeTextEdit;
    @FXML
    TextField costTextField;
    @FXML
    TextArea compInfoTextArea;
    @FXML
    TextField datasheetPathTextField;
    @FXML
    Button openDatasheetButton;
    @FXML
    Button addCompButton;
    @FXML
    Button saveDataButton;

    private final ComponentsManager _manager = SQLSuperManager.getInstance().getComponentsManager();

    @FXML
    public void initialize() {
        this._refreshList();
    }

    @FXML
    public void _onAddEmptyButtonClicked() {
        this._manager.addNote(new Component(
                "Empty",
                "Controller",
                "Specification here!",
                "www.data.ru",
                100
                ));
        this._refreshList();
    }

    @FXML
    public void _onRemoveButtonClicked() {

    }

    @FXML
    public void _onOpenDatasheetButtonClicked() {

    }

    @FXML
    public void _onAddCompButtonClicked() {

    }

    @FXML
    public void _onSaveDataButtonClicked() {

    }

    private ObservableList<Component> _updateList() {
        return FXCollections.observableList(this._manager.getAllNotes());
    }

    private void _refreshList() {
        ObservableList<Component> notes = this._updateList();
        this.compList.setItems(notes);
    }
}
