package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Enums.ComponentColumns;
import com.monolatte.kontur.service.SQL.DAO.ComponentsDAO;
import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.service.SQL.SQLTableManager;
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
    @FXML Button addEmptyButton;
    @FXML Button removeButton;
    @FXML Button openInTableButton;
    @FXML Button searchButton;
    @FXML Button resetSearchButton;
    @FXML ChoiceBox<ComponentColumns> columnSorterChoiceBox;
    @FXML TextField searchTextField;
    @FXML TextField idTextField;
    @FXML TextField nameCompTextField;
    @FXML TextField typeTextEdit;
    @FXML TextField costTextField;
    @FXML TextArea compInfoTextArea;
    @FXML TextField datasheetPathTextField;
    @FXML Button openDatasheetButton;
    @FXML Button addCompButton;
    @FXML Button saveDataButton;

    private final ComponentsDAO _componentsDAO = SQLTableManager.getInstance().getComponentsManager();

    @FXML
    public void initialize() {
        this._refreshList();
        this.columnSorterChoiceBox.getItems().addAll(ComponentColumns.values());
    }

    @FXML
    public void _onAddEmptyButtonClicked() {
        this._componentsDAO.addNote(new Component(
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
        this._componentsDAO.deleteNote(currentItem.getId());
        this._refreshList();
    }

    @FXML
    public void onOpenInTableButton() {
        try {
            var popupLoader = new FXMLLoader(ComponentsPanelController.class.getResource(
                    "/com/monolatte/kontur/FXML/ComponentTablePopup.fxml"
            ));
            Parent root = popupLoader.load();

            Stage popupStage = new Stage();
            Scene popupScene = new Scene(root);

            popupStage.setScene(popupScene);

            popupStage.setTitle("Component table");
            popupStage.setResizable(false);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                    System.out.println("Datasheet opened with supported default application\n");
                } else {
                    System.err.println("Datasheet can't be opened with current desktop environment\n");
                }
            } else {
                System.err.printf("Current file on path '%s' cannot be opened. Try to check your file destination\n",
                        currentItem.getDatasheet_link());
            }
        } catch (IOException e) {
            System.err.println("An error occurred while trying to open the file");
        } catch (IllegalArgumentException e) {
            System.err.println("File could not be opened (e.g., no application registered for PDFs).");
        }
    }

    @FXML
    public void _onAddCompButtonClicked() {
        this._componentsDAO.addNote(new Component(
                this.nameCompTextField.getText(),
                this.typeTextEdit.getText(),
                this.compInfoTextArea.getText(),
                this.datasheetPathTextField.getText(),
                Float.parseFloat(this.costTextField.getText())
        ));
        this._refreshList();
    }

    @FXML
    public void _onSaveDataButtonClicked() {
        var currentItem = this.compList.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }

        currentItem.setName(this.nameCompTextField.getText());
        currentItem.setType(this.typeTextEdit.getText());
        currentItem.setSpecification(this.compInfoTextArea.getText());
        currentItem.setDatasheet_link(this.datasheetPathTextField.getText());
        currentItem.setPrice(Float.parseFloat(this.costTextField.getText()));

        this._componentsDAO.updateNote(currentItem);
        this._refreshList();
    }

    @FXML
    public void onSearchButtonClicked() {
        var foundedItems = FXCollections.observableList(this._componentsDAO.search(
                this.columnSorterChoiceBox.getValue().getDescription(),
                this.searchTextField.getText()
        ));
        this.compList.setItems(foundedItems);
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
        this.typeTextEdit.setText(currentItem.getType());
        this.costTextField.setText(String.valueOf(currentItem.getPrice()));
        this.compInfoTextArea.setText(currentItem.getSpecification());
        this.datasheetPathTextField.setText(currentItem.getDatasheet_link());

    }

    private ObservableList<Component> _updateList() {
        return FXCollections.observableList(this._componentsDAO.getAllNotes());
    }

    private void _refreshList() {
        var update = this._updateList();
        this.compList.setItems(update);
    }
}
