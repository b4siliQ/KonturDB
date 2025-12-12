package com.monolatte.kontur.model.SQL;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import com.monolatte.kontur.model.Notes.Component;

public class ComponentsDAO implements ISQLDAOSearchable<Component> {
    final private String _tableName;
    final private Connection _connect;

    public ComponentsDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s (id INTEGER PRIMARY KEY AUTOINCREMENT," // <-- Добавлен пробел перед (id
                + " name TEXT NOT NULL, type TEXT NOT NULL, specification TEXT NOT NULL,"
                + "datasheet_link TEXT NOT NULL, price REAL NOT NULL, quantity INTEGER NOT NULL)", this._tableName);
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
    public void addNote(Component component) {
        String sqlRequest = String.format("INSERT INTO %s (name, type, specification, datasheet_link, price, quantity) VALUES(?,?,?,?,?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, component.getName());
            pstmt.setString(2, component.getType());
            pstmt.setString(3, component.getSpecification());
            pstmt.setString(4, component.getDatasheet_link());
            pstmt.setFloat(5, component.getPrice());
            pstmt.setInt(6, component.getQuantity());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        component.setId(id);
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
    public void updateNote(Component component) {
        String sqlRequest = String.format("UPDATE %s SET name = ?, type = ?, specification = ?,"
                + "datasheet_link = ?, price = ?, quantity = ? WHERE id = ?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setString(1, component.getName());
            pstmt.setString(2, component.getType());
            pstmt.setString(3, component.getSpecification());
            pstmt.setString(4, component.getDatasheet_link());
            pstmt.setFloat(5, component.getPrice());
            pstmt.setInt(6, component.getQuantity());
            pstmt.setLong(7, component.getId());
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " updated");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Component> search(String columnDescription, String searchTerm) {
            List<Component> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, columnDescription);
            try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
//                if (SearchType == 4) {
//                    pstmt.setFloat(1, Integer.parseInt(searchTerm));
//                }
//                else {
//                    pstmt.setString(1, "%" + searchTerm + "%");
//                }
                pstmt.setString(1, "%" + searchTerm + "%");
                try(ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        notes.add(mapResultSetToComponent(rs));
                    }
                }
            } catch (SQLException e) {
                // Логирование и обработка ошибок базы данных
                throw new RuntimeException("Ошибка выполнения поискового запроса: " + e.getMessage(), e);
            } catch (NumberFormatException e) {
                // Обработка случая, когда searchTerm для цены не является числом
                throw new IllegalArgumentException("Цена должна быть числом.", e);
            }
        return notes;
    }

    @Override
    public List<Component> getAllNotes() {
        List<Component> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s ORDER BY id DESC", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Component component = mapResultSetToComponent(rs);
                notes.add(component);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    private Component mapResultSetToComponent(ResultSet rs) throws SQLException {
        Component component = new Component(
        rs.getString("name"),
        rs.getString("type"),
        rs.getString("specification"),
        rs.getString("datasheet_link"),
        rs.getFloat("price"),
        rs.getInt("quantity"));

        component.setId(rs.getLong("id"));

        return component;
    }
}