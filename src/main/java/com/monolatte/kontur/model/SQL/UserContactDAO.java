package com.monolatte.kontur.model.SQL;

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
                + "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    @Override
    public void dropTable() {
        String sqlRequest = String.format("DROP TABLE IF EXISTS %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    @Override
    public void addNote(UserContact userContact) {
        String sqlRequest = String.format("INSERT INTO %s (user_id, contact_type, contact_value) VALUES (?,?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, userContact.getUser_id());
            pstmt.setString(2, userContact.getContact_type());
            pstmt.setString(3, userContact.getContact_value());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) { if (rs.next()) userContact.setId(rs.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    @Override
    public void deleteNote(long id) {
        String sqlRequest = String.format("DELETE FROM %s WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
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
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    @Override
    public List<UserContact> getAllNotes() {
        List<UserContact> notes = new ArrayList<>();
        try (Statement stmt = this._connect.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + this._tableName)) {
            while (rs.next()) notes.add(mapResultSetToUserContact(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    @Override
    public List<UserContact> search(String columnDescription, String searchTerm) {
        List<UserContact> notes = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, columnDescription);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) notes.add(mapResultSetToUserContact(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    public UserContact getUserContactByUserId(long userId) {
        String sqlRequest = String.format(
                "SELECT * FROM %s WHERE user_id = ? LIMIT 1",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserContact userContact = new UserContact(
                            rs.getLong("user_id"),
                            rs.getString("contact_type"),
                            rs.getString("contact_value")
                    );
                    userContact.setId(rs.getLong("id"));
                    return userContact;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения контактов по ID: " + e.getMessage());
        }
        return null;
    }

    // --- Твой специальный запрос (Обновлено: Конкатенация + Порядок) ---
    public List<String[]> getUserContactsSpecial() {
        List<String[]> data = new ArrayList<>();
        // SQL: Выводим Имя и контакт одной строкой через тире
        String sql = "SELECT u.name, (uc.contact_type || ' — ' || uc.contact_value) as formatted_contact " +
                "FROM Users u JOIN " + this._tableName + " uc ON u.id = uc.user_id " +
                "ORDER BY u.name ASC";
        try (Statement stmt = this._connect.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString("name"), rs.getString("formatted_contact")});
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return data;
    }

    private UserContact mapResultSetToUserContact(ResultSet rs) throws SQLException {
        UserContact contact = new UserContact(rs.getLong("user_id"), rs.getString("contact_type"), rs.getString("contact_value"));
        contact.setId(rs.getLong("id"));
        return contact;
    }
}