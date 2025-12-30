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
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name TEXT NOT NULL, type TEXT NOT NULL, specification TEXT NOT NULL,"
                + "datasheet_link TEXT NOT NULL, price REAL NOT NULL, quantity INTEGER NOT NULL)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " created/verified");
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void dropTable() {
        String sqlRequest = String.format("DROP TABLE IF EXISTS %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void addNote(Component component) {
        String sql = String.format("INSERT INTO %s (name, type, specification, datasheet_link, price, quantity) VALUES(?,?,?,?,?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, component.getName());
            pstmt.setString(2, component.getType());
            pstmt.setString(3, component.getSpecification());
            pstmt.setString(4, component.getDatasheet_link());
            pstmt.setFloat(5, component.getPrice());
            pstmt.setInt(6, component.getQuantity());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) component.setId(keys.getLong(1));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void deleteNote(long id) {
        String sql = String.format("DELETE FROM %s WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateNote(Component component) {
        String sql = String.format("UPDATE %s SET name=?, type=?, specification=?, datasheet_link=?, price=?, quantity=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setString(1, component.getName());
            pstmt.setString(2, component.getType());
            pstmt.setString(3, component.getSpecification());
            pstmt.setString(4, component.getDatasheet_link());
            pstmt.setFloat(5, component.getPrice());
            pstmt.setInt(6, component.getQuantity());
            pstmt.setLong(7, component.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Component> getAllNotes() {
        List<Component> notes = new ArrayList<>();
        String sql = "SELECT * FROM " + this._tableName + " ORDER BY id DESC";
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) notes.add(mapResultSetToComponent(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    @Override
    public List<Component> search(String col, String term) {
        List<Component> notes = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, col);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) notes.add(mapResultSetToComponent(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    public Component getNoteById(long id) {
        String sql = "SELECT * FROM " + this._tableName + " WHERE id = ?";
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) { if (rs.next()) return mapResultSetToComponent(rs); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    public List<Component> getComponentsAboveAveragePrice() {
        List<Component> notes = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE price > (SELECT AVG(price) FROM %s)", this._tableName, this._tableName);
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) notes.add(mapResultSetToComponent(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    public List<Component> getUsedComponents() {
        List<Component> notes = new ArrayList<>();
        String sql = "SELECT * FROM Components c WHERE EXISTS (SELECT 1 FROM Component_Usage cu WHERE cu.component_id = c.id)";
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) notes.add(mapResultSetToComponent(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    // Специальная выборка для вкладки 1 (Аналог "Блюда")
    public List<String[]> getComponentsSpecialSelection() {
        List<String[]> data = new ArrayList<>();
        // SQL: Склеиваем имя и тип, считаем цену с наценкой 15%
        String sql = "SELECT id, (name || ' [' || type || ']') as info, " +
                "price, (price * 1.15) as price_with_tax, " +
                "CASE WHEN quantity > 0 THEN 'В наличии' ELSE 'Нет' END as status " +
                "FROM " + this._tableName;
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("id"), rs.getString("info"),
                        rs.getString("price"), rs.getString("price_with_tax"), rs.getString("status")
                });
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return data;
    }

    private Component mapResultSetToComponent(ResultSet rs) throws SQLException {
        Component c = new Component(rs.getString("name"), rs.getString("type"), rs.getString("specification"),
                rs.getString("datasheet_link"), rs.getFloat("price"), rs.getInt("quantity"));
        c.setId(rs.getLong("id"));
        return c;
    }
}