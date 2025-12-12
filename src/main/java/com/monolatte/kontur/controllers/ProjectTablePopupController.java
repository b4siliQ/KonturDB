package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Project;
import com.monolatte.kontur.model.Notes.Properties.ProjectProperty;
import com.monolatte.kontur.model.SQL.ProjectDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ProjectTablePopupController {
    @FXML
    TableView<ProjectProperty> projectsTable;
    @FXML
    TableColumn<ProjectProperty, Long> idColumn;
    @FXML
    TableColumn<ProjectProperty, String> nameColumn;
    @FXML
    TableColumn<ProjectProperty, String> startDateColumn;
    @FXML
    TableColumn<ProjectProperty, String> endDateColumn;
    @FXML
    TableColumn<ProjectProperty, String> statusColumn;
    @FXML
    TextField searchTextField;
    @FXML
    CheckBox filterCheckBox;

    private final ProjectDAO _projectDAO = SQLTableManager.getInstance().getProjectManager();
    private final ObservableList<ProjectProperty> _masterData = FXCollections.observableArrayList();
    private FilteredList<ProjectProperty> _filteredData;
    private SortedList<ProjectProperty> _sortedData;

    private String _currentSearchText = "";

    @FXML
    public void initialize() {
        this._setupTableColumns();
        this._loadDataIntoTable();
        this._setupSearchFunctionality();
    }

    private void _setupTableColumns() {
        this.idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        this.nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.startDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
        this.endDateColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
        this.statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void _loadDataIntoTable() {
        this._fillProjectPropertyList(this._projectDAO.getAllNotes());
        // 2. Инициализируем FilteredList, привязанный к Master List
        this._filteredData = new FilteredList<>(this._masterData, _ -> true);

        // 3. Оборачиваем в SortedList
        this._sortedData = new SortedList<>(this._filteredData);

        // 4. Привязываем компаратор SortedList к компаратору TableView
        // Примечание: Это нужно только когда TableView отображает _sortedData
        this._sortedData.comparatorProperty().bind(this.projectsTable.comparatorProperty());

        // 5. Изначально TableView отображает ВСЕ данные (_masterData)
        this.projectsTable.setItems(this._masterData);
    }

    private void _setupSearchFunctionality() {
        this.projectsTable.setRowFactory(_ -> new TableRow<>() {
            @Override
            protected void updateItem(ProjectProperty item, boolean empty) {
                super.updateItem(item, empty);

                // Удаляем предыдущий стиль
                getStyleClass().remove("highlighted-row");

                if (item == null || empty || _currentSearchText.isEmpty() || filterCheckBox.isSelected()) {
                    // Если нет данных, или нет поиска, или включен фильтр, не подсвечиваем.
                    // Примечание: Если фильтр включен, подсветка не нужна, т.к. отображаются только найденные строки.
                } else {
                    if (_isMatch(item, _currentSearchText)) {
                        getStyleClass().add("highlighted-row"); // Добавляем синий стиль
                    }
                }
            }
        });

        // 2. Добавляем слушатель к полю поиска
        this.searchTextField.textProperty().addListener((_, _, newValue) -> {
            _currentSearchText = newValue != null ? newValue.toLowerCase() : "";

            if (this.filterCheckBox.isSelected()) {
                // Если фильтр ВКЛЮЧЕН, при изменении текста сразу обновляем FilteredList (см. Шаг 3)
                this._applyFilterPredicate(newValue);
            } else {
                // Если фильтр ВЫКЛЮЧЕН, принудительно обновляем TableView для перерисовки (Подсветка)
                this._forceTableRefresh();
            }
        });

        // 3. Слушатель для CheckBox (см. Шаг 3)
        this.filterCheckBox.selectedProperty().addListener((_, _, isNowSelected) -> this._handleFilterToggle(isNowSelected));
    }

    // Вспомогательный метод для проверки соответствия
    private boolean _isMatch(ProjectProperty item, String filter) {
        if (filter == null || filter.isEmpty()) return true;

        String lowerCaseFilter = filter.toLowerCase();

        String name = item.nameProperty().get();
        String startDate = item.startDateProperty().get();
        String endDate = item.endDateProperty().get();
        String status = item.statusProperty().get();

        return (name != null && name.toLowerCase().contains(lowerCaseFilter)) ||
                (startDate != null && startDate.toLowerCase().contains(lowerCaseFilter)) ||
                (endDate != null && endDate.toLowerCase().contains(lowerCaseFilter)) ||
                (status != null && status.toLowerCase().contains(lowerCaseFilter));
    }

    // Вспомогательный метод для принудительного обновления RowFactory
    private void _forceTableRefresh() {
        this.projectsTable.getColumns().getFirst().setVisible(false);
        this.projectsTable.getColumns().getFirst().setVisible(true);
    }

    private void _applyFilterPredicate(String filter) {
        if (_filteredData == null) return;

        if (filter == null || filter.isEmpty()) {
            _filteredData.setPredicate(_ -> true);
        } else {
            _filteredData.setPredicate(projectProperty -> _isMatch(projectProperty, filter));
        }
    }

    private void _handleFilterToggle(boolean isNowSelected) {
        if (isNowSelected) {
            // ФИЛЬТРАЦИЯ ВКЛЮЧЕНА: Применяем фильтр к FilteredList и отображаем FilteredList
            this._applyFilterPredicate(this.searchTextField.getText());
            this.projectsTable.setItems(this._sortedData);
        } else {
            // ФИЛЬТРАЦИЯ ВЫКЛЮЧЕНА: Отображаем Master List (все данные)
            this.projectsTable.setItems(this._masterData);

            // Принудительно обновляем для применения подсветки
            this._forceTableRefresh();
        }
    }

    private void _fillProjectPropertyList(List<Project> projectList) {
        this._masterData.clear();
        for (var project : projectList) {
            this._masterData.add(new ProjectProperty(project));
        }
    }
}
