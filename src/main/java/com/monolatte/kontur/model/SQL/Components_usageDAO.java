package com.monolatte.kontur.model.SQL;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Component_usage;
import com.monolatte.kontur.model.Notes.Project;

public class Components_usageDAO implements ISQLDAO<Component_usage> {
    final private String _tableName;
    final private Connection _connect;

    public Components_usageDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "project_id INTEGER NOT NULL, "
                + "component_id INTEGER NOT NULL, "
                + "FOREIGN KEY(project_id) REFERENCES Projects(id) ON DELETE CASCADE, "
                + "FOREIGN KEY(component_id) REFERENCES Components(id) ON DELETE CASCADE)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " created");
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void dropTable() {
        try (Statement stmt = _connect.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + _tableName);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void addNote(Component_usage cu) {
        String sql = String.format("INSERT INTO %s (project_id, component_id) VALUES (?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, cu.getProject_id());
            pstmt.setLong(2, cu.getComponent_id());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) { if (keys.next()) cu.setId(keys.getLong(1)); }
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
    public void updateNote(Component_usage cu) {
        String sql = String.format("UPDATE %s SET project_id=?, component_id=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setLong(1, cu.getProject_id());
            pstmt.setLong(2, cu.getComponent_id());
            pstmt.setLong(3, cu.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Component_usage> getAllNotes() {
        List<Component_usage> notes = new ArrayList<>();
        try (Statement stmt = _connect.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM " + _tableName)) {
            while (rs.next()) {
                Component_usage cu = new Component_usage(rs.getLong("project_id"), rs.getLong("component_id"));
                cu.setId(rs.getLong("id"));
                notes.add(cu);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    // Методы для Контроллера
    public List<Project> getProjectsByComponentId(long componentId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT p.* FROM Projects p INNER JOIN " + this._tableName + " cu ON p.id = cu.project_id WHERE cu.component_id = ?";
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setLong(1, componentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Project p = new Project(rs.getString("project_name"), rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"));
                    p.setId(rs.getLong("id"));
                    projects.add(p);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return projects;
    }

    public List<Component> getComponentsByProjectId(long projectId) {
        List<Component> components = new ArrayList<>();
        String sql = "SELECT c.* FROM Components c INNER JOIN " + this._tableName + " cu ON c.id = cu.component_id WHERE cu.project_id = ?";
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setLong(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Component c = new Component(rs.getString("name"), rs.getString("type"), rs.getString("specification"), rs.getString("datasheet_link"), rs.getFloat("price"), rs.getInt("quantity"));
                    c.setId(rs.getLong("id"));
                    components.add(c);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return components;
    }

    public void removeComponentByProjectId(long componentId, long projectId) {
        String sql = String.format("DELETE FROM %s WHERE component_id = ? AND project_id = ?", this._tableName);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setLong(1, componentId);
            pstmt.setLong(2, projectId);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}