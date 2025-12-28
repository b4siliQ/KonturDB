package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.Manufacturer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManufacturerDAO implements ISQLDAOSearchable<Manufacturer> {
    final private String _tableName;
    final private Connection _connect;

    public ManufacturerDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    // --- ТОТ САМЫЙ МЕТОД, КОТОРОГО НЕ ХВАТАЛО ---
    public Manufacturer getNoteById(long id) {
        String sql = String.format("SELECT * FROM %s WHERE id = ?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Manufacturer m = new Manufacturer(rs.getString("name"), rs.getString("description"));
                    m.setId(rs.getLong("id"));
                    return m;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении производителя по ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createTable() {
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT)", this._tableName);
        try (Statement stmt = _connect.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void addNote(Manufacturer m) {
        String sql = String.format("INSERT INTO %s (name, description) VALUES (?,?)", this._tableName);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getDescription());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) { if (keys.next()) m.setId(keys.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateNote(Manufacturer m) {
        String sql = String.format("UPDATE %s SET name=?, description=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getDescription());
            pstmt.setLong(3, m.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void deleteNote(long id) {
        try (PreparedStatement pstmt = _connect.prepareStatement("DELETE FROM " + _tableName + " WHERE id=?")) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Manufacturer> getAllNotes() {
        List<Manufacturer> notes = new ArrayList<>();
        try (Statement stmt = _connect.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM " + _tableName)) {
            while (rs.next()) {
                Manufacturer m = new Manufacturer(rs.getString("name"), rs.getString("description"));
                m.setId(rs.getLong("id"));
                notes.add(m);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    @Override
    public List<Manufacturer> search(String col, String term) {
        List<Manufacturer> notes = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE %s LIKE ?", _tableName, col);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Manufacturer m = new Manufacturer(rs.getString("name"), rs.getString("description"));
                    m.setId(rs.getLong("id"));
                    notes.add(m);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    @Override
    public void dropTable() {
        try (Statement stmt = _connect.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + _tableName);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}