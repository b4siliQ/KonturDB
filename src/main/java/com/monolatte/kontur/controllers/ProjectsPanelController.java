package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Enums.ComponentColumns;
import com.monolatte.kontur.model.Notes.Enums.ProjectColumns;
import com.monolatte.kontur.model.Notes.Enums.ProjectStatus;
import com.monolatte.kontur.model.SQL.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ProjectsPanelController {
    @FXML
    ListView<Project> projectListView;
    @FXML
    ListView<User> userListView;
    @FXML
    Button addEmptyButton;
    @FXML
    Button removeButton;
    @FXML
    Button openInTableButton;
    @FXML
    Button searchButton;
    @FXML
    Button resetSearchButton;
    @FXML
    ChoiceBox<ProjectColumns> columnSorterChoiceBox;
    @FXML
    TextField searchTextField;
    @FXML
    TextField idTextField;
    @FXML
    TextField nameTextField;
    @FXML
    TextField startDateTextField;
    @FXML
    TextField endDateTextField;
    @FXML
    ChoiceBox<ProjectStatus> statusChoiceBox;
    @FXML
    ListView<Component> inprojectComponentsListView;
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
    private final User_usageDAO _userUsageDAO = SQLTableManager.getInstance().getUserUsageDAO();

    @FXML
    public void initialize() {
        this._refreshMainList();
        this.statusChoiceBox.getItems().addAll(ProjectStatus.values());
        this.columnSorterChoiceBox.getItems().addAll(ProjectColumns.values());
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
    public void onOpenInTableButton() {
        try {
            var popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource(
                    "/com/monolatte/kontur/ProjectTablePopup.fxml"
            ));
            Parent root = popupLoader.load();

            Stage popupStage = new Stage();
            Scene popupScene = new Scene(root);

            popupStage.setScene(popupScene);

            popupStage.setTitle("Project table");
            popupStage.setResizable(false);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void addInProjectComponentButtonClicked() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentProject == null) { return; }

        try {
            FXMLLoader popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource(
                    "/com/monolatte/kontur/SearchPopup.fxml"
            ));
            Parent root = popupLoader.load();
            SearchPopupController<Component> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.COMPONENT);
            popupController.initData(ComponentColumns.values());

            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);
            popupController.setStage(newStage);

            newStage.setTitle("Component Searcher");
            newStage.setResizable(false);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                this._componentsUsageDAO.addNote(new Component_usage(
                        currentProject.getId(),
                        result.getId()
                ));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this._refreshComponentList();
    }

    @FXML
    public void removeInProjectComponentButtonClicked() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        var currentComponent = this.inprojectComponentsListView.getSelectionModel().getSelectedItem();
        if (currentProject == null && currentComponent == null) { return; }

        this._componentsUsageDAO.removeComponentByProjectId(currentComponent.getId(), currentProject.getId());
        this._refreshComponentList();
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
    public void onSearchButton() {
        var searchedItem = FXCollections.observableList(this._projectDAO.search(
                this.columnSorterChoiceBox.getValue().getDescription(),
                this.searchTextField.getText()
        ));
        this.projectListView.setItems(searchedItem);
    }

    @FXML
    public void onResetSearchButton() {
        this.searchTextField.setText("");
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
        this._refreshComponentList();
        this._refreshUserList();
    }

    private ObservableList<Project> _updateMainList() {
        return FXCollections.observableList(this._projectDAO.getAllNotes());
    }

    private void _refreshMainList() {
        var update = this._updateMainList();
        this.projectListView.setItems(update);
    }

    private ObservableList<Component> _updateComponentList() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        return FXCollections.observableList(this._componentsUsageDAO.getComponentsByProjectId(
                currentProject.getId()
        ));
    }

    private void _refreshComponentList() {
        var update = this._updateComponentList();
        this.inprojectComponentsListView.setItems(update);
    }

    private ObservableList<User> _updateUserList() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        return FXCollections.observableList(this._userUsageDAO.getUsersByProjectId(
                currentProject.getId()
        ));
    }

    private void _refreshUserList() {
        var update = this._updateUserList();
        this.userListView.setItems(update);
    }
}