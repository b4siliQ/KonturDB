package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.Notes.Properties.ProjectProperty;
import com.monolatte.kontur.model.SQL.ProjectDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class ProjectTablePopupController {
    @FXML
    TableView<ProjectProperty> projectsTable;
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

    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final List<ProjectProperty> _projectPropertyList = new ArrayList<>();

    @FXML
    public void initialize() {
        this._setupTableColumns();
        this._loadDataIntoTable();
    }

    private void _setupTableColumns() {
        this.idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        this.nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.startDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
        this.endDateColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
        this.statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void _loadDataIntoTable() {
        this._fillProjectPropertyList(this._projectDAO.getAllNotes());
        this.projectsTable.setItems(FXCollections.observableList(this._projectPropertyList));
    }

    private void _fillProjectPropertyList(List<Project> projectList) {
        this._projectPropertyList.clear();
        for (var project : projectList) {
            this._projectPropertyList.add(new ProjectProperty(project));
        }
    }
}
