package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.SQLManager.Components_usageManager;
import com.monolatte.kontur.model.Notes.Component_usage;
import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.SQLManager.ProjectManager;
import com.monolatte.kontur.model.SQLManager.SQLSuperManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    @FXML
    ChoiceBox<String> statusChoiceBox;
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
        statusChoiceBox.getItems().addAll("Draft", "In Progress", "Completed", "Canceled");

        loadProjects();

        projectListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showProjectDetails(newSelection);
                        this.selectedProject = newSelection;
                    } else {
                        clearProjectDetails();
                        this.selectedProject = null;
                    }
                }
        );
    }

    private void loadProjects() {
        try {
            projectListView.getItems().setAll(projectManager.getAllNotes());
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке проектов: " + e.getMessage());
        }
    }

    private void showProjectDetails(Project project) {
        idTextField.setText(String.valueOf(project.getId()));
        nameTextField.setText(project.getProject_name());
        startDateTextField.setText(project.getStart_date());
        endDateTextField.setText(project.getEnd_date());
        statusChoiceBox.setValue(project.getStatus());

        loadComponents(project.getId());
    }

    private void clearProjectDetails() {
        idTextField.clear();
        nameTextField.clear();
        startDateTextField.clear();
        endDateTextField.clear();
        statusChoiceBox.setValue(null);
        inprojectComponentsListView.getItems().clear();
    }

    private void loadComponents(int projectId) {
        try {
            List<Component_usage> usage = usageManager.getUsageByProjectId(projectId);
            inprojectComponentsListView.getItems().setAll(usage);
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке компонентов проекта: " + e.getMessage());
        }
    }

    private Project getProjectFromFields(int currentId) {
        String name = nameTextField.getText();
        String startDate = startDateTextField.getText();
        String endDate = endDateTextField.getText();
        String status = statusChoiceBox.getValue();

        if (name == null || name.trim().isEmpty() || startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty() || status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Все поля проекта должны быть заполнены.");
        }

        return new Project(currentId, name, startDate, endDate, status);
    }

    @FXML
    public void addEmptyButtonClicked() {
        Project newProject = new Project(0, "Новый проект", "YYYY-MM-DD", "YYYY-MM-DD", "Draft");

        projectListView.getItems().add(0, newProject);
        projectListView.getSelectionModel().select(0);
        nameTextField.requestFocus();
    }

    @FXML
    public void removeButtonClicked() {
        Project projectToRemove = projectListView.getSelectionModel().getSelectedItem();
        if (projectToRemove != null) {
            if (projectToRemove.getId() == 0) {
                projectListView.getItems().remove(projectToRemove);
                return;
            }
        }

        try {
            projectManager.deleteNote(projectToRemove.getId());
            projectListView.getItems().remove(projectToRemove);
            clearProjectDetails();
        } catch (Exception e) {
            System.err.println("Ошибка при удалении проекта: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Ошибка при удалении проекта: " + e.getMessage()).show();
        }
    }

    @FXML
    public void addInProjectComponentButtonClicked() {

    }

    @FXML
    public void removeInProjectComponentButtonClicked() {

    }

    @FXML
    public void addProjectButtonClicked() {
        if (selectedProject == null || selectedProject.getId() != 0) {
            new Alert(Alert.AlertType.WARNING, "Для добавления нового проекта сначала нажмите 'Добавить пустой' и заполните поля.").show();
            return;
        }

        try {
            Project newProject = getProjectFromFields(0);
            projectManager.addNote(newProject);
            projectListView.getItems().remove(selectedProject);
            projectListView.getItems().add(0, newProject);
            showProjectDetails(newProject);
            this.selectedProject = newProject;

        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).show();
        } catch (Exception e) {
            String errorMessage = "Ошибка при сохранении проекта в БД: " + e.getMessage();
            System.err.println(errorMessage);
            new Alert(Alert.AlertType.ERROR, errorMessage).show();
        }
    }

    @FXML
    public void saveDataButtonClicked() {
        try {
            Project project = getProjectFromFields(selectedProject.getId());
            projectManager.updateNote(project);
            int selectedIndex = projectListView.getSelectionModel().getSelectedIndex();
            projectListView.getItems().set(selectedIndex, project);
            this.selectedProject = project;
            clearProjectDetails();
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка сохранения: " + e.getMessage()).show();
        } catch (Exception e) {
            // Обработка ошибок базы данных
            String errorMessage = "Ошибка при обновлении данных проекта в БД: " + e.getMessage();
            System.err.println(errorMessage);
            new Alert(Alert.AlertType.ERROR, errorMessage).show();
        }
    }
}