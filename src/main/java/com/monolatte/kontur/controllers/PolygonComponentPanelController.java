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
import javafx.beans.property.SimpleLongProperty;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class PolygonComponentPanelController {

    // --- Вкладка 1: Примеры запросов ---
    @FXML private RadioButton componentsRadioButton, manufaturersRadioButton, manufacturersAddressesRadioButton;
    @FXML private AnchorPane tableContainer1; // Не забудьте добавить fx:id в FXML для AnchorPane на 1 вкладке

    // --- Вкладка 2: Полная запись Select ---
    @FXML private TextField idComponentTextField1, componentCostTextField;
    @FXML private CheckBox componentsCostCheckBox, sortedByCostCheckBox;
    @FXML private RadioButton costRadioButton, showAllComponentsRadioButton, withoutDetailsRadioButton;
    @FXML private Button showResult1;
    @FXML private AnchorPane tableContainer2; // Нужно убедиться, что fx:id прописан в последнем AnchorPane VBox-а

    // --- Вкладка 3: Подзапросы ---
    @FXML private TextField idManufacturerTextField;
    @FXML private RadioButton correlatedQueryRadioButton, uncorrelatedQueryRadioButton;
    @FXML private Button showResultButton2;
    @FXML private AnchorPane tableContainer3; // Аналогично, проверьте fx:id в FXML

    // --- Вкладка 4: Изменения данных ---
    @FXML private RadioButton addСomponentsDataRadioButton, changeComponentsDataRadioButton, deleteComponentsDataRadioButton;
    @FXML private TextField idComponentTextField2, nameComponentTextField, datasheetLinkComponentTextFielf, costComponentTextFeild, quantityComponentTextFeild;
    @FXML private TextArea specificationComponentTextField;
    @FXML private CheckBox completeComponentCheckBox;
    @FXML private ListView<Project> componentsProjectsListVuew;
    @FXML private Button requestButton, showResultButton3;
    @FXML private AnchorPane tableContainer4;

    // DAO
    private final ComponentsDAO _componentDAO = SQLTableManager.getInstance().getComponentsManager();
    private final ManufacturerDAO _manufacturerDAO = SQLTableManager.getInstance().getManufacturerDAO();
    private final Components_usageDAO _usageDAO = SQLTableManager.getInstance().getComponentsUsageManager();

    @FXML
    public void initialize() {
        _setupToggleGroups();

        // Вкладка 1
        componentsRadioButton.setOnAction(_ -> _loadData("components", tableContainer1));
        manufaturersRadioButton.setOnAction(_ -> _loadData("manufacturers", tableContainer1));
        manufacturersAddressesRadioButton.setOnAction(_ -> _loadData("addresses", tableContainer1));

        // Вкладка 2
        showResult1.setOnAction(_ -> _handleFullSelect());

        // Вкладка 3
        showResultButton2.setOnAction(_ -> _handleSubqueries());

        // Вкладка 4
        _setupModificationLogic();
    }

    private void _setupModificationLogic() {
        idComponentTextField2.textProperty().addListener((_, _, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) _autoFillComponent(newValue);
            else _clearFields();
        });

        requestButton.setOnAction(_ -> {
            _executeDML();
            _loadData("components", tableContainer4);
        });

        showResultButton3.setOnAction(_ -> _loadData("components", tableContainer4));
    }

    private void _executeDML() {
        try {
            String name = nameComponentTextField.getText();
            String spec = specificationComponentTextField.getText();
            String link = datasheetLinkComponentTextFielf.getText();
            float cost = Float.parseFloat(costComponentTextFeild.getText());
            int quantity = Integer.parseInt(quantityComponentTextFeild.getText());

            // Предполагаем конструктор: Component(name, spec, link, cost, quantity)
            Component comp = new Component(name, spec, link, cost, quantity);

            if (!idComponentTextField2.getText().isEmpty()) {
                long id = Long.parseLong(idComponentTextField2.getText());
                if (changeComponentsDataRadioButton.isSelected()) {
                    comp.setId(id);
                    _componentDAO.updateNote(comp);
                } else if (deleteComponentsDataRadioButton.isSelected()) {
                    _componentDAO.deleteNote(id);
                }
            } else if (addСomponentsDataRadioButton.isSelected()) {
                _componentDAO.addNote(comp);
            }
        } catch (Exception e) { System.err.println("DML Error: " + e.getMessage()); }
    }

    private void _handleFullSelect() {
        TableView<Object> table = new TableView<>();
        _setupComponentBaseColumns(table);

        if (!withoutDetailsRadioButton.isSelected()) {
            _addComponentDetailColumns(table);
        }

        List<Component> list;
        if (showAllComponentsRadioButton.isSelected() || idComponentTextField1.getText().isEmpty()) {
            list = _componentDAO.getAllNotes();
        } else {
            list = _componentDAO.search("id", idComponentTextField1.getText());
        }

        ObservableList<Object> data = FXCollections.observableArrayList(list);

        // Сортировка по цене (Float)
        if (sortedByCostCheckBox.isSelected()) {
            data.sort((o1, o2) -> Float.compare(((Component)o2).getPrice(), ((Component)o1).getPrice()));
        }

        table.setItems(data);
        _injectTable(tableContainer2, table);
    }

    private void _loadData(String type, AnchorPane container) {
        if (container == null) return;
        TableView<Object> table = new TableView<>();
        ObservableList<Object> data = FXCollections.observableArrayList();

        if (type.equals("components")) {
            _setupComponentBaseColumns(table);
            _addComponentDetailColumns(table);
            data.addAll(_componentDAO.getAllNotes());
        } else if (type.equals("manufacturers")) {
            _setupManufacturerColumns(table);
            data.addAll(_manufacturerDAO.getAllNotes());
        }

        table.setItems(data);
        _injectTable(container, table);
    }

    // Настройка колонок
    private void _setupComponentBaseColumns(TableView<Object> table) {
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> new SimpleLongProperty(((Component)cd.getValue()).getId()).asObject());
        TableColumn<Object, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(((Component)cd.getValue()).getName()));
        table.getColumns().setAll(idCol, nameCol);
    }

    private void _addComponentDetailColumns(TableView<Object> table) {
        TableColumn<Object, Number> costCol = new TableColumn<>("Цена");
        costCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleFloatProperty(((Component)cd.getValue()).getPrice()));
        TableColumn<Object, Number> qCol = new TableColumn<>("Кол-во");
        qCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(((Component)cd.getValue()).getQuantity()));
        table.getColumns().addAll(costCol, qCol);
    }

    private void _setupManufacturerColumns(TableView<Object> table) {
        TableColumn<Object, String> nameCol = new TableColumn<>("Производитель");
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(((Manufacturer)cd.getValue()).getName()));
        table.getColumns().setAll(nameCol);
    }

    private void _autoFillComponent(String id) {
        try {
            Component c = _componentDAO.getNoteById(Long.parseLong(id));
            if (c != null) {
                nameComponentTextField.setText(c.getName());
                specificationComponentTextField.setText(c.getSpecification());
                datasheetLinkComponentTextFielf.setText(c.getDatasheet_link());
                costComponentTextFeild.setText(String.valueOf(c.getPrice()));
                quantityComponentTextFeild.setText(String.valueOf(c.getQuantity()));
                completeComponentCheckBox.setSelected(c.getQuantity() <= 0);
                // Загрузка проектов, где юзается компонент
                componentsProjectsListVuew.setItems(FXCollections.observableArrayList(_usageDAO.getProjectsByComponentId(c.getId())));
            }
        } catch (Exception ignored) {}
    }

    private void _handleSubqueries() {
        // Логика аналогична предыдущей панели, но для производителей
    }

    private void _injectTable(AnchorPane container, TableView<Object> table) {
        if (container == null) return;
        container.getChildren().setAll(table);
        AnchorPane.setTopAnchor(table, 0.0); AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0); AnchorPane.setRightAnchor(table, 0.0);
    }

    private void _clearFields() {
        nameComponentTextField.clear(); specificationComponentTextField.clear();
        datasheetLinkComponentTextFielf.clear(); costComponentTextFeild.clear();
        quantityComponentTextFeild.clear(); completeComponentCheckBox.setSelected(false);
    }

    private void _setupToggleGroups() {
        ToggleGroup g1 = new ToggleGroup();
        componentsRadioButton.setToggleGroup(g1); manufaturersRadioButton.setToggleGroup(g1); manufacturersAddressesRadioButton.setToggleGroup(g1);

        ToggleGroup g2 = new ToggleGroup();
        costRadioButton.setToggleGroup(g2); showAllComponentsRadioButton.setToggleGroup(g2); withoutDetailsRadioButton.setToggleGroup(g2);

        ToggleGroup g3 = new ToggleGroup();
        addСomponentsDataRadioButton.setToggleGroup(g3); changeComponentsDataRadioButton.setToggleGroup(g3); deleteComponentsDataRadioButton.setToggleGroup(g3);
    }
}