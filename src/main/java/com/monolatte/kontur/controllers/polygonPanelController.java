package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Properties.*;
import com.monolatte.kontur.model.SQL.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.beans.property.SimpleStringProperty;
import java.util.List;
import java.util.ArrayList;

public class polygonPanelController {

    // --- Вкладка 1: Примеры запросов ---
    @FXML private RadioButton projectsRadioButton, usersRadioButton, usersContactsRadioButton;
    @FXML private AnchorPane tableContainer1;

    // --- Вкладка 2: Полная запись Select ---
    @FXML private TextField idProjectTextField1;
    @FXML private RadioButton showProjectsRadioButton;
    @FXML private Button showResult1;
    @FXML private AnchorPane tableContainer2;

    // --- Вкладка 3: Пример подзапросов ---
    @FXML private RadioButton correlatedQueryRadioButton, uncorrelatedQueryRadioButton;
    @FXML private Button showResultButton2;
    @FXML private AnchorPane tableContainer3;

    // --- Вкладка 4: Запросы изменения данных (DML) ---
    @FXML private RadioButton addProjectsDataRadioButton, changeProjectsDataRadioButton, deleteProjectsDataRadioButton;
    @FXML private TextField idProjectTextField3, nameProjectTextField, startDateProjectTextField, endDateProjectTextField, statusProjectTextField;
    @FXML private CheckBox completeProjectCheckBox;
    @FXML private Button requestButton;
    @FXML private AnchorPane tableContainer4;

    // --- DAO менеджеры ---
    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final UserContactDAO _userContactDAO = SQLTableManager.getInstance().getUserContactDAO();
    private final User_usageDAO _userUsageDAO = SQLTableManager.getInstance().getUserUsageDAO();

