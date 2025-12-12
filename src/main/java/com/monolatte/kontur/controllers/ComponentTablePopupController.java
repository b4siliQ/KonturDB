package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Properties.ComponentProperty;
import com.monolatte.kontur.model.SQL.ComponentsDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ComponentTablePopupController {
    @FXML
    TableView<ComponentProperty> componentsTable;
    @FXML
    TableColumn<ComponentProperty, Long> idColumn;
    @FXML
    TableColumn<ComponentProperty, String> nameColumn;
    @FXML
    TableColumn<ComponentProperty, String> typeColumn;
    @FXML
    TableColumn<ComponentProperty, String> specificationColumn;
    @FXML
    TableColumn<ComponentProperty, String> datasheetLinkColumn;
    @FXML
    TableColumn<ComponentProperty, Float> priceColumn;
    @FXML
    TextField searchTextField;
    @FXML
    CheckBox filterCheckBox;

    private final ComponentsDAO _componentsDAO = SQLTableManager.getInstance().getComponentsManager();
    private final ObservableList<ComponentProperty> _masterData = FXCollections.observableArrayList();
    private FilteredList<ComponentProperty> _filteredData;
    private SortedList<ComponentProperty> _sortedData;

    private String _currentSearchText = "";

    @FXML
    public void initialize() {
        this._setupTableColumns();
        this._loadDataIntoTable();
        this._setupSearchFunctionality();
    }

    private void _setupTableColumns() {
        this.idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject()); // asObject() для числовых типов
        this.nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        this.typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        this.specificationColumn.setCellValueFactory(cellData -> cellData.getValue().specificationProperty());
        this.datasheetLinkColumn.setCellValueFactory(cellData -> cellData.getValue().datasheetLinkProperty());
        this.priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject()); // asObject()
    }

    private void _loadDataIntoTable() {
        this._fillComponentPropertyList(this._componentsDAO.getAllNotes());
        // 2. Инициализируем FilteredList, привязанный к Master List
        this._filteredData = new FilteredList<>(this._masterData, _ -> true);

        // 3. Оборачиваем в SortedList
        this._sortedData = new SortedList<>(this._filteredData);
        // 4. Привязываем компаратор SortedList к компаратору TableView
        // Примечание: Это нужно только когда TableView отображает _sortedData
        this._sortedData.comparatorProperty().bind(this.componentsTable.comparatorProperty());

        // 5. Изначально TableView отображает ВСЕ данные (_masterData)
        this.componentsTable.setItems(this._masterData);
    }

    private void _setupSearchFunctionality() {
        this.componentsTable.setRowFactory(_ -> new TableRow<>() {
            @Override
            protected void updateItem(ComponentProperty item, boolean empty) {
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
    private boolean _isMatch(ComponentProperty item, String filter) {
        if (filter == null || filter.isEmpty()) return true;

        String lowerCaseFilter = filter.toLowerCase();

        String name = item.nameProperty().get();
        String type = item.typeProperty().get();

        return (name != null && name.toLowerCase().contains(lowerCaseFilter)) ||
                (type != null && type.toLowerCase().contains(lowerCaseFilter));
    }

    // Вспомогательный метод для принудительного обновления RowFactory
    private void _forceTableRefresh() {
        this.componentsTable.getColumns().getFirst().setVisible(false);
        this.componentsTable.getColumns().getFirst().setVisible(true);
    }

    private void _applyFilterPredicate(String filter) {
        if (_filteredData == null) return;

        if (filter == null || filter.isEmpty()) {
            _filteredData.setPredicate(_ -> true);
        } else {
            _filteredData.setPredicate(componentProperty -> _isMatch(componentProperty, filter));
        }
    }

    private void _handleFilterToggle(boolean isNowSelected) {
        if (isNowSelected) {
            // ФИЛЬТРАЦИЯ ВКЛЮЧЕНА: Применяем фильтр к FilteredList и отображаем FilteredList
            this._applyFilterPredicate(this.searchTextField.getText());
            this.componentsTable.setItems(this._sortedData);
        } else {
            // ФИЛЬТРАЦИЯ ВЫКЛЮЧЕНА: Отображаем Master List (все данные)
            this.componentsTable.setItems(this._masterData);

            // Принудительно обновляем для применения подсветки
            this._forceTableRefresh();
        }
    }

    private void _fillComponentPropertyList(List<Component> componentList) {
        this._masterData.clear();
        for (var component : componentList) {
            this._masterData.add(new ComponentProperty(component));
        }
    }
}
