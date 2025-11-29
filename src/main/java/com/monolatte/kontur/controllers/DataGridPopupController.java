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

public class DataGridPopupController {
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
        setupTableColumns();
        loadDataIntoTable();
    }

    private void setupTableColumns() {
        // Привязка свойств (Property) класса Component к столбцам TableView
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject()); // asObject() для числовых типов
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        specificationColumn.setCellValueFactory(cellData -> cellData.getValue().specificationProperty());
        datasheetLinkColumn.setCellValueFactory(cellData -> cellData.getValue().datasheetLinkProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject()); // asObject()
    }

    public void loadDataIntoTable() {
        // Получаем List<Component> из DAO
        this._fillComponentPropertyList(this._componentsDAO.getAllNotes());
        componentsTable.setItems(FXCollections.observableList(this._componentPropertyList));
    }

    private void _fillComponentPropertyList(List<Component> componentList) {
        this._componentPropertyList.clear();
        for (var component : componentList) {
            this._componentPropertyList.add(new ComponentProperty(component));
        }
    }
}
