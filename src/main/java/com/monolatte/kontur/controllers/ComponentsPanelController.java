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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

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
                "Empty Name",
                "Controller Type",
                "Enter your specification here!",
                "Enter your datasheet link here!",
                100
        ));
        this._refreshList();
    }

    @FXML
    public void _onRemoveButtonClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        this._manager.deleteNote(currentItem.getId());
        this._refreshList();
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
                    System.out.println("Datasheet opened with supported default application\n");
                } else {
                    System.err.println("Datasheet can't be opened with current desktop environment\n");
                }
            } else {
                System.err.printf("Current file on path '%s' cannot be opened. Try to check your file destination\n",
                        currentItem.getDatasheet_link());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("An error occurred while trying to open the file");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("File could not be opened (e.g., no application registered for PDFs).");
        }
    }

    @FXML
    public void _onAddCompButtonClicked() {
        this._manager.addNote(new Component(
                this.nameCompTextField.getText(),
                this.typeTextEdit.getText(),
                this.compInfoTextArea.getText(),
                this.datasheetPathTextField.getText(),
                Integer.parseInt(this.costTextField.getText())
        ));
        this._refreshList();
    }

    // ! Исправить проблему с отказом сохранения данных
    @FXML
    public void _onSaveDataButtonClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        this._manager.updateNote(currentItem);
        this._refreshList();
    }

    @FXML
    public void onCompListMouseClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        this.idTextField.setText(String.valueOf(currentItem.getId()));
        this.nameCompTextField.setText(currentItem.getName());
        this.typeTextEdit.setText(currentItem.getType());
        this.costTextField.setText(String.valueOf(currentItem.getPrice()));
        this.compInfoTextArea.setText(currentItem.getSpecification());
        this.datasheetPathTextField.setText(currentItem.getDatasheet_link());
    }

    private ObservableList<Component> _updateList() {
        return FXCollections.observableList(this._manager.getAllNotes());
    }

    private void _refreshList() {
        ObservableList<Component> notes = this._updateList();
        this.compList.setItems(notes);
    }
}