    @FXML
    public void initialize() {
        _setupToggleGroups();

        // Настройка действий для кнопок ПЕРВОЙ вкладки
        projectsRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "projects_special"));
        usersRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "users_projects_join"));
        usersContactsRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "addresses_special"));

        // Настройка остальных вкладок
        if (showResult1 != null) showResult1.setOnAction(_ -> _handleFullSelect());
        if (showResultButton2 != null) showResultButton2.setOnAction(_ -> _handleSubqueries());
        _setupModificationTab();

        // Стартовая загрузка (Проекты)
        projectsRadioButton.setSelected(true);
        _loadSpecialDataToContainer(tableContainer1, "projects_special");
    }

    // --- ЛОГИКА ВКЛАДКИ 1 (Ваши три кнопки) ---
    private void _loadSpecialDataToContainer(AnchorPane container, String type) {
        if (container == null) return;

        TableView<String[]> table = new TableView<>();
        List<String[]> data = new ArrayList<>();
        String[] headers = new String[]{};

        // Вызываем именно ваши методы из DAO
        switch (type) {
            case "projects_special" -> {
                headers = new String[]{"ID", "Проект [Статус]", "Дата начала", "Пояснение"};
                data = _projectDAO.getProjectsSpecialSelection();
            }
            case "users_projects_join" -> {
                headers = new String[]{"Имя пользователя", "Закреплен за проектом"};
                data = _userUsageDAO.getUsersAndProjectsJoin();
            }
            case "addresses_special" -> {
                headers = new String[]{"Имя пользователя", "Контактные данные"};
                data = _userContactDAO.getUserContactsSpecial();
            }
        }

        // ПРАВИЛЬНОЕ создание колонок для массивов String[]
        table.getColumns().clear();
        for (int i = 0; i < headers.length; i++) {
            final int index = i;
            TableColumn<String[], String> column = new TableColumn<>(headers[i]);

            // Этот кусок кода связывает данные с таблицей (БЕЗ НЕГО БУДЕТ ПУСТО)
            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                return new SimpleStringProperty((row != null && index < row.length) ? row[index] : "");
            });
            table.getColumns().add(column);
        }

        table.setItems(FXCollections.observableArrayList(data));
        _injectTable(container, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 2 (Поиск и выборка) ---
    private void _handleFullSelect() {
        TableView<Object> table = new TableView<>();
        _setupProjectColumns(table);

        List<Project> raw = (showProjectsRadioButton.isSelected() || idProjectTextField1.getText().isEmpty())
                ? _projectDAO.getAllNotes()
                : _projectDAO.search("id", idProjectTextField1.getText());

        ObservableList<Object> props = FXCollections.observableArrayList();
        raw.forEach(p -> props.add(new ProjectProperty(p)));
        table.setItems(props);
        _injectTable(tableContainer2, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 3 (Подзапросы) ---
    private void _handleSubqueries() {
        TableView<Object> table = new TableView<>();
        _setupProjectColumns(table);

        List<Project> res = (uncorrelatedQueryRadioButton.isSelected())
                ? _projectDAO.getProjectsWithAboveAverageCost()
                : _projectDAO.getProjectsWithExpensiveComponents();

        ObservableList<Object> data = FXCollections.observableArrayList();
        res.forEach(p -> data.add(new ProjectProperty(p)));
        table.setItems(data);
        _injectTable(tableContainer3, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 4 (DML изменения) ---
    private void _setupModificationTab() {
        if (idProjectTextField3 != null) {
            idProjectTextField3.textProperty().addListener((obs, oldV, newV) -> {
                if (newV != null && !newV.isEmpty()) {
                    try {
                        Project p = _projectDAO.getNoteById(Long.parseLong(newV));
                        if (p != null) {
                            nameProjectTextField.setText(p.getProject_name());
                            statusProjectTextField.setText(p.getStatus());
                            startDateProjectTextField.setText(p.getStart_date());
                            endDateProjectTextField.setText(p.getEnd_date());
                        }
                    } catch (Exception ignored) {}
                }
            });
        }
        if (requestButton != null) {
            requestButton.setOnAction(_ -> {
                _executeModification();
                _refreshTable4();
            });
        }
    }

    private void _executeModification() {
        try {
            Project project = new Project(nameProjectTextField.getText(), startDateProjectTextField.getText(),
                    endDateProjectTextField.getText(), statusProjectTextField.getText());
            String idRaw = idProjectTextField3.getText();

            if (addProjectsDataRadioButton.isSelected()) {
                _projectDAO.addNote(project);
            } else if (!idRaw.isEmpty()) {
                project.setId(Long.parseLong(idRaw));
                if (changeProjectsDataRadioButton.isSelected()) _projectDAO.updateNote(project);
                else if (deleteProjectsDataRadioButton.isSelected()) _projectDAO.deleteNote(project.getId());
            }
        } catch (Exception e) { System.err.println("Ошибка DML: " + e.getMessage()); }
    }

    private void _refreshTable4() {
        TableView<Object> table = new TableView<>();
        _setupProjectColumns(table);
        ObservableList<Object> data = FXCollections.observableArrayList();
        _projectDAO.getAllNotes().forEach(p -> data.add(new ProjectProperty(p)));
        table.setItems(data);
        _injectTable(tableContainer4, table);
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    private void _setupProjectColumns(TableView<Object> table) {
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).idProperty().asObject());
        TableColumn<Object, String> nameCol = new TableColumn<>("Название проекта");
        nameCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).nameProperty());
        table.getColumns().setAll(idCol, nameCol);
    }

    private void _injectTable(AnchorPane container, TableView<?> table) {
        container.getChildren().setAll(table);
        AnchorPane.setTopAnchor(table, 0.0); AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0); AnchorPane.setRightAnchor(table, 0.0);
    }

    private void _setupToggleGroups() {
        ToggleGroup tg1 = new ToggleGroup();
        projectsRadioButton.setToggleGroup(tg1); usersRadioButton.setToggleGroup(tg1); usersContactsRadioButton.setToggleGroup(tg1);
        ToggleGroup tg2 = new ToggleGroup();
        addProjectsDataRadioButton.setToggleGroup(tg2); changeProjectsDataRadioButton.setToggleGroup(tg2); deleteProjectsDataRadioButton.setToggleGroup(tg2);
    }
}