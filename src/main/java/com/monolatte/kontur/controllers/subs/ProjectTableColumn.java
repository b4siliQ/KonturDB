package com.monolatte.kontur.controllers.subs;

import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.Notes.Properties.ProjectProperty;
import com.monolatte.kontur.model.SQL.Components_usageDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class ProjectTableColumn {
    @FXML
    TableView<ProjectProperty> projectTableView;
    @FXML
    TableColumn<ProjectProperty, Long> idColumn;
    @FXML
    TableColumn<ProjectProperty, String> nameColumn;
    @FXML
    TableColumn<ProjectProperty, String> startDateColumn;
    @FXML
    TableColumn<ProjectProperty, String> endDateColumn;
    @FXML
    TableColumn<ProjectProperty, String> statusColumn;
    @FXML
    TableColumn<ProjectProperty, Integer> componentsQuantityColumn;
    @FXML
    TableColumn<ProjectProperty, Float> totalCostColumn;

    private final Components_usageDAO _componentUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private final ObservableList<ProjectProperty> _masterData = FXCollections.observableArrayList();
    private long _currentId;

    @FXML
    public void initialize() {
        this._setupTableColumns();
        this.projectTableView.setItems(this._masterData);
    }

    public void setId(long id) {
        this._currentId = id;
        this._loadDataIntoTable();
    }

    private void _setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        startDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
        endDateColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        componentsQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().componentsQuantityProperty().asObject());
        totalCostColumn.setCellValueFactory(cellData -> cellData.getValue().totalComponentPriceProperty().asObject());
    }

    private void _loadDataIntoTable() {
        this._fillProjectPropertyList(this._componentUsageDAO.getProjectsByComponentId(this._currentId));
        this.projectTableView.setItems(this._masterData);
    }

    private void _fillProjectPropertyList(List<Project> projectList) {
        this._masterData.clear();
        for (var project : projectList) {
            this._masterData.add(new ProjectProperty(project));
        }
    }
}
