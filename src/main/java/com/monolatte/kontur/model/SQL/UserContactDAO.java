package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.User;
import com.monolatte.kontur.model.Notes.UserContact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserContactDAO implements ISQLDAOSearchable<UserContact> {
    final private String _tableName;
    final private Connection _connect;

    public UserContactDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "contact_type TEXT NOT NULL, "
                        + "contact_value TEXT NOT NULL, "
                        + "user_id INTEGER NOT NULL, "
                        + "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE)",
                this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " created or already exists");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void dropTable() {
        String sqlRequest = String.format("DROP TABLE IF EXISTS %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " dropped");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void addNote(UserContact userContact) {
        String sqlRequest = String.format("INSERT INTO %s (user_id, contact_type, contact_value) VALUES (?,?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, userContact.getUser_id());
            pstmt.setString(2, userContact.getContact_type());
            pstmt.setString(3, userContact.getContact_value());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        userContact.setId(id);
                        System.out.printf("Note has inserted in %s with %d%n id\n",
                                this._tableName,
                                id
                        );
                    } else {
                        System.err.println("Warning! Note has inserted, but without generated id\n");
                    }
                }
            } else {
                System.err.printf("Alert! Note hasn't inserted in %s\n", this._tableName);
            }
            System.out.println("Table " + this._tableName + " added");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteNote(long id) {
        String sqlRequest = String.format("DELETE FROM %s WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " deleted");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void updateNote(UserContact userContact) {
        String sqlRequest = String.format("UPDATE %s SET user_id=?, contact_type=?, contact_value=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, userContact.getUser_id());
            pstmt.setString(2, userContact.getContact_type());
            pstmt.setString(3, userContact.getContact_value());
            pstmt.setLong(4, userContact.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<UserContact> getAllNotes() {
        List<UserContact> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                UserContact note = mapResultSetToUserContact(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    @Override
    public List<UserContact> search(String columnDescription, String searchTerm) {
        List<UserContact> notes = new ArrayList<>();

        long idToSearch;
        try {
            idToSearch = Long.parseLong(searchTerm);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Для поиска по ID введите число. Получено: " + searchTerm);
            return notes;
        }

        String sqlRequest = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, columnDescription);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, idToSearch);
            try(ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToUserContact(rs));
                }
            }
        } catch (SQLException e) {
            // Логирование и обработка ошибок базы данных
            throw new RuntimeException("Ошибка выполнения поискового запроса: " + e.getMessage(), e);
        }
        return notes;
    }

    public List<UserContact> getUserContactByUserId(long userId) {
        List<UserContact> userContacts = new ArrayList<>();
        String sqlRequest = String.format(
                "SELECT u.* FROM UserContact u " +
                        "INNER JOIN %s mu ON u.id = mu.id " +
                        "WHERE mu.user_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Используем ваш существующий метод маппинга
                    UserContact userContact = new UserContact(
                            rs.getLong("user_id"),
                            rs.getString("contact_type"),
                            rs.getString("contact_value")
                    );

                    userContact.setId(rs.getLong("id"));
                    userContacts.add(userContact);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении проекта для пользователей: " + e.getMessage(), e);
        }

        return userContacts;
    }


    public List<User> getUsersByUserContactId(long contactId) {
        List<User> users = new ArrayList<>();
        String sqlRequest = String.format(
                "SELECT u.* FROM Users u " +
                        "INNER JOIN %s mu ON u.id = mu.user_id " +
                        "WHERE mu.id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, contactId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                            rs.getString("name"),
                            rs.getString("description"));

                    user.setId(rs.getLong("id"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении пользователей для проекта: " + e.getMessage(), e);
        }

        return users;
    }

    public void removeContactByUserId(long userId) {
        String sqlRequest = String.format("DELETE FROM %s WHERE user_id = ?",
                this._tableName
        );
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UserContact mapResultSetToUserContact(ResultSet rs) throws SQLException {
        UserContact userContact = new UserContact(
                rs.getLong("user_id"),
                rs.getString("contact_type"),
                rs.getString("contact_value")
        );

        userContact.setId(rs.getLong("id"));

        return userContact;
    }
}
