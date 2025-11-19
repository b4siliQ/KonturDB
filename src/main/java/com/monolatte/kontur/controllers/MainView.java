package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.ElectComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class MainView {
    @FXML
    private Button _addEmptyButton;
    @FXML
    private Button _removeButton;
    @FXML
    private Button _openDatasheetButton;
    @FXML
    private Button _addCompButton;
    @FXML
    private Button _saveDataButton;
    @FXML
    private ListView<ElectComponent> _compList;
    @FXML
    private TextField _idTextField;
    @FXML
    private TextField _nameCompTextField;
    @FXML
    private TextField _typeTextEdit;
    @FXML
    private TextField _costTextField;
    @FXML
    private TextField _datasheetPathTextField;
    @FXML
    private TextArea _compInfoTextArea;

    private ObservableList<ElectComponent> _components;

    // TODO Покумекать над выбором объекта из списка
    private ElectComponent _selectedItem;
    private MultipleSelectionModel<ElectComponent> _selectionModel;

    public void initialize() throws IOException {
        this._components = FXCollections.observableArrayList();
        this._compList.setItems(this._components);
    }

    @FXML
    private void _onAddEmptyButtonClicked() {
        this._components.add(new ElectComponent("Нуль", "Пустышка", 100, "Пустое пространство", "path://"));
    }

    @FXML
    private void _onRemoveButtonClicked() {

    }

    @FXML
    private void _onOpenDatasheetButtonClicked() {}

    @FXML
    private void _onAddCompButtonClicked() {
        this._components.add(new ElectComponent(
                this._nameCompTextField.getText(),
                this._typeTextEdit.getText(),
                Integer.parseInt(this._costTextField.getText()),
                this._compInfoTextArea.getText(),
                this._datasheetPathTextField.getText()
        ));
        this._clearTextFields();
    }

    @FXML
    private void _onSaveDataButtonClicked() {}

    private void _clearTextFields() {
        this._nameCompTextField.setText("");
        this._typeTextEdit.setText("");
        this._costTextField.setText("");
        this._compInfoTextArea.setText("");
        this._datasheetPathTextField.setText("");
    }

}
