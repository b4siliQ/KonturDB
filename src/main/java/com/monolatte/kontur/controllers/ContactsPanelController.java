package com.monolatte.kontur.controllers;

import com.monolatte.kontur.model.Notes.User;
import com.monolatte.kontur.model.Notes.UserContact;
import com.monolatte.kontur.model.SQL.SQLTableManager;
import com.monolatte.kontur.model.SQL.UserContactDAO;
import com.monolatte.kontur.model.SQL.UserDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ContactsPanelController {

    @FXML private TextField searchUserField;
    @FXML private ListView<User> usersListView;

    @FXML private TextField userIdTextField;
    @FXML private ComboBox<String> contactTypeComboBox;
    @FXML private TextField contactValueTextField;

    @FXML private TableView<String[]> contactsSummaryTable;
    @FXML private TableColumn<String[], String> userNameColumn;
    @FXML private TableColumn<String[], String> contactInfoColumn;

    private final UserDAO _userDAO = SQLTableManager.getInstance().getUserDAO();
    private final UserContactDAO _contactDAO = SQLTableManager.getInstance().getUserContactDAO();

    @FXML
    public void initialize() {
        // 1. Настройка выпадающего списка (согласно БД CHECK)
        contactTypeComboBox.getItems().addAll("Email", "Телефон", "Социальная сеть");

        // 2. Настройка колонок таблицы для отображения String[] из getUserContactsSpecial()
        userNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        contactInfoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

        // 3. Слушатель выбора в списке пользователей
        usersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                _displayUserContact(newVal);
            }
        });

        // 4. Слушатель для поиска (фильтрация списка пользователей)
        searchUserField.textProperty().addListener((obs, oldVal, newVal) -> {
            _refreshUsersList(newVal);
        });

        // Загрузка начальных данных
        _refreshUsersList("");
        _refreshSummaryTable();
    }

    private void _displayUserContact(User user) {
        userIdTextField.setText(String.valueOf(user.getId()));

        // Ищем контакт в базе для этого пользователя
        UserContact contact = _contactDAO.getUserContactByUserId(user.getId());

        if (contact != null) {
            contactTypeComboBox.setValue(contact.getContact_type());
            contactValueTextField.setText(contact.getContact_value());
        } else {
            // Если контакта нет — очищаем поля для нового ввода
            contactTypeComboBox.setValue("Телефон");
            contactValueTextField.clear();
        }
    }

    @FXML
    public void onSaveContactClicked() {
        User selectedUser = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            _showAlert("Ошибка", "Пожалуйста, выберите пользователя из списка слева.");
            return;
        }

        String type = contactTypeComboBox.getValue();
        String value = contactValueTextField.getText();

        if (value == null || value.trim().isEmpty()) {
            _showAlert("Ошибка", "Значение контакта не может быть пустым.");
            return;
        }

        // Проверяем, существует ли уже контакт
        UserContact existingContact = _contactDAO.getUserContactByUserId(selectedUser.getId());

        if (existingContact != null) {
            // Обновляем старый
            existingContact.setContact_type(type);
            existingContact.setContact_value(value);
            _contactDAO.updateNote(existingContact);
        } else {
            // Создаем новый
            _contactDAO.addNote(new UserContact(selectedUser.getId(), type, value));
        }

        _refreshSummaryTable();
    }

    @FXML
    public void onDeleteContactClicked() {
        User selectedUser = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        UserContact contact = _contactDAO.getUserContactByUserId(selectedUser.getId());
        if (contact != null) {
            _contactDAO.deleteNote(contact.getId());
            contactValueTextField.clear();
            _refreshSummaryTable();
        }
    }

    private void _refreshUsersList(String filter) {
        List<User> users;
        if (filter == null || filter.isEmpty()) {
            users = _userDAO.getAllNotes();
        } else {
            users = _userDAO.search("name", filter); // Используем ваш метод поиска в UserDAO
        }
        usersListView.setItems(FXCollections.observableArrayList(users));
    }

    private void _refreshSummaryTable() {
        // Используем ваш "Специальный запрос" для заполнения таблицы
        List<String[]> specialData = _contactDAO.getUserContactsSpecial();
        contactsSummaryTable.setItems(FXCollections.observableArrayList(specialData));
    }

    private void _showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}