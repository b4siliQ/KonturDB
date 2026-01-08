package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.Manufacturer;
import com.monolatte.kontur.model.Notes.ManufacturerAddresses;
import com.monolatte.kontur.model.SQL.ManufacturerAdressesDAO;
import com.monolatte.kontur.model.SQL.ManufacturerDAO;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ManufacturerAddressController {

    @FXML private TextField searchManufacturerField;
    @FXML private ListView<Manufacturer> manufacturersListView;

    @FXML private TextField componentIdTextField; // Это поле для ID (заглушка или доп. логика)
    @FXML private ComboBox<String> addressTypeComboBox;
    @FXML private TextField cityTextField;

    @FXML private TableView<String[]> addressesSummaryTable;
    @FXML private TableColumn<String[], String> manufacturerNameColumn;
    @FXML private TableColumn<String[], String> addressDetailsColumn;

    private final ManufacturerDAO _manufacturerDAO = SQLTableManager.getInstance().getManufacturerDAO();
    private final ManufacturerAdressesDAO _addressDAO = SQLTableManager.getInstance().getManufacturerAddressDAO();

    @FXML
    public void initialize() {
        // 1. Настройка ComboBox (Типы адресов)
        addressTypeComboBox.getItems().addAll("Юридический", "Фактический", "Склад", "Производство");

        // 2. Настройка колонок таблицы (используем JOIN данные из DAO)
        // Массив из getAddressesWithJoinSelection: [0]=city, [1]=type, [2]=manufacturer_name
        manufacturerNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        addressDetailsColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[1] + ": " + data.getValue()[0]));

        // 3. Слушатель выбора производителя
        manufacturersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                _displayManufacturerAddress(newVal);
            }
        });

        // 4. Поиск по производителям
        searchManufacturerField.textProperty().addListener((obs, oldVal, newVal) -> {
            _refreshManufacturersList(newVal);
        });

        // Начальная загрузка
        _refreshManufacturersList("");
        _refreshSummaryTable();
    }

    private void _displayManufacturerAddress(Manufacturer manufacturer) {
        // В поле ID компонента выводим ID производителя (или иную логику по вашему желанию)
        componentIdTextField.setText(String.valueOf(manufacturer.getId()));

        // Ищем адрес в базе по ID производителя
        ManufacturerAddresses addr = _addressDAO.getManufacturerAddressesByManufacturerId(manufacturer.getId());

        if (addr != null) {
            addressTypeComboBox.setValue(addr.getAddresses_type());
            cityTextField.setText(addr.getCity());
        } else {
            // Если адреса еще нет
            addressTypeComboBox.setValue("Фактический");
            cityTextField.clear();
        }
    }

    @FXML
    public void onSaveAddressClicked() {
        Manufacturer selectedMan = manufacturersListView.getSelectionModel().getSelectedItem();
        if (selectedMan == null) {
            _showAlert("Внимание", "Выберите производителя из списка.");
            return;
        }

        String type = addressTypeComboBox.getValue();
        String city = cityTextField.getText();

        if (city == null || city.trim().isEmpty()) {
            _showAlert("Ошибка", "Поле 'Город' не может быть пустым.");
            return;
        }

        ManufacturerAddresses existingAddr = _addressDAO.getManufacturerAddressesByManufacturerId(selectedMan.getId());

        if (existingAddr != null) {
            // Обновляем
            existingAddr.setAddresses_type(type);
            existingAddr.setCity(city);
            _addressDAO.updateNote(existingAddr);
        } else {
            // Создаем новый
            _addressDAO.addNote(new ManufacturerAddresses(selectedMan.getId(), type, city));
        }

        _refreshSummaryTable();
    }

    @FXML
    public void onDeleteAddressClicked() {
        Manufacturer selectedMan = manufacturersListView.getSelectionModel().getSelectedItem();
        if (selectedMan == null) return;

        ManufacturerAddresses addr = _addressDAO.getManufacturerAddressesByManufacturerId(selectedMan.getId());
        if (addr != null) {
            _addressDAO.deleteNote(addr.getId());
            cityTextField.clear();
            _refreshSummaryTable();
        }
    }

    private void _refreshManufacturersList(String filter) {
        List<Manufacturer> list;
        if (filter == null || filter.isEmpty()) {
            list = _manufacturerDAO.getAllNotes();
        } else {
            list = _manufacturerDAO.search("name", filter);
        }
        manufacturersListView.setItems(FXCollections.observableArrayList(list));
    }

    private void _refreshSummaryTable() {
        // Используем метод с JOIN для красивой таблицы
        List<String[]> data = _addressDAO.getAddressesWithJoinSelection();
        addressesSummaryTable.setItems(FXCollections.observableArrayList(data));
    }

    private void _showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}