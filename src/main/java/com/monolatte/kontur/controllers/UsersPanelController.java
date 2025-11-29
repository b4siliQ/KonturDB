package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Enums.ProjectColumns;
import com.monolatte.kontur.model.SQL.DAOFactory;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import com.monolatte.kontur.model.SQL.UserDAO;
import com.monolatte.kontur.model.SQL.User_usageDAO;
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
    @FXML
    ListView<User> usersListView;
    @FXML
    ListView<Project> projectsListView;
    @FXML
    Button addEmptyButton;
    @FXML
    Button removeButton;
    @FXML
    Button addUserButton;
    @FXML
    Button saveDataButton;
    @FXML
    Button pinButton;
    @FXML
    Button unpinButton;
    @FXML
    TextField idUserTextField;
    @FXML
    TextField nameUserTextField;
    @FXML
    TextArea descriptionUserTextField;

    private final UserDAO _userDAO = SQLTableManager.getInstance().getUserDAO();
    private final User_usageDAO _userUsageDAO = SQLTableManager.getInstance().getUserUsageDAO();

    @FXML
    public void initialize() {
        this._refreshMainList();
    }

    @FXML
    public void onAddEmptyButtonClicked() {
        this._userDAO.addNote(new User("new user", "new user"));
        this._refreshMainList();
    }

    @FXML
    public void onRemoveButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) { return; }
        this._userDAO.deleteNote(currentUser.getId());
        this._refreshMainList();
    }

    @FXML
    public void onAddUserButtonClicked() {
        this._userDAO.addNote(new User(
                this.nameUserTextField.getText(),
                this.descriptionUserTextField.getText()
        ));
        this._refreshMainList();
    }

    @FXML
    public void onSaveDataButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) { return; }

        currentUser.setName(nameUserTextField.getText());
        currentUser.setDescription(descriptionUserTextField.getText());

        this._userDAO.updateNote(currentUser);
        this._refreshMainList();
    }

    @FXML
    public void onUsersListViewMouseClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();

        if (currentUser == null) { return; }
        this.idUserTextField.setText(String.valueOf(currentUser.getId()));
        this.nameUserTextField.setText(currentUser.getName());
        this.descriptionUserTextField.setText(currentUser.getDescription());
        this._refreshProjectList();
    }

    @FXML
    public void onPinButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) { return; }

        try {
            FXMLLoader popupLoader = new FXMLLoader(ManufacturersPanelController.class.getResource(
                    "/com/monolatte/kontur/SearchPopup.fxml"
            ));
            Parent root = popupLoader.load();
            SearchPopupController<Project> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.PROJECT);
            popupController.initData(ProjectColumns.values());

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
                this._userUsageDAO.addNote(new User_usage(
                        result.getId(),
                        currentUser.getId()
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onUnpinButtonClicked() {

    }

    @FXML
    public void onProjectsListViewMouseClicked() {
        var currentProject = this.projectsListView.getSelectionModel().getSelectedItem();

    }

    private ObservableList<User> _updateMainList() {
        return FXCollections.observableList(this._userDAO.getAllNotes());
    }

    private void _refreshMainList() {
        var update = this._updateMainList();
        this.usersListView.setItems(update);
    }

    private ObservableList<Project> _updateProjectList() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        return FXCollections.observableList(this._userUsageDAO.getProjectsByUserId(
                currentUser.getId()
        ));
    }

    private void _refreshProjectList() {
        var update = this._updateProjectList();
        this.projectsListView.setItems(update);
    }
}
