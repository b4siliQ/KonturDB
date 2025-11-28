package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.Notes.User;
import com.monolatte.kontur.model.Notes.User_usage;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import com.monolatte.kontur.model.SQL.UserDAO;
import com.monolatte.kontur.model.SQL.User_usageDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    TextField idUserTextField;
    @FXML
    TextField nameUserTextField;
    @FXML
    TextArea descriptionUserTextField;

    private final UserDAO _userDAO = SQLTableManager.getInstance().getUserDAO();
    private final User_usageDAO _user_usageDAO = SQLTableManager.getInstance().getUserUsageDAO();

    @FXML
    public void initialize() {
        this._refreshList();
    }

    @FXML
    public void onAddEmptyButtonClicked() {
        this._userDAO.addNote(new User("new user", "new user"));
        this._refreshList();
    }

    @FXML
    public void onRemoveButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) { return; }
        this._userDAO.deleteNote(currentUser.getId());
    }

    @FXML
    public void onAddUserButtonClicked() {
        this._userDAO.addNote(new User(
                this.nameUserTextField.getText(),
                this.descriptionUserTextField.getText()
        ));
        this._refreshList();
    }

    @FXML
    public void onSaveDataButtonClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) { return; }

        currentUser.setName(this.nameUserTextField.getText());
        currentUser.setDescription(this.descriptionUserTextField.getText());

        this._userDAO.updateNote(currentUser);
        this._refreshList();
    }

    @FXML
    public void onUsersListViewMouseClicked() {
        var currentUser = this.usersListView.getSelectionModel().getSelectedItem();

        if (currentUser == null) { return; }
        this.idUserTextField.setText(String.valueOf(currentUser.getId()));
        this.nameUserTextField.setText(currentUser.getName());
        this.descriptionUserTextField.setText(currentUser.getDescription());
    }

    @FXML
    public void onProjectsListViewMouseClicked() {
        var currentProject = this.projectsListView.getSelectionModel().getSelectedItem();

    }

    private ObservableList<User> _updateList() {
        return FXCollections.observableList(this._userDAO.getAllNotes());
    }

    private void _refreshList() {
        var update = this._updateList();
        this.usersListView.setItems(update);
    }
}
