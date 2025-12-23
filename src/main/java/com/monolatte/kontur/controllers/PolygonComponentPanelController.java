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
    @FXML private TextField idComponentTextField;
    @FXML private RadioButton correlatedQueryRadioButton, uncorrelatedQueryRadioButton;
    @FXML private Button showResultButton2;
    @FXML private AnchorPane tableContainer3;

    // --- Вкладка 4: Изменения данных (DML) ---
    @FXML private RadioButton addСomponentsDataRadioButton, changeComponentsDataRadioButton, deleteComponentsDataRadioButton;
    @FXML private TextField idComponentTextField2, nameComponentTextField, datasheetLinkComponentTextFielf, costComponentTextFeild, quantityComponentTextFeild;
    @FXML private TextArea specificationComponentTextField;
    @FXML private CheckBox completeComponentCheckBox;
    @FXML private ListView<Project> componentsProjectsListVuew;
    @FXML private Button requestButton;
    @FXML private Button showResultButton3;
    @FXML private AnchorPane tableContainer4;

    // --- DAO Сервисы (Точные названия из вашего SQLTableManager) ---
    private final ComponentsDAO _componentDAO = SQLTableManager.getInstance().getComponentsManager();
    private final Components_usageDAO _usageDAO = SQLTableManager.getInstance().getComponentsUsageManager();
    private final ManufacturerAdressesDAO _addressDAO = SQLTableManager.getInstance().getManufacturerAddressDAO();
    private final ManufacturerDAO _manufacturerDAO = SQLTableManager.getInstance().getManufacturerDAO();

    @FXML
    public void initialize() {
        _setupToggleGroups();

        // 1. Простые выборки
        componentsRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "components"));
        manufaturersRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "manufacturers"));
        manufacturersAddressesRadioButton.setOnAction(_ -> _loadDataToContainer(tableContainer1, "addresses"));

        // 2. Сложный Select
        showResult1.setOnAction(_ -> _handleFullSelect());

        // 3. Подзапросы (SQL логика в DAO)
        showResultButton2.setOnAction(_ -> _handleSubqueries());

        // 4. Модификация (DML)
        _setupModificationLogic();

        // Старт по умолчанию
        componentsRadioButton.setSelected(true);
        _loadDataToContainer(tableContainer1, "components");
    }

    private void _setupModificationLogic() {
        // Автозаполнение при вводе ID
        idComponentTextField2.textProperty().addListener((_, _, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    Component c = _componentDAO.getNoteById(Long.parseLong(newValue));
                    if (c != null) _fillForm(c); else _clearForm();
                } catch (Exception e) { _clearForm(); }
            } else _clearForm();
        });

        // INSERT / UPDATE / DELETE
        requestButton.setOnAction(_ -> {
            _executeChange();
            _loadDataToContainer(tableContainer4, "components");
        });

        // Кнопка "Показать список"
        showResultButton3.setOnAction(_ -> _loadDataToContainer(tableContainer4, "components"));
    }

    private void _executeChange() {
        try {
            String name = nameComponentTextField.getText();
            String spec = specificationComponentTextField.getText();
            String link = datasheetLinkComponentTextFielf.getText();
            float price = Float.parseFloat(costComponentTextFeild.getText().isEmpty() ? "0" : costComponentTextFeild.getText());
            int qty = Integer.parseInt(quantityComponentTextFeild.getText().isEmpty() ? "0" : quantityComponentTextFeild.getText());
            if (completeComponentCheckBox.isSelected()) qty = 0;

            Component component = new Component(name, "Electronic", spec, link, price, qty);
            long id = idComponentTextField2.getText().isEmpty() ? 0 : Long.parseLong(idComponentTextField2.getText());

            if (addСomponentsDataRadioButton.isSelected()) {
                _componentDAO.addNote(component);
            } else if (changeComponentsDataRadioButton.isSelected() && id != 0) {
                component.setId(id);
                _componentDAO.updateNote(component);
            } else if (deleteComponentsDataRadioButton.isSelected() && id != 0) {
                _componentDAO.deleteNote(id);
            }
        } catch (Exception e) { System.err.println("DML Error: " + e.getMessage()); }
    }

    private void _handleFullSelect() {
        TableView<Object> table = new TableView<>();
        _setupComponentColumns(table);
        if (!withoutDetailsRadioButton.isSelected()) _addComponentDetailColumns(table);

        List<ComponentProperty> props = new ArrayList<>();
        _componentDAO.getAllNotes().forEach(c -> props.add(new ComponentProperty(c)));

        if (!componentCostTextField.getText().isEmpty()) {
            float min = Float.parseFloat(componentCostTextField.getText());
            props.removeIf(p -> p.priceProperty().get() < min);
        }
        if (sortedByCostCheckBox.isSelected()) {
            props.sort((a, b) -> Float.compare(b.priceProperty().get(), a.priceProperty().get()));
        }
        table.setItems(FXCollections.observableArrayList(props));
        _injectTable(tableContainer2, table);
    }

    private void _handleSubqueries() {
        TableView<Object> table = new TableView<>();
        _setupComponentColumns(table);
        _addComponentDetailColumns(table);

        List<Component> result = uncorrelatedQueryRadioButton.isSelected()
                ? _componentDAO.getComponentsAboveAveragePrice()
                : _componentDAO.getUsedComponents();

        ObservableList<Object> data = FXCollections.observableArrayList();
        result.forEach(c -> data.add(new ComponentProperty(c)));
        table.setItems(data);
        _injectTable(tableContainer3, table);
    }

    private void _loadDataToContainer(AnchorPane container, String type) {
        if (container == null) return;
        TableView<Object> table = new TableView<>();
        ObservableList<Object> data = FXCollections.observableArrayList();

        switch (type) {
            case "components" -> {
                _setupComponentColumns(table);
                _addComponentDetailColumns(table);
                _componentDAO.getAllNotes().forEach(c -> data.add(new ComponentProperty(c)));
            }
            case "manufacturers" -> {
                TableColumn<Object, String> col = new TableColumn<>("Наименование");
                col.setCellValueFactory(cd -> new SimpleStringProperty(((Manufacturer)cd.getValue()).getName()));
                table.getColumns().add(col);
                data.addAll(_manufacturerDAO.getAllNotes());
            }
            case "addresses" -> {
                TableColumn<Object, String> cityCol = new TableColumn<>("Город");
                cityCol.setCellValueFactory(cd -> new SimpleStringProperty(((ManufacturerAddresses)cd.getValue()).getCity()));
                TableColumn<Object, String> typeCol = new TableColumn<>("Тип");
                typeCol.setCellValueFactory(cd -> new SimpleStringProperty(((ManufacturerAddresses)cd.getValue()).getAddresses_type()));
                table.getColumns().addAll(cityCol, typeCol);
                data.addAll(_addressDAO.getAllNotes());
            }
        }
        table.setItems(data);
        _injectTable(container, table);
    }

    private void _setupComponentColumns(TableView<Object> table) {
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> ((ComponentProperty)cd.getValue()).idProperty().asObject());
        TableColumn<Object, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cd -> ((ComponentProperty)cd.getValue()).nameProperty());
        table.getColumns().setAll(idCol, nameCol);
    }

    private void _addComponentDetailColumns(TableView<Object> table) {
        TableColumn<Object, Number> pCol = new TableColumn<>("Цена");
        pCol.setCellValueFactory(cd -> ((ComponentProperty)cd.getValue()).priceProperty());
        TableColumn<Object, Number> qCol = new TableColumn<>("Кол-во");
        qCol.setCellValueFactory(cd -> ((ComponentProperty)cd.getValue()).quantityProperty());
        table.getColumns().addAll(pCol, qCol);
    }

    private void _fillForm(Component c) {
        nameComponentTextField.setText(c.getName());
        specificationComponentTextField.setText(c.getSpecification());
        datasheetLinkComponentTextFielf.setText(c.getDatasheet_link());
        costComponentTextFeild.setText(String.valueOf(c.getPrice()));
        quantityComponentTextFeild.setText(String.valueOf(c.getQuantity()));
        completeComponentCheckBox.setSelected(c.getQuantity() <= 0);
        componentsProjectsListVuew.setItems(FXCollections.observableArrayList(_usageDAO.getProjectsByComponentId(c.getId())));
    }

    private void _clearForm() {
        nameComponentTextField.clear(); specificationComponentTextField.clear();
        datasheetLinkComponentTextFielf.clear(); costComponentTextFeild.clear();
        quantityComponentTextFeild.clear(); completeComponentCheckBox.setSelected(false);
        componentsProjectsListVuew.getItems().clear();
    }

    private void _injectTable(AnchorPane container, TableView<Object> table) {
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