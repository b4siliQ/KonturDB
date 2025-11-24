package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.SQLManager.Components_usageManager;
import com.monolatte.kontur.model.SQLManager.Notes.Component_usage;
import com.monolatte.kontur.model.SQLManager.Notes.Project;
import com.monolatte.kontur.model.SQLManager.ProjectManager;
import com.monolatte.kontur.model.SQLManager.SQLSuperManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

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

    private final ProjectManager projectManager = SQLSuperManager.getInstance().getProjectManager();
    private final Components_usageManager usageManager = SQLSuperManager.getInstance().getComponentsUsageManager();

    private Project selectedProject = null;

    @FXML
    public void initialize() {
    loadProjects();

    projectListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showProjectDetails(newSelection);
                } else {
                    clearProjectDetails();
                }
            }
    );
    //TODO: Инициализация statusChoiceBox

    }

    private void loadProjects() {
        try {
            projectListView.getItems().setAll(projectManager.getAllNotes());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void showProjectDetails(Project project) {
        idTextField.setText(String.valueOf(project.getId()));
        nameTextField.setText(project.getProject_name());
        startDateTextField.setText(project.getStart_date());
        endDateTextField.setText(project.getEnd_date());

        loadComponents(project.getId());
    }

    private void clearProjectDetails() {
        idTextField.clear();
        nameTextField.clear();
        startDateTextField.clear();
        endDateTextField.clear();
        // TODO: Очистить statusChoiceBox
        inprojectComponentsListView.getItems().clear();
    }

    private void loadComponents(int projectId) {
        try {
            List<Component_usage> usage = usageManager.getUsageByProjectId(projectId);
            inprojectComponentsListView.getItems().setAll(usage);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @FXML
    public void addEmptyProject() {
        Project newProject = new Project(0, "Новый проект", "YYYY-MM-DD", "YYYY-MM-DD", "Draft");

        projectListView.getItems().add(0, newProject);
        projectListView.getSelectionModel().select(0);
        nameTextField.requestFocus();
    }

    @FXML
    public void saveProjectData() {
        if (this.selectedProject == null) return;

        try {
            int id = idTextField.getText().isEmpty() ? 0 : Integer.parseInt(idTextField.getText());

            this.selectedProject.setId(id);
            this.selectedProject.setProject_name(nameTextField.getText());
            this.selectedProject.setStart_date(startDateTextField.getText());
            this.selectedProject.setEnd_date(endDateTextField.getText());
            //this.selectedProject.setStatus(statusChoiceBox.getValue());

            if (this.selectedProject.getId() == 0) {
                projectManager.addNote(this.selectedProject);
            } else {
                projectManager.updateNote(this.selectedProject);
            }

            loadProjects();
            projectListView.getSelectionModel().select(this.selectedProject);
        } catch (NumberFormatException e) {
            // Ошибка, если ID не число
            System.err.println("Ошибка ID: " + e.getMessage());
        } catch (RuntimeException e) {
            // Ошибка SQL
            System.err.println("Ошибка сохранения данных: " + e.getMessage());
        }
    }

    @FXML
    public void removeProject() {
        Project projectToDelete = projectListView.getSelectionModel().getSelectedItem();

        if (projectToDelete != null && projectToDelete.getId() > 0) {
            try {
                projectManager.deleteNote(projectToDelete.getId());
                projectListView.getItems().remove(projectToDelete);
                clearProjectDetails();
            } catch (RuntimeException e) {
                System.err.println("Ошибка удаления: " + e.getMessage());
            }
        }
    }
}
