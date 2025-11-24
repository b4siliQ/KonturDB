package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.SQLManager.Notes.Component_usage;
import com.monolatte.kontur.model.SQLManager.Notes.Project;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

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
    TextField startDateTextField;
    @FXML
    TextField endDateTextField;
    // TODO: Добавить управление элемента для statusChoiceBox
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
}
