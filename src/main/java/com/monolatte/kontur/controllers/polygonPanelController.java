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
    @FXML private CheckBox projectsCostCheckBox;
    @FXML private CheckBox sortedByCostCheckBox;
    @FXML private TextField projectsCostTextField;
    @FXML private RadioButton costRadioButton, showProjectsRadioButton, withoutDetailsRadioButton;
    @FXML private Button showResult1;
    @FXML private AnchorPane tableContainer2;

    // --- Вкладка 3: Пример подзапросов ---
    @FXML private TextField idProjectTextField2;
    @FXML private RadioButton correlatedQueryRadioButton, uncorrelatedQueryRadioButton;
    @FXML private Button showResultButton2;
    @FXML private AnchorPane tableContainer3;

    // --- Вкладка 4: Запросы изменения данных ---
    @FXML private RadioButton addProjectsDataRadioButton, changeProjectsDataRadioButton, deleteProjectsDataRadioButton;
    @FXML private TextField idProjectTextField3, nameProjectTextField, startDateProjectTextField, endDateProjectTextField, statusProjectTextField;
    @FXML private CheckBox completeProjectCheckBox;
    @FXML private ListView<Component> projectsComponentsListVuew;
    @FXML private Button showResultButton3;
    @FXML private AnchorPane tableContainer4;

    // --- DAO ---
    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final UserDAO _userDAO = SQLTableManager.getInstance().getUserDAO();
    private final Components_usageDAO _componentsUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private final UserContactDAO _userContactDAO = SQLTableManager.getInstance().getUserContactDAO();

    @FXML
    public void initialize() {
        _setupToggleGroups();

        projectsRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "projects"));
        usersRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "users"));
        usersContactsRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "contacts"));

        showResult1.setOnAction(_ -> _handleFullSelect());
        showResultButton2.setOnAction(_ -> _handleSubqueries());

        idProjectTextField3.textProperty().addListener((_, _, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) _autoFillProjectData(newValue);
            else _clearModifyFields();
        });
        showResultButton3.setOnAction(_ -> _loadDataToContainer(tableContainer4, "projects"));

        projectsRadioButton.setSelected(true);
        _loadDataToContainer(tableContainer1, "projects");
    }

    private void _handleFullSelect() {
        TableView<Object> table = new TableView<>();
        ObservableList<Object> data = FXCollections.observableArrayList();

        _setupProjectColumns(table);

        if (!withoutDetailsRadioButton.isSelected()) {
            TableColumn<Object, String> statusCol = new TableColumn<>("Статус");
            statusCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).statusProperty());
            TableColumn<Object, String> dateCol = new TableColumn<>("Дата начала");
            dateCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).startDateProperty());
            table.getColumns().addAll(statusCol, dateCol);
        }

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
        for (Project p : rawProjects) {
            propertyList.add(new ProjectProperty(p));
        }

        // Фильтрация по минимальной цене
        String minPriceText = projectsCostTextField.getText();
        if (minPriceText != null && !minPriceText.isEmpty()) {
            try {
                float minPrice = Float.parseFloat(minPriceText);
                propertyList.removeIf(p -> p.totalComponentPriceProperty().get() < minPrice);
            } catch (NumberFormatException ignored) {}
        }

        // --- ИСПРАВЛЕННАЯ ЛОГИКА СОРТИРОВКИ ---
        Comparator<ProjectProperty> comparator = null;

        if (costRadioButton.isSelected()) {
            // Сортировка по статусу (алфавитный порядок)
            comparator = Comparator.comparing(p -> p.statusProperty().get(), Comparator.nullsLast(String::compareTo));

            // Если при этом выбрана и убывающая цена, добавляем второй уровень сортировки
            if (sortedByCostCheckBox.isSelected()) {
                comparator = comparator.thenComparing((p1, p2) ->
                        Float.compare(p2.totalComponentPriceProperty().get(), p1.totalComponentPriceProperty().get()));
            }
        } else if (sortedByCostCheckBox.isSelected()) {
            // Только по убыванию цены
            comparator = (p1, p2) -> Float.compare(p2.totalComponentPriceProperty().get(), p1.totalComponentPriceProperty().get());
        }

        if (comparator != null) {
            propertyList.sort(comparator);
        }

        data.addAll(propertyList);
        table.setItems(data);
        _injectTable(tableContainer2, table);
    }

    private void _handleSubqueries() {
        TableView<Object> table = new TableView<>();
        _setupProjectColumns(table);
        List<Project> results = uncorrelatedQueryRadioButton.isSelected()
                ? _projectDAO.getProjectsWithAboveAverageCost()
                : _projectDAO.getProjectsWithExpensiveComponents();
        ObservableList<Object> data = FXCollections.observableArrayList();
        results.forEach(p -> data.add(new ProjectProperty(p)));
        table.setItems(data);
        _injectTable(tableContainer3, table);
    }

    private void _loadDataToContainer(AnchorPane container, String type) {
        TableView<Object> table = new TableView<>();
        ObservableList<Object> data = FXCollections.observableArrayList();
        if (type.equals("projects")) {
            _setupProjectColumns(table);
            _projectDAO.getAllNotes().forEach(p -> data.add(new ProjectProperty(p)));
        } else if (type.equals("users")) {
            _setupUserColumns(table);
            _userDAO.getAllNotes().forEach(u -> data.add(new UserProperty(new SimpleLongProperty(u.getId()), u)));
        } else if (type.equals("contacts")) {
            _setupContactColumns(table);
            data.addAll(_userContactDAO.getAllNotes());
        }
        table.setItems(data);
        _injectTable(container, table);
    }

    private void _setupProjectColumns(TableView<Object> table) {
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).idProperty().asObject());
        TableColumn<Object, String> nameCol = new TableColumn<>("Проект");
        nameCol.setCellValueFactory(cd -> ((ProjectProperty)cd.getValue()).nameProperty());
        table.getColumns().setAll(idCol, nameCol);
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

    private void _autoFillProjectData(String id) {
        try {
            Project project = _projectDAO.getNoteById(Long.parseLong(id));
            if (project != null) {
                nameProjectTextField.setText(project.getProject_name());
                startDateProjectTextField.setText(project.getStart_date());
                endDateProjectTextField.setText(project.getEnd_date());
                statusProjectTextField.setText(project.getStatus());
                completeProjectCheckBox.setSelected("Completed".equalsIgnoreCase(project.getStatus()));
                List<Component> components = _componentsUsageDAO.getComponentsByProjectId(project.getId());
                projectsComponentsListVuew.setItems(FXCollections.observableArrayList(components));
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
        AnchorPane.setTopAnchor(table, 0.0);
        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
    }

    private void _setupToggleGroups() {
        ToggleGroup tg1 = new ToggleGroup();
        projectsRadioButton.setToggleGroup(tg1);
        usersRadioButton.setToggleGroup(tg1);
        usersContactsRadioButton.setToggleGroup(tg1);

        ToggleGroup tgSelectDetails = new ToggleGroup();
        costRadioButton.setToggleGroup(tgSelectDetails);
        showProjectsRadioButton.setToggleGroup(tgSelectDetails);
        withoutDetailsRadioButton.setToggleGroup(tgSelectDetails);

        ToggleGroup tgSubqueries = new ToggleGroup();
        correlatedQueryRadioButton.setToggleGroup(tgSubqueries);
        uncorrelatedQueryRadioButton.setToggleGroup(tgSubqueries);

        ToggleGroup tgMod = new ToggleGroup();
        addProjectsDataRadioButton.setToggleGroup(tgMod);
        changeProjectsDataRadioButton.setToggleGroup(tgMod);
        deleteProjectsDataRadioButton.setToggleGroup(tgMod);
    }
}