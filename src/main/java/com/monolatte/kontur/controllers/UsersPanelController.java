package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Enums.ProjectColumns;
import com.monolatte.kontur.model.SQL.DAOFactory;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import com.monolatte.kontur.model.SQL.UserDAO;
import com.monolatte.kontur.model.SQL.User_usageDAO;
import com.monolatte.kontur.model.Notifyers.ErrorNotifyer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class UsersPanelController {
    @FXML ListView<User> usersListView;
    @FXML ListView<Project> projectsListView;
    @FXML Button addEmptyButton;
    @FXML Button removeButton;
    @FXML Button addUserButton;
    @FXML Button saveDataButton;
    @FXML Button pinButton;
    @FXML Button unpinButton;
    @FXML TextField idUserTextField;
    @FXML TextField nameUserTextField;
    @FXML TextArea descriptionUserTextField;

    private final UserDAO _userDAO = SQLTableManager.getInstance().getUserDAO();
    private final User_usageDAO _userUsageDAO = SQLTableManager.getInstance().getUserUsageDAO();

    @FXML
    public void initialize() {
        this._refreshMainList();
    }

    @FXML
    public void onAddEmptyButtonClicked() {
        try {
            this._userDAO.addNote(new User("new user", "new user"));
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to add empty user", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onRemoveButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        try {
            this._userDAO.deleteNote(currentUser.getId());
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Database Error", "Failed to remove user", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onAddUserButtonClicked() {
        try {
            this._userDAO.addNote(new User(
                    this.nameUserTextField.getText(),
                    this.descriptionUserTextField.getText()
            ));
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Data Error", "Failed to create user", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onSaveDataButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;

        try {
            currentUser.setName(nameUserTextField.getText());
            currentUser.setDescription(descriptionUserTextField.getText());
            this._userDAO.updateNote(currentUser);
            this._refreshMainList();
        } catch (Exception e) {
            new ErrorNotifyer("Update Error", "Failed to update user data", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onUsersListViewMouseClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;

        this.idUserTextField.setText(String.valueOf(currentUser.getId()));
        this.nameUserTextField.setText(currentUser.getName());
        this.descriptionUserTextField.setText(currentUser.getDescription());
        this._refreshProjectList();
    }

    @FXML
    public void onPinButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) {
            new ErrorNotifyer("Selection Error", "No User Selected", "Please select a user to pin a project.").apprise();
            return;
        }

        try {
            FXMLLoader popupLoader = new FXMLLoader(getClass().getResource("/com/monolatte/kontur/SearchPopup.fxml"));
            Parent root = popupLoader.load();
            SearchPopupController<Project> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.PROJECT);
            popupController.initData(ProjectColumns.values());

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            popupController.setStage(newStage);
            newStage.setTitle("Project Searcher");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                this._userUsageDAO.addNote(new User_usage(result.getId(), currentUser.getId()));
                this._refreshProjectList();
            }
        } catch (IOException e) {
            new ErrorNotifyer("UI Error", "Could not load Search Popup", e.getMessage()).apprise();
        } catch (Exception e) {
            new ErrorNotifyer("Link Error", "Failed to pin project to user", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onUnpinButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        var currentProject = this.projectsListView.getSelectionModel().getSelectedItem();
        if (currentUser == null || currentProject == null) return;

        try {
            this._userUsageDAO.removeUserFromProject(currentUser.getId(), currentProject.getId());
            this._refreshProjectList();
        } catch (Exception e) {
            new ErrorNotifyer("Link Error", "Failed to unpin project", e.getMessage()).apprise();
        }
    }

    @FXML
    public void onProjectsListViewMouseClicked() {
        // Логика при клике на проект пользователя, если потребуется
    }

    private void _refreshMainList() {
        this.usersListView.setItems(FXCollections.observableList(this._userDAO.getAllNotes()));
    }

    private void _refreshProjectList() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        this.projectsListView.setItems(FXCollections.observableList(this._userUsageDAO.getProjectsByUserId(currentUser.getId())));
    }
}