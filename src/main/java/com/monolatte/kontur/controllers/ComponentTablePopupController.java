package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Properties.ComponentProperty;
import com.monolatte.kontur.model.SQL.ComponentsDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class ComponentTablePopupController {
    @FXML
    TableView<ComponentProperty> componentsTable;
    @FXML
    TableColumn<ComponentProperty, Long> idColumn;
    @FXML
    TableColumn<ComponentProperty, String> nameColumn;
    @FXML
    TableColumn<ComponentProperty, String> typeColumn;
    @FXML
    TableColumn<ComponentProperty, String> specificationColumn;
    @FXML
    TableColumn<ComponentProperty, String> datasheetLinkColumn;
    @FXML
    TableColumn<ComponentProperty, Float> priceColumn;

    private final ComponentsDAO _componentsDAO = SQLTableManager.getInstance().getComponentsManager();
    private final List<ComponentProperty> _componentPropertyList = new ArrayList<>();

    @FXML
    public void initialize() {
        this._setupTableColumns();
        this._loadDataIntoTable();
    }

    private void _setupTableColumns() {
        this.idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject()); // asObject() для числовых типов
        this.nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        this.specificationColumn.setCellValueFactory(cellData -> cellData.getValue().specificationProperty());
        this.datasheetLinkColumn.setCellValueFactory(cellData -> cellData.getValue().datasheetLinkProperty());
        this.priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject()); // asObject()
    }

    private void _loadDataIntoTable() {
        this._fillComponentPropertyList(this._componentsDAO.getAllNotes());
        this.componentsTable.setItems(FXCollections.observableList(this._componentPropertyList));
    }

    private void _fillComponentPropertyList(List<Component> componentList) {
        this._componentPropertyList.clear();
        for (var component : componentList) {
            this._componentPropertyList.add(new ComponentProperty(component));
        }
    }
}
