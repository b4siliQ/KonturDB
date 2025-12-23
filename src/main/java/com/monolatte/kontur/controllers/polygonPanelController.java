package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.*;
import com.monolatte.kontur.model.Notes.Properties.*;
import com.monolatte.kontur.model.SQL.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class polygonPanelController {

    // --- Вкладка 1: Примеры запросов ---
    @FXML private RadioButton projectsRadioButton, usersRadioButton, usersContactsRadioButton;
    @FXML private AnchorPane tableContainer1;

    // --- Вкладка 2: Полная запись Select ---
    @FXML private TextField idProjectTextField1;
    @FXML private CheckBox projectsCostCheckBox, sortedByCostCheckBox;
    @FXML private TextField projectsCostTextField;
    @FXML private RadioButton costRadioButton, showProjectsRadioButton, withoutDetailsRadioButton;
    @FXML private Button showResult1;
    @FXML private AnchorPane tableContainer2;

    // --- Вкладка 3: Пример подзапросов ---
    @FXML private TextField idProjectTextField2;
    @FXML private RadioButton correlatedQueryRadioButton, uncorrelatedQueryRadioButton;
    @FXML private Button showResultButton2;
    @FXML private AnchorPane tableContainer3;

    // --- Вкладка 4: Запросы изменения данных (DML) ---
    @FXML private RadioButton addProjectsDataRadioButton, changeProjectsDataRadioButton, deleteProjectsDataRadioButton;
    @FXML private TextField idProjectTextField3, nameProjectTextField, startDateProjectTextField, endDateProjectTextField, statusProjectTextField;
    @FXML private CheckBox completeProjectCheckBox;
    @FXML private ListView<Component> projectsComponentsListVuew;
    @FXML private Button requestButton;      // Кнопка "Выполнить запрос"
    @FXML private Button showResultButton3;  // Кнопка "Показать список проектов"
    @FXML private AnchorPane tableContainer4;

    // --- DAO (Менеджеры таблиц) ---
    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final UserDAO _userDAO = SQLTableManager.getInstance().getUserDAO();
    private final Components_usageDAO _componentsUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private final UserContactDAO _userContactDAO = SQLTableManager.getInstance().getUserContactDAO();

    @FXML
    public void initialize() {
        _setupToggleGroups();

        // Настройка событий для вкладок
        _setupSelectionTabs();
        _setupModificationTab();

        // Установка начального отображения
        if (projectsRadioButton != null) {
            projectsRadioButton.setSelected(true);
            _loadDataToContainer(tableContainer1, "projects");
        }
    }

    // --- ЛОГИКА ВКЛАДКИ 4 (Изменение данных) ---
    private void _setupModificationTab() {
        // 1. Автозаполнение полей при вводе ID
        if (idProjectTextField3 != null) {
            idProjectTextField3.textProperty().addListener((_, _, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) _autoFillProjectData(newValue);
                else _clearModifyFields();
            });
        }

        // 2. Кнопка ВЫПОЛНИТЬ ЗАПРОС (INSERT, UPDATE, DELETE)
        if (requestButton != null) {
            requestButton.setOnAction(_ -> {
                _executeModification();
                _loadDataToContainer(tableContainer4, "projects"); // Авто-обновление таблицы после действия
            });
        }

        // 3. Кнопка ПОКАЗАТЬ СПИСОК ПРОЕКТОВ (ПРОСТО SELECT)
        if (showResultButton3 != null) {
            showResultButton3.setOnAction(_ -> _loadDataToContainer(tableContainer4, "projects"));
        }
    }

    private void _executeModification() {
        try {
            // Собираем данные из полей
            String name = nameProjectTextField.getText();
            String start = startDateProjectTextField.getText();
            String end = endDateProjectTextField.getText();
            String status = completeProjectCheckBox.isSelected() ? "Completed" : statusProjectTextField.getText();

            // Если поле статуса пустое, а чекбокс не нажат, поставим заглушку
            if (status.isEmpty()) status = "Draft";

            Project project = new Project(name, start, end, status);

            long id = 0;
            if (!idProjectTextField3.getText().isEmpty()) {
                id = Long.parseLong(idProjectTextField3.getText());
            }

            // Выбираем действие в зависимости от радиокнопки
            if (addProjectsDataRadioButton.isSelected()) {
                _projectDAO.addNote(project);
            } else if (changeProjectsDataRadioButton.isSelected() && id != 0) {
                project.setId(id);
                _projectDAO.updateNote(project);
            } else if (deleteProjectsDataRadioButton.isSelected() && id != 0) {
                _projectDAO.deleteNote(id);
            } else {
                System.out.println("Хозяин, вы не выбрали тип операции или не указали ID!");
            }
        } catch (Exception e) {
            System.err.println("Ошибка выполнения DML запроса: " + e.getMessage());
        }
    }

    // --- ОБЩАЯ ЛОГИКА ТАБЛИЦ (Вывод всех полей) ---
    private void _loadDataToContainer(AnchorPane container, String type) {
        if (container == null) return;
        TableView<Object> table = new TableView<>();
        ObservableList<Object> data = FXCollections.observableArrayList();

        switch (type) {
            case "projects" -> {
                _setupProjectColumns(table);     // ID, Имя
                _addProjectDetailColumns(table); // Статус, Начало, Конец
                _projectDAO.getAllNotes().forEach(p -> data.add(new ProjectProperty(p)));
            }
            case "users" -> {
                _setupUserColumns(table);
                _userDAO.getAllNotes().forEach(u -> data.add(new UserProperty(new SimpleLongProperty(u.getId()), u)));
            }
            case "contacts" -> {
                _setupContactColumns(table);
                data.addAll(_userContactDAO.getAllNotes());
            }
        }
        table.setItems(data);
        _injectTable(container, table);
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ КОЛОНОК ---
    private void _setupProjectColumns(TableView<Object> table) {
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).idProperty().asObject());
        TableColumn<Object, String> nameCol = new TableColumn<>("Проект");
        nameCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).nameProperty());
        table.getColumns().setAll(idCol, nameCol);
    }

    private void _addProjectDetailColumns(TableView<Object> table) {
        TableColumn<Object, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).statusProperty());
        TableColumn<Object, String> startCol = new TableColumn<>("Начало");
        startCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).startDateProperty());
        TableColumn<Object, String> endCol = new TableColumn<>("Конец");
        endCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).endDateProperty());
        table.getColumns().addAll(statusCol, startCol, endCol);
    }

    // --- СИСТЕМНЫЕ МЕТОДЫ ---
    private void _setupSelectionTabs() {
        if (projectsRadioButton != null) {
            projectsRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "projects"));
            usersRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "users"));
            usersContactsRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "contacts"));
        }
        if (showResult1 != null) showResult1.setOnAction(_ -> _handleFullSelect());
        if (showResultButton2 != null) showResultButton2.setOnAction(_ -> _handleSubqueries());
    }

    private void _handleFullSelect() {
        TableView<Object> table = new TableView<>();
        _setupProjectColumns(table);
        if (!withoutDetailsRadioButton.isSelected()) _addProjectDetailColumns(table);

        if (projectsCostCheckBox.isSelected()) {
            TableColumn<Object, Number> costCol = new TableColumn<>("Общая стоимость");
            costCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).totalComponentPriceProperty());
            table.getColumns().add(costCol);
        }

        List<Project> rawProjects;
        String idSearch = idProjectTextField1.getText();
        if (showProjectsRadioButton.isSelected() || idSearch.isEmpty()) {
            rawProjects = _projectDAO.getAllNotes();
        } else {
            rawProjects = _projectDAO.search("id", idSearch);
        }

        List<ProjectProperty> propertyList = new ArrayList<>();
        rawProjects.forEach(p -> propertyList.add(new ProjectProperty(p)));

        // Фильтрация и Сортировка по цене
        _applyFiltersAndSort(propertyList);

        table.setItems(FXCollections.observableArrayList(propertyList));
        _injectTable(tableContainer2, table);
    }

    private void _applyFiltersAndSort(List<ProjectProperty> list) {
        // Фильтр по цене
        String minPriceStr = projectsCostTextField.getText();
        if (minPriceStr != null && !minPriceStr.isEmpty()) {
            try {
                float minPrice = Float.parseFloat(minPriceStr);
                list.removeIf(p -> p.totalComponentPriceProperty().get() < minPrice);
            } catch (NumberFormatException ignored) {}
        }
        // Сортировка
        Comparator<ProjectProperty> comp = null;
        if (costRadioButton.isSelected()) {
            comp = Comparator.comparing(p -> p.statusProperty().get(), Comparator.nullsLast(String::compareTo));
            if (sortedByCostCheckBox.isSelected()) {
                comp = comp.thenComparing((p1, p2) -> Float.compare(p2.totalComponentPriceProperty().get(), p1.totalComponentPriceProperty().get()));
            }
        } else if (sortedByCostCheckBox.isSelected()) {
            comp = (p1, p2) -> Float.compare(p2.totalComponentPriceProperty().get(), p1.totalComponentPriceProperty().get());
        }
        if (comp != null) list.sort(comp);
    }

    private void _setupUserColumns(TableView<Object> table) {
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> ((UserProperty)cd.getValue()).idProperty().asObject());
        TableColumn<Object, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(cd -> ((UserProperty)cd.getValue()).nameProperty());
        table.getColumns().setAll(idCol, nameCol);
    }

    private void _setupContactColumns(TableView<Object> table) {
        TableColumn<Object, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(cd -> new SimpleStringProperty(((UserContact)cd.getValue()).getContact_type()));
        TableColumn<Object, String> valCol = new TableColumn<>("Значение");
        valCol.setCellValueFactory(cd -> new SimpleStringProperty(((UserContact)cd.getValue()).getContact_value()));
        table.getColumns().setAll(typeCol, valCol);
    }

    private void _handleSubqueries() {
        TableView<Object> table = new TableView<>();
        _setupProjectColumns(table);
        _addProjectDetailColumns(table);
        List<Project> res = uncorrelatedQueryRadioButton.isSelected() ? _projectDAO.getProjectsWithAboveAverageCost() : _projectDAO.getProjectsWithExpensiveComponents();
        ObservableList<Object> data = FXCollections.observableArrayList();
        res.forEach(p -> data.add(new ProjectProperty(p)));
        table.setItems(data);
        _injectTable(tableContainer3, table);
    }

    private void _autoFillProjectData(String id) {
        try {
            Project project = _projectDAO.getNoteById(Long.parseLong(id));
            if (project != null) {
                nameProjectTextField.setText(project.getProject_name());
                startDateProjectTextField.setText(project.getStart_date());
                endDateProjectTextField.setText(project.getEnd_date());
                statusProjectTextField.setText(project.getStatus());
                completeProjectCheckBox.setSelected("Completed".equalsIgnoreCase(project.getStatus()));
                projectsComponentsListVuew.setItems(FXCollections.observableArrayList(_componentsUsageDAO.getComponentsByProjectId(project.getId())));
            }
        } catch (Exception ignored) {}
    }

    private void _clearModifyFields() {
        nameProjectTextField.clear(); startDateProjectTextField.clear();
        endDateProjectTextField.clear(); statusProjectTextField.clear();
        completeProjectCheckBox.setSelected(false);
        projectsComponentsListVuew.getItems().clear();
    }

    private void _injectTable(AnchorPane container, TableView<Object> table) {
        if (container == null) return;
        container.getChildren().setAll(table);
        AnchorPane.setTopAnchor(table, 0.0); AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0); AnchorPane.setRightAnchor(table, 0.0);
    }

    private void _setupToggleGroups() {
        ToggleGroup tg1 = new ToggleGroup();
        if (projectsRadioButton != null) { projectsRadioButton.setToggleGroup(tg1); usersRadioButton.setToggleGroup(tg1); usersContactsRadioButton.setToggleGroup(tg1); }
        ToggleGroup tg2 = new ToggleGroup();
        if (costRadioButton != null) { costRadioButton.setToggleGroup(tg2); showProjectsRadioButton.setToggleGroup(tg2); withoutDetailsRadioButton.setToggleGroup(tg2); }
        ToggleGroup tg3 = new ToggleGroup();
        if (correlatedQueryRadioButton != null) { correlatedQueryRadioButton.setToggleGroup(tg3); uncorrelatedQueryRadioButton.setToggleGroup(tg3); }
        ToggleGroup tg4 = new ToggleGroup();
        if (addProjectsDataRadioButton != null) { addProjectsDataRadioButton.setToggleGroup(tg4); changeProjectsDataRadioButton.setToggleGroup(tg4); deleteProjectsDataRadioButton.setToggleGroup(tg4); }
    }
}