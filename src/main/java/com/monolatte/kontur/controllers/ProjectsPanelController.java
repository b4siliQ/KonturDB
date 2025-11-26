package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Enums.ProjectStatus;
import com.monolatte.kontur.model.SQL.Components_usageDAO;
import com.monolatte.kontur.model.Notes.Component_usage;
import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.SQL.ProjectDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProjectsPanelController {
    @FXML
    ListView<Project> projectListView;
    @FXML
    Button addEmptyButton;
    @FXML
    Button removeButton;
    @FXML
    TextField idTextField;
    @FXML
    TextField nameTextField;
    @FXML
    ChoiceBox<String> userChoiceBox;
    @FXML
    TextField startDateTextField;
    @FXML
    TextField endDateTextField;
    @FXML
    ChoiceBox<ProjectStatus> statusChoiceBox;
    @FXML
    ListView<Component_usage> inprojectComponentsListView;
    @FXML
    Button addInProjectComponentButton;
    @FXML
    Button removeInProjectComponentButton;
    @FXML
    Button addProjectButton;
    @FXML
    Button saveDataButton;

    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final Components_usageDAO _componentsUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();

    @FXML
    public void initialize() {
        this._refreshMainList();
        this.statusChoiceBox.getItems().addAll(ProjectStatus.values());
    }

    @FXML
    public void addEmptyButtonClicked() {
        this._projectDAO.addNote(new Project(
                "Empty Project",
                "2000-01-01",
                "3000-01-01",
                "Draft"
        ));
        this._refreshMainList();
    }

    @FXML
    public void removeButtonClicked() {
        var currentItem = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }
        this._projectDAO.deleteNote(currentItem.getId());
        this._refreshMainList();
    }

    @FXML
    public void addInProjectComponentButtonClicked() {

    }

    @FXML
    public void removeInProjectComponentButtonClicked() {

    }

    @FXML
    public void addProjectButtonClicked() {
        var currentStatus = this.statusChoiceBox.getValue();
        this._projectDAO.addNote(new Project(
                this.nameTextField.getText(),
                this.startDateTextField.getText(),
                this.endDateTextField.getText(),
                currentStatus.getDescription()
        ));
        this._refreshMainList();
    }

    @FXML
    public void saveDataButtonClicked() {
        var currentItem = this.projectListView.getSelectionModel().getSelectedItem();
        var currentStatus = this.statusChoiceBox.getValue();
        if (currentItem == null) { return; }

        currentItem.setProject_name(this.nameTextField.getText());
        currentItem.setStatus(currentStatus.getDescription());
        currentItem.setStart_date(this.startDateTextField.getText());
        currentItem.setEnd_date(this.endDateTextField.getText());

        this._projectDAO.updateNote(currentItem);
        this._refreshMainList();
    }

    @FXML
    public void _onProjectListViewMouseClicked() {
        var currentItem = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentItem == null) { return; }

        this.idTextField.setText(String.valueOf(currentItem.getId()));
        this.nameTextField.setText(currentItem.getProject_name());
        this.statusChoiceBox.setValue(ProjectStatus.getByDescription(currentItem.getStatus()));
        this.startDateTextField.setText(currentItem.getStart_date());
        this.endDateTextField.setText(currentItem.getEnd_date());
    }

    private ObservableList<Project> _updateMainList() {
        return FXCollections.observableList(this._projectDAO.getAllNotes());
    }

    private void _refreshMainList() {
        var update = this._updateMainList();
        this.projectListView.setItems(update);
    }
}