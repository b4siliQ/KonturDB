package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Enums.ComponentColumns;
import com.monolatte.kontur.model.Notes.Enums.ProjectColumns;
import com.monolatte.kontur.model.Notes.Enums.ProjectStatus;
import com.monolatte.kontur.model.SQL.*;
import com.monolatte.kontur.model.Notifyers.ErrorNotifyer;
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
    @FXML ListView<Project> projectListView;
    @FXML ListView<User> userListView;
    @FXML Button addEmptyButton;
    @FXML Button removeButton;
    @FXML Button openInTableButton;
    @FXML Button openUserButton;
    @FXML Button searchButton;
    @FXML Button resetSearchButton;
    @FXML ChoiceBox<ProjectColumns> columnSorterChoiceBox;
    @FXML TextField searchTextField;
    @FXML TextField idTextField;
    @FXML TextField nameTextField;
    @FXML TextField startDateTextField;
    @FXML TextField endDateTextField;
    @FXML ChoiceBox<ProjectStatus> statusChoiceBox;
    @FXML ListView<Component> inprojectComponentsListView;
    @FXML Button addInProjectComponentButton;
    @FXML Button changeInProjectComponentButton;
    @FXML Button removeInProjectComponentButton;
    @FXML Button addProjectButton;
    @FXML Button saveDataButton;

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
        try {
            this._projectDAO.addNote(new Project("Empty Project", "2000-01-01", "3000-01-01", "Draft"));
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to add project", e.getMessage()).apprise();
        }
    }

    @FXML
    public void removeButtonClicked() {
        var currentItem = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentItem == null) return;
        try {
            this._projectDAO.deleteNote(currentItem.getId());
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to remove project", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onOpenInTableButton() {
        try {
            var popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource("/com/monolatte/kontur/ProjectTablePopup.fxml"));
            Parent root = popupLoader.load();
            Stage popupStage = new Stage();
            Scene popupScene = new Scene(root);
            popupScene.getStylesheets().add(getClass().getResource("/com/monolatte/style/application.css").toExternalForm());
            popupStage.setScene(popupScene);
            popupStage.setTitle("Project table");
            popupStage.setResizable(false);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            new ErrorNotifyer("UI Error", "Could not load Project Table", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onOpenUserButtonClicked() {
        try {
            var popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource("/com/monolatte/kontur/UsersPopup.fxml"));
            Parent root = popupLoader.load();
            Stage popupStage = new Stage();
            Scene popupScene = new Scene(root);
            popupStage.setScene(popupScene);
            popupStage.setTitle("User window");
            popupStage.setResizable(false);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            new ErrorNotifyer("UI Error", "Could not load Users window", e.getMessage()).apprise();
        }
    }

    @FXML
    public void addInProjectComponentButtonClicked() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentProject == null) {
            new ErrorNotifyer("Selection Error", "No Project Selected", "Select a project to add components.").apprise();
            return;
        }
        try {
            FXMLLoader popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource("/com/monolatte/kontur/SearchPopup.fxml"));
            Parent root = popupLoader.load();
            SearchPopupController<Component> popupController = popupLoader.getController();
            popupController.initDAO(DAOFactory.DAOType.COMPONENT);
            popupController.initData(ComponentColumns.values());

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            popupController.setStage(newStage);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                this._componentsUsageDAO.addNote(new Component_usage(currentProject.getId(), result.getId()));
            }
        } catch (Exception e) {
            new ErrorNotifyer("Component Error", "Failed to add component usage", e.getMessage()).apprise();
        }
        this._refreshComponentList();
    }

    @FXML
    public void onChangeInProjectComponentButton() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        var currentComponent = this.inprojectComponentsListView.getSelectionModel().getSelectedItem();
        if (currentProject == null || currentComponent == null) return;

        try {
            FXMLLoader popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource("/com/monolatte/kontur/SearchPopup.fxml"));
            Parent root = popupLoader.load();
            SearchPopupController<Component> popupController = popupLoader.getController();
            popupController.initDAO(DAOFactory.DAOType.COMPONENT);
            popupController.initData(ComponentColumns.values());

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            popupController.setStage(newStage);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                var usage = new Component_usage(currentProject.getId(), result.getId());
                usage.setId(currentComponent.getId());
                this._componentsUsageDAO.updateNote(usage);
            }
        } catch (Exception e) {
            new ErrorNotifyer("Update Error", "Failed to change component", e.getMessage()).apprise();
        }
        this._refreshComponentList();
    }

    @FXML
    public void removeInProjectComponentButtonClicked() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        var currentComponent = this.inprojectComponentsListView.getSelectionModel().getSelectedItem();
        if (currentProject == null || currentComponent == null) return;

        try {
            this._componentsUsageDAO.removeComponentByProjectId(currentComponent.getId(), currentProject.getId());
            this._refreshComponentList();
        } catch (Exception e) {
            new ErrorNotifyer("Delete Error", "Failed to remove component from project", e.getMessage()).apprise();
        }
    }

    @FXML
    public void addProjectButtonClicked() {
        try {
            var currentStatus = this.statusChoiceBox.getValue();
            this._projectDAO.addNote(new Project(
                    this.nameTextField.getText(),
                    this.startDateTextField.getText(),
                    this.endDateTextField.getText(),
                    currentStatus != null ? currentStatus.getDescription() : "Draft"
            ));
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Data Error", "Failed to create project", e.getMessage()).apprise();
        }
    }

    @FXML
    public void saveDataButtonClicked() {
        var currentItem = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentItem == null) return;

        try {
            var currentStatus = this.statusChoiceBox.getValue();
            currentItem.setProject_name(this.nameTextField.getText());
            currentItem.setStatus(currentStatus != null ? currentStatus.getDescription() : currentItem.getStatus());
            currentItem.setStart_date(this.startDateTextField.getText());
            currentItem.setEnd_date(this.endDateTextField.getText());

            this._projectDAO.updateNote(currentItem);
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Update Error", "Failed to save project data", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onSearchButton() {
        try {
            var col = this.columnSorterChoiceBox.getValue();
            var searchedItem = FXCollections.observableList(this._projectDAO.search(
                    col != null ? col.getDescription() : "",
                    this.searchTextField.getText()
            ));
            this.projectListView.setItems(searchedItem);
        } catch (Exception e) {
            new ErrorNotifyer("Search Error", "Search failed", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onResetSearchButton() {
        this.searchTextField.setText("");
        this._refreshMainList();
    }

    @FXML
    public void _onProjectListViewMouseClicked() {
        var currentItem = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentItem == null) return;

        this.idTextField.setText(String.valueOf(currentItem.getId()));
        this.nameTextField.setText(currentItem.getProject_name());
        this.statusChoiceBox.setValue(ProjectStatus.getByDescription(currentItem.getStatus()));
        this.startDateTextField.setText(currentItem.getStart_date());
        this.endDateTextField.setText(currentItem.getEnd_date());
        this._refreshComponentList();
        this._refreshUserList();
    }

    private void _refreshMainList() {
        this.projectListView.setItems(FXCollections.observableList(this._projectDAO.getAllNotes()));
    }

    private void _refreshComponentList() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentProject == null) return;
        this.inprojectComponentsListView.setItems(FXCollections.observableList(this._componentsUsageDAO.getComponentsByProjectId(currentProject.getId())));
    }

    private void _refreshUserList() {
        var currentProject = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentProject == null) return;
        this.userListView.setItems(FXCollections.observableList(this._userUsageDAO.getUsersByProjectId(currentProject.getId())));
    }
}