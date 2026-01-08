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

public class PolygonComponentPanelController {

    // --- Вкладка 1: Примеры запросов ---
    @FXML private RadioButton componentsRadioButton, manufaturersRadioButton, manufacturersAddressesRadioButton;
    @FXML private AnchorPane tableContainer1;

    // --- Вкладка 2: Полная запись Select ---
    @FXML private TextField idComponentTextField1, componentCostTextField;
    @FXML private CheckBox componentsCostCheckBox, sortedByCostCheckBox;
    @FXML private RadioButton costRadioButton, showAllComponentsRadioButton, withoutDetailsRadioButton;
    @FXML private Button showResult1;
    @FXML private AnchorPane tableContainer2;

    // --- Вкладка 3: Пример подзапросов ---
    @FXML private RadioButton correlatedQueryRadioButton, uncorrelatedQueryRadioButton;
    @FXML private Button showResultButton2;
    @FXML private AnchorPane tableContainer3;

    // --- Вкладка 4: Изменения данных (DML) ---
    @FXML private RadioButton addСomponentsDataRadioButton, changeComponentsDataRadioButton, deleteComponentsDataRadioButton;
    @FXML private TextField idComponentTextField2, nameComponentTextField, datasheetLinkComponentTextFielf, costComponentTextFeild, quantityComponentTextFeild;
    @FXML private TextArea specificationComponentTextField;
    @FXML private CheckBox completeComponentCheckBox;
    @FXML private ListView<Project> componentsProjectsListVuew;
    @FXML private Button requestButton, showResultButton3;
    @FXML private AnchorPane tableContainer4;

    private final ComponentsDAO _componentDAO = SQLTableManager.getInstance().getComponentsManager();
    private final Components_usageDAO _usageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private final ManufacturerAdressesDAO _addressDAO = SQLTableManager.getInstance().getManufacturerAddressDAO();
    private final ManufacturerDAO _manufacturerDAO = SQLTableManager.getInstance().getManufacturerDAO();

    @FXML
    public void initialize() {
        _setupToggleGroups();

        // ЛОГИКА ВКЛАДКИ 1: Переключение между простым и сложными запросами
        componentsRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "comp_simple"));
        manufaturersRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "manuf_complex"));
        manufacturersAddressesRadioButton.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer1, "addr_join"));

        // Остальные вкладки
        showResult1.setOnAction(_ -> _handleFullSelect());
        showResultButton2.setOnAction(_ -> _handleSubqueries());
        showResultButton3.setOnAction(_ -> _loadSpecialDataToContainer(tableContainer4, "comp_simple"));
        _setupModificationLogic();

        // Начальное состояние
        componentsRadioButton.setSelected(true);
        _loadSpecialDataToContainer(tableContainer1, "comp_simple");
    }

    // --- ЦЕНТРАЛЬНЫЙ МЕТОД ДЛЯ ВКЛАДКИ 1 (Использование String[]) ---
    private void _loadSpecialDataToContainer(AnchorPane container, String type) {
        if (container == null) return;

        TableView<String[]> table = new TableView<>();
        List<String[]> data = new ArrayList<>();
        String[] headers = new String[]{};

        switch (type) {
            case "comp_simple" -> {
                // Радиобаттон 1: ПРОСТОЙ ВЫВОД (SELECT *)
                headers = new String[]{"ID", "Название", "Тип", "Цена", "Кол-во"};
                List<Component> raw = _componentDAO.getAllNotes();
                for (Component c : raw) {
                    data.add(new String[]{
                            String.valueOf(c.getId()), c.getName(), c.getType(),
                            String.valueOf(c.getPrice()), String.valueOf(c.getQuantity())
                    });
                }
            }
            case "manuf_complex" -> {
                // Радиобаттон 2: СЛОЖНЫЙ ЗАПРОС (CASE + Склейка строк в SQL)
                headers = new String[]{"ID", "Производитель (Инфо)", "Статус"};
                data = _manufacturerDAO.getManufacturersSpecialSelection();
            }
            case "addr_join" -> {
                // Радиобаттон 3: JOIN ЗАПРОС (Связь адресов и имен производителей)
                headers = new String[]{"Город", "Тип контакта", "Имя производителя (из JOIN)"};
                data = _addressDAO.getAddressesWithJoinSelection();
            }
        }

        // Динамическое создание колонок для массива String[]
        table.getColumns().clear();
        for (int i = 0; i < headers.length; i++) {
            final int index = i;
            TableColumn<String[], String> column = new TableColumn<>(headers[i]);
            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                return new SimpleStringProperty((row != null && index < row.length) ? row[index] : "");
            });
            table.getColumns().add(column);
        }

        table.setItems(FXCollections.observableArrayList(data));
        _injectTable(container, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 2 (Select с фильтрами) ---
    private void _handleFullSelect() {
        TableView<Object> table = new TableView<>();
        _setupComponentColumns(table);
        if (!withoutDetailsRadioButton.isSelected()) _addComponentDetailColumns(table);

        List<ComponentProperty> props = new ArrayList<>();
        List<Component> raw = (idComponentTextField1.getText().isEmpty())
                ? _componentDAO.getAllNotes()
                : _componentDAO.search("id", idComponentTextField1.getText());

        raw.forEach(c -> props.add(new ComponentProperty(c)));

        if (!componentCostTextField.getText().isEmpty()) {
            float minPrice = Float.parseFloat(componentCostTextField.getText());
            props.removeIf(p -> p.priceProperty().get() < minPrice);
        }

        table.setItems(FXCollections.observableArrayList(props));
        _injectTable(tableContainer2, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 3 (Подзапросы) ---
    private void _handleSubqueries() {
        TableView<Object> table = new TableView<>();
        _setupComponentColumns(table);

        List<Component> result = uncorrelatedQueryRadioButton.isSelected()
                ? _componentDAO.getComponentsAboveAveragePrice()
                : _componentDAO.getUsedComponents();

        ObservableList<Object> data = FXCollections.observableArrayList();
        result.forEach(c -> data.add(new ComponentProperty(c)));
        table.setItems(data);
        _injectTable(tableContainer3, table);
    }

    // --- ЛОГИКА ВКЛАДКИ 4 (DML) ---
    private void _setupModificationLogic() {
        idComponentTextField2.textProperty().addListener((_, _, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    Component c = _componentDAO.getNoteById(Long.parseLong(newValue));
                    if (c != null) _fillForm(c); else _clearForm();
                } catch (Exception e) { _clearForm(); }
            } else _clearForm();
        });

        requestButton.setOnAction(_ -> {
            _executeDML();
            _loadSpecialDataToContainer(tableContainer4, "comp_simple");
        });
    }

    private void _executeDML() {
        try {
            String name = nameComponentTextField.getText();
            float price = Float.parseFloat(costComponentTextFeild.getText());
            int qty = Integer.parseInt(quantityComponentTextFeild.getText());

            Component c = new Component(name, "Electronic", specificationComponentTextField.getText(),
                    datasheetLinkComponentTextFielf.getText(), price, qty);

            long id = idComponentTextField2.getText().isEmpty() ? 0 : Long.parseLong(idComponentTextField2.getText());

            if (addСomponentsDataRadioButton.isSelected()) _componentDAO.addNote(c);
            else if (changeComponentsDataRadioButton.isSelected() && id != 0) { c.setId(id); _componentDAO.updateNote(c); }
            else if (deleteComponentsDataRadioButton.isSelected() && id != 0) _componentDAO.deleteNote(id);

            _clearForm();
        } catch (Exception e) { System.err.println("Ошибка DML"); }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    private void _setupComponentColumns(TableView<Object> table) {
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> ((ComponentProperty)cd.getValue()).idProperty().asObject());
        TableColumn<Object, String> nameCol = new TableColumn<>("Наименование");
        nameCol.setCellValueFactory(cd -> ((ComponentProperty)cd.getValue()).nameProperty());
        table.getColumns().setAll(idCol, nameCol);
    }

    private void _addComponentDetailColumns(TableView<Object> table) {
        TableColumn<Object, Number> pCol = new TableColumn<>("Цена");
        pCol.setCellValueFactory(cd -> ((ComponentProperty)cd.getValue()).priceProperty());
        table.getColumns().add(pCol);
    }

    private void _fillForm(Component c) {
        nameComponentTextField.setText(c.getName());
        costComponentTextFeild.setText(String.valueOf(c.getPrice()));
        quantityComponentTextFeild.setText(String.valueOf(c.getQuantity()));
        specificationComponentTextField.setText(c.getSpecification());
        datasheetLinkComponentTextFielf.setText(c.getDatasheet_link());
        componentsProjectsListVuew.setItems(FXCollections.observableArrayList(_usageDAO.getProjectsByComponentId(c.getId())));
    }

    private void _clearForm() {
        nameComponentTextField.clear(); costComponentTextFeild.clear();
        quantityComponentTextFeild.clear(); specificationComponentTextField.clear();
        datasheetLinkComponentTextFielf.clear(); componentsProjectsListVuew.getItems().clear();
    }

    private void _injectTable(AnchorPane container, TableView<?> table) {
        container.getChildren().setAll(table);
        AnchorPane.setTopAnchor(table, 0.0); AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0); AnchorPane.setRightAnchor(table, 0.0);
    }

    private void _setupToggleGroups() {
        ToggleGroup g1 = new ToggleGroup();
        componentsRadioButton.setToggleGroup(g1); manufaturersRadioButton.setToggleGroup(g1); manufacturersAddressesRadioButton.setToggleGroup(g1);
        ToggleGroup g2 = new ToggleGroup();
        costRadioButton.setToggleGroup(g2); showAllComponentsRadioButton.setToggleGroup(g2); withoutDetailsRadioButton.setToggleGroup(g2);
        ToggleGroup g3 = new ToggleGroup();
        correlatedQueryRadioButton.setToggleGroup(g3); uncorrelatedQueryRadioButton.setToggleGroup(g3);
        ToggleGroup g4 = new ToggleGroup();
        addСomponentsDataRadioButton.setToggleGroup(g4); changeComponentsDataRadioButton.setToggleGroup(g4); deleteComponentsDataRadioButton.setToggleGroup(g4);
    }
}