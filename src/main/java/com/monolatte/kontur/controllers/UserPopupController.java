package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Enums.ProjectColumns;
import com.monolatte.kontur.model.Notes.Enums.UserColumns;
import com.monolatte.kontur.model.Notes.Properties.ProjectProperty;
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
import java.util.List;

public class UserPopupController {
    @FXML
    Label nameLabel;
    @FXML
    TextArea descriptionTextArea;
    @FXML
    TextField contactTextField;
    @FXML
    TextField contactTypeTextField;
    @FXML
    Button showUserButton;
    @FXML
    TableView<ProjectProperty> projectTable;
    @FXML
    TableColumn<ProjectProperty, Long> idTableColumn;
    @FXML
    TableColumn<ProjectProperty, String> projectNameTableColumn;
    @FXML
    TableColumn<ProjectProperty, String> startDateTableColumn;
    @FXML
    TableColumn<ProjectProperty, String> endDateTableColumn;
    @FXML
    TableColumn<ProjectProperty, String> statusTableColumn;
    @FXML
    TableColumn<ProjectProperty, Integer> qantityTableColumn;
    @FXML
    TableColumn<ProjectProperty, Float> totalPriceTableColumn;

    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final UserContactDAO _userContactDAO = SQLTableManager.getInstance().getUserContactDAO();
    private final User_usageDAO _userUsageDAO = SQLTableManager.getInstance().getUserUsageDAO();
    private final ObservableList<ProjectProperty> _masterData = FXCollections.observableArrayList();
    private User _currentUser;
    private UserContact _currentUserContact;

    @FXML
    public void initialize() {
        this._setupTableColumns();
    }

    @FXML
    public void onShowUserButtonClicked() {
        try {
            var popupLoader = new FXMLLoader(UserPopupController.class.getResource(
                    "/com/monolatte/kontur/SearchPopup.fxml"
            ));
            Parent root = popupLoader.load();
            SearchPopupController<User> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.USER);
            popupController.initData(UserColumns.values());

            var newStage = new Stage();
            var newScene = new Scene(root);
            newStage.setScene(newScene);
            popupController.setStage(newStage);

            newStage.setTitle("User Searcher");
            newStage.setResizable(false);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                this._currentUser = result;

                this._currentUserContact = this._userContactDAO.getUserContactByUserId(this._currentUser.getId());

                this._loadDataIntoTable();
                this._refillUserFields();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void _refillUserFields() {
        this.nameLabel.setText(this._currentUser.getName());
        this.descriptionTextArea.setText(this._currentUser.getDescription());
        if (this._currentUserContact != null) {
            this.contactTypeTextField.setText(this._currentUserContact.getContact_type());
            this.contactTextField.setText(this._currentUserContact.getContact_value());
        } else {
            this.contactTypeTextField.setText("Не указано");
            this.contactTextField.setText("");
        }
    }

    private void _setupTableColumns() {
        this.idTableColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        this.projectNameTableColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.startDateTableColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
        this.endDateTableColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
        this.statusTableColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        this.qantityTableColumn.setCellValueFactory(cellData -> cellData.getValue().componentsQuantityProperty().asObject());
        this.totalPriceTableColumn.setCellValueFactory(cellData -> cellData.getValue().totalComponentPriceProperty().asObject());
    }

    private void _loadDataIntoTable() {
        // 1. Проверяем, не пустой ли список данных
        var notes = this._userUsageDAO.getProjectsByUserId(this._currentUser.getId());
        if (notes == null) {
            System.err.println("DAO вернул null вместо списка проектов");
            return;
        }

        this._fillProjectPropertyList(notes);

        // 2. ГЛАВНАЯ ПРОВЕРКА
        if (this.projectTable == null) {
            System.err.println("!!! АХТУНГ !!! projectsTable == null. " +
                    "Это значит, что метод вызван в контроллере, который не привязан к активному FXML с таблицей.");
            return;
        }

        this.projectTable.setItems(this._masterData);
    }

    private void _fillProjectPropertyList(List<Project> projectList) {
        this._masterData.clear();
        for (var project : projectList) {
            this._masterData.add(new ProjectProperty(project));
        }
    }

}
