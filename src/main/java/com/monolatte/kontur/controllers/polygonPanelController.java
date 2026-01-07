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
    @FXML private TextField idProjectTextField1, projectsCostTextField;
    @FXML private CheckBox projectsCostCheckBox, sortedByCostCheckBox;
    @FXML private RadioButton showProjectsRadioButton, costRadioButton, withoutDetailsRadioButton;
    @FXML private Button showResult1;
    @FXML private AnchorPane tableContainer2;

    // --- Вкладка 3: Пример подзапросов ---
    @FXML private RadioButton correlatedQueryRadioButton, uncorrelatedQueryRadioButton;
    @FXML private Button showResultButton2;
    @FXML private AnchorPane tableContainer3;

    // --- Вкладка 4: Изменение данных ---
    @FXML private RadioButton addProjectsDataRadioButton, changeProjectsDataRadioButton, deleteProjectsDataRadioButton;
    @FXML private TextField idProjectTextField3, nameProjectTextField, startDateProjectTextField, endDateProjectTextField, statusProjectTextField;
    @FXML private Button requestButton, updateComponentsButton;
    @FXML private ListView<String> projectsComponentsListVuew; // Список компонентов справа
    @FXML private AnchorPane tableContainer4;

    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final UserContactDAO _userContactDAO = SQLTableManager.getInstance().getUserContactDAO();
    private final User_usageDAO _userUsageDAO = SQLTableManager.getInstance().getUserUsageDAO();
    private final Components_usageDAO _compUsageDAO = SQLTableManager.getInstance().getComponentsUsageManager();

    @FXML
    public void initialize() {
        _setupToggleGroups();

        // ВКЛАДКА 1
        projectsRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "projects_simple"));
        usersRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "users_complex"));
        usersContactsRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "contacts_join"));

        // ВКЛАДКИ 2 и 3
        if (showResult1 != null) showResult1.setOnAction(_ -> _handleFullSelect());
        if (showResultButton2 != null) showResultButton2.setOnAction(_ -> _handleSubqueries());

        // ВКЛАДКА 4
        _setupModificationTab();
        if (updateComponentsButton != null) {
            updateComponentsButton.setOnAction(_ -> {
                if (!idProjectTextField3.getText().isEmpty())
                    _updateComponentsList(Long.parseLong(idProjectTextField3.getText()));
            });
        }

        // Стартовое состояние
        projectsRadioButton.setSelected(true);
        _loadSpecialDataToContainer(tableContainer1, "projects_simple");
    }

    // --- ОБЩАЯ НАСТРОЙКА КОЛОНОК (ID, Название, Даты, Статус, Кол-во, Цена) ---
    private void _setupProjectColumns(TableView<ProjectProperty> table) {
        TableColumn<ProjectProperty, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> cd.getValue().idProperty().asObject());

        TableColumn<ProjectProperty, String> nameCol = new TableColumn<>("Название проекта");
        nameCol.setCellValueFactory(cd -> cd.getValue().nameProperty());

        TableColumn<ProjectProperty, String> startCol = new TableColumn<>("Начало");
        startCol.setCellValueFactory(cd -> cd.getValue().startDateProperty());

        TableColumn<ProjectProperty, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cd -> cd.getValue().statusProperty());

        // Используем свойства из ProjectProperty
        TableColumn<ProjectProperty, Integer> qantityCol = new TableColumn<>("Кол-во комп.");
        qantityCol.setCellValueFactory(cd -> cd.getValue().componentsQuantityProperty().asObject());

        TableColumn<ProjectProperty, Float> totalPriceCol = new TableColumn<>("Общая стоимость");
        totalPriceCol.setCellValueFactory(cd -> cd.getValue().totalComponentPriceProperty().asObject());

        table.getColumns().setAll(idCol, nameCol, startCol, statusCol, qantityCol, totalPriceCol);
    }

    // --- ЛОГИКА ВКЛАДКИ 2 ---
    private void _handleFullSelect() {
        TableView<ProjectProperty> table = new TableView<>();
        _setupProjectColumns(table);

        List<Project> raw = (showProjectsRadioButton.isSelected() || idProjectTextField1.getText().isEmpty())
                ? _projectDAO.getAllNotes()
                : _projectDAO.search("id", idProjectTextField1.getText());

        table.setItems(_wrapAndFilter(raw));
        _injectTable(tableContainer2, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 3 (Подзапросы) ---
    private void _handleSubqueries() {
        TableView<ProjectProperty> table = new TableView<>();
        _setupProjectColumns(table);

        List<Project> res = (uncorrelatedQueryRadioButton.isSelected())
                ? _projectDAO.getProjectsWithAboveAverageCost()
                : _projectDAO.getProjectsWithExpensiveComponents();

        ObservableList<ProjectProperty> data = FXCollections.observableArrayList();
        if (res != null) res.forEach(p -> data.add(new ProjectProperty(p)));

        table.setItems(data);
        _injectTable(tableContainer3, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 4 (DML и Список компонентов) ---
    private void _setupModificationTab() {
        idProjectTextField3.textProperty().addListener((_, _, newV) -> {
            if (newV != null && !newV.isEmpty()) {
                try {
                    long id = Long.parseLong(newV);
                    Project p = _projectDAO.getNoteById(id);
                    if (p != null) {
                        nameProjectTextField.setText(p.getProject_name());
                        statusProjectTextField.setText(p.getStatus());
                        startDateProjectTextField.setText(p.getStart_date());
                        endDateProjectTextField.setText(p.getEnd_date());
                        _updateComponentsList(id); // Загружаем компоненты
                    }
                } catch (Exception ignored) {}
            }
        });

        requestButton.setOnAction(_ -> { _executeModification(); _refreshTable4(); });
    }

    private void _updateComponentsList(long projectId) {
        if (projectsComponentsListVuew == null) return;
        List<Component> components = _compUsageDAO.getComponentsByProjectId(projectId); //
        ObservableList<String> items = FXCollections.observableArrayList();
        if (components != null) {
            components.forEach(c -> items.add(c.getName() + " | " + c.getPrice() + " руб."));
        }
        projectsComponentsListVuew.setItems(items);
    }

    private void _executeModification() {
        try {
            Project p = new Project(nameProjectTextField.getText(), startDateProjectTextField.getText(), endDateProjectTextField.getText(), statusProjectTextField.getText());
            String id = idProjectTextField3.getText();
            if (addProjectsDataRadioButton.isSelected()) _projectDAO.addNote(p);
            else if (!id.isEmpty()) {
                p.setId(Long.parseLong(id));
                if (changeProjectsDataRadioButton.isSelected()) _projectDAO.updateNote(p);
                else if (deleteProjectsDataRadioButton.isSelected()) _projectDAO.deleteNote(p.getId());
            }
        } catch (Exception e) { System.err.println("Ошибка DML"); }
    }

    private void _refreshTable4() {
        TableView<ProjectProperty> table = new TableView<>();
        _setupProjectColumns(table);
        ObservableList<ProjectProperty> data = FXCollections.observableArrayList();
        _projectDAO.getAllNotes().forEach(p -> data.add(new ProjectProperty(p)));
        table.setItems(data);
        _injectTable(tableContainer4, table);
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    private ObservableList<ProjectProperty> _wrapAndFilter(List<Project> raw) {
        ObservableList<ProjectProperty> data = FXCollections.observableArrayList();
        for (Project p : raw) {
            ProjectProperty pp = new ProjectProperty(p);
            if (projectsCostCheckBox.isSelected() && !projectsCostTextField.getText().isEmpty()) {
                try {
                    float min = Float.parseFloat(projectsCostTextField.getText());
                    if (pp.totalComponentPriceProperty().get() < min) continue;
                } catch (Exception ignored) {}
            }
            data.add(pp);
        }
        if (sortedByCostCheckBox.isSelected()) {
            data.sort((a, b) -> Float.compare(b.totalComponentPriceProperty().get(), a.totalComponentPriceProperty().get()));
        }
        return data;
    }

    private void _loadSpecialDataToContainer(AnchorPane container, String type) {
        if (container == null) return;
        TableView<String[]> table = new TableView<>();
        List<String[]> data = new ArrayList<>();
        String[] headers = switch (type) {
            case "projects_simple" -> {
                for (Project p : _projectDAO.getAllNotes())
                    data.add(new String[]{String.valueOf(p.getId()), p.getProject_name(), p.getStatus(), p.getStart_date()});
                yield new String[]{"ID", "Название", "Статус", "Начало"};
            }
            case "users_complex" -> { data = _userUsageDAO.getUsersAndProjectsJoin(); yield new String[]{"Пользователь", "Проект"}; }
            case "contacts_join" -> { data = _userContactDAO.getUserContactsSpecial(); yield new String[]{"Пользователь", "Контакты"}; }
            default -> new String[]{};
        };

        for (int i = 0; i < headers.length; i++) {
            final int index = i;
            TableColumn<String[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[index]));
            table.getColumns().add(col);
        }
        table.setItems(FXCollections.observableArrayList(data));
        _injectTable(container, table);
    }

    private void _injectTable(AnchorPane container, TableView<?> table) {
        container.getChildren().setAll(table);
        AnchorPane.setTopAnchor(table, 0.0); AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0); AnchorPane.setRightAnchor(table, 0.0);
    }

    private void _setupToggleGroups() {
        ToggleGroup tg1 = new ToggleGroup(), tg2 = new ToggleGroup(), tg3 = new ToggleGroup();
        projectsRadioButton.setToggleGroup(tg1); usersRadioButton.setToggleGroup(tg1); usersContactsRadioButton.setToggleGroup(tg1);
        addProjectsDataRadioButton.setToggleGroup(tg2); changeProjectsDataRadioButton.setToggleGroup(tg2); deleteProjectsDataRadioButton.setToggleGroup(tg2);
        costRadioButton.setToggleGroup(tg3); showProjectsRadioButton.setToggleGroup(tg3); withoutDetailsRadioButton.setToggleGroup(tg3);
    }
}