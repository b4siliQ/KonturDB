package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Properties.ProjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.awt.*;

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
}
