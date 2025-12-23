package com.monolatte.kontur.controllers.subs;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Properties.ComponentProperty;
import com.monolatte.kontur.model.SQL.Components_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.beans.property.SimpleFloatProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class ComponentTableController {
    @FXML
    TableView<ComponentProperty> componentTableView;
    @FXML
    TableColumn<ComponentProperty, Long> idColumn;
    @FXML
    TableColumn<ComponentProperty, String> nameColumn;
    @FXML
    TableColumn<ComponentProperty, String> typeColumn;
    @FXML
    TableColumn<ComponentProperty, Float> priceColumn;
    @FXML
    TableColumn<ComponentProperty, Integer> quantityColumn;
    @FXML
    TableColumn<ComponentProperty, Float> finalCostColumn;

    private final Components_usageDAO _componentUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private final ObservableList<ComponentProperty> _masterData = FXCollections.observableArrayList();
    private long _currentId;

    @FXML
    public void initialize() {
        this._setupTableColumns();
        this.componentTableView.setItems(this._masterData);
    }

    public void setId(long id) {
        this._currentId = id;
        this._loadDataIntoTable();
    }

    private void _setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        finalCostColumn.setCellValueFactory(cellData -> {
            ComponentProperty component = cellData.getValue();
            float result = component.priceProperty().getValue() * component.quantityProperty().getValue();
            return new SimpleFloatProperty(result).asObject();
        });
    }

    private void _loadDataIntoTable() {
        this._fillComponentPropertyList(this._componentUsageDAO.getComponentsByProjectId(this._currentId));
        this.componentTableView.setItems(this._masterData);
    }

    private void _fillComponentPropertyList(List<Component> componentList) {
        this._masterData.clear();
        for (var component : componentList) {
            this._masterData.add(new ComponentProperty(component));
        }
    }
}
