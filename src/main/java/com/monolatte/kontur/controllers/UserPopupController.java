package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Enums.ProjectColumns;
import com.monolatte.kontur.model.Notes.Enums.UserColumns;
import com.monolatte.kontur.model.Notes.Enums.ComponentColumns;
import com.monolatte.kontur.model.Notes.Properties.ProjectProperty;
import com.monolatte.kontur.model.SQL.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    private Label nameLabel;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField contactTextField;
    @FXML
    private TextField contactTypeTextField;
    @FXML
    private Button showUserButton;
    @FXML
    private Button addUserButton;
    @FXML
    private Button addProjectButton;
    @FXML
    private Button removeProjectButton;
    @FXML
    TableView<ProjectProperty> projectsTable;
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
    private final UserDAO _userDAO = SQLTableManager.getInstance().getUserDAO();
    private final User_usageDAO _userUsageDAO = SQLTableManager.getInstance().getUserUsageDAO();
    private final ObservableList<ProjectProperty> _masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this._setupTableColumns();
        this._loadDataIntoTable();
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
        this._fillProjectPropertyList(this._projectDAO.getAllNotes());
        this.projectsTable.setItems(this._masterData);
    }

    @FXML
    public void onShowUserButtonClicked() {

    }

    @FXML
    public void onAddUserButtonClicked() {
//        try {
//            FXMLLoader popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource(
//                    "/com/monolatte/kontur/SearchPopup.fxml"
//            ));
//            Parent root = popupLoader.load();
//            SearchPopupController<User> popupController = popupLoader.getController();
//
//            popupController.initDAO(DAOFactory.DAOType.USER);
//            popupController.initData(UserColumns.values());
//
//            Stage newStage = new Stage();
//            Scene newScene = new Scene(root);
//            newStage.setScene(newScene);
//            popupController.setStage(newStage);
//
//            newStage.setTitle("User Searcher");
//            newStage.setResizable(false);
//            newStage.initModality(Modality.APPLICATION_MODAL);
//            newStage.showAndWait();
//
//            var result = popupController.getChosenObject();
//            if (result != null) {
//                this._userUsageDAO.addNote(new User_usage(
//                        currentProject.getId(),
//                        result.getId()
//                ));
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        this._refreshComponentList();
    }

    @FXML
    public void onAddProjectButtonClicked() {
        var currentUser = this.projectListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) { return; }

        try {
            FXMLLoader popupLoader = new FXMLLoader(ProjectsPanelController.class.getResource(
                    "/com/monolatte/kontur/SearchPopup.fxml"
            ));
            Parent root = popupLoader.load();
            SearchPopupController<Component> popupController = popupLoader.getController();

            popupController.initDAO(DAOFactory.DAOType.PROJECT);
            popupController.initData(ProjectColumns.values());

            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);
            popupController.setStage(newStage);

            newStage.setTitle("Project Searcher");
            newStage.setResizable(false);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            var result = popupController.getChosenObject();
            if (result != null) {
                this._userUsageDAO.addNote(new Component_usage(
                        currentUser.getId(),
                        result.getId()
                ));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this._refreshComponentList();
    }

    @FXML
    public void onRemoveProjectButtonClicked() {

    }

    private void _fillProjectPropertyList(List<Project> projectList) {
        this._masterData.clear();
        for (var project : projectList) {
            this._masterData.add(new ProjectProperty(project));
        }
    }

}
