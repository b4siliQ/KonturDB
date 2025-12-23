package com.monolatte.kontur.model.SQL;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Component_usage;
import com.monolatte.kontur.model.Notes.Manufacturer;
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
    public void addNote(Component_usage component_usage) {
        String sqlRequest = String.format("INSERT INTO %s (project_id, component_id) VALUES (?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, component_usage.getProject_id());
            pstmt.setLong(2, component_usage.getComponent_id());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        component_usage.setId(id);
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
    public void updateNote(Component_usage component_usage) {
        String sqlRequest = String.format("UPDATE %s SET project_id=?, component_id=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, component_usage.getProject_id());
            pstmt.setLong(2, component_usage.getComponent_id());
            pstmt.setLong(3, component_usage.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Component_usage> getAllNotes() {
        List<Component_usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Component_usage note = mapResultSetToComponent(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    public List<Component_usage> getUsageByProjectId(long project_id) {
        List<Component_usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s WHERE project_id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, project_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Component_usage note = mapResultSetToComponent(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    public List<Project> getProjectsByComponentId(long componentId) {
        List<Project> projects = new ArrayList<>();
        String sqlRequest = String.format(
                "SELECT p.* FROM Projects p " +
                        "INNER JOIN %s mu ON p.id = mu.project_id " +
                        "WHERE mu.component_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, componentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Используем ваш существующий метод маппинга
                    Project project = new Project(
                            //rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getString("status"));

                    project.setId(rs.getLong("id"));
                    projects.add(project);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении проекта для компонента: " + e.getMessage(), e);
        }

        return projects;
    }


    public List<Component> getComponentsByProjectId(long projectId) {
        List<Component> components = new ArrayList<>();

        // ВАЖНО:
        // 1. this._tableName — это таблица компонентов (например, 'components')
        // 2. 'manufacturer_usage' — это имя вашей связующей таблицы.
        String sqlRequest = String.format(
                "SELECT c.* FROM Components c " +
                        "INNER JOIN %s mu ON c.id = mu.component_id " +
                        "WHERE mu.project_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, projectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Используем ваш существующий метод маппинга
                    Component component = new Component(
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("specification"),
                            rs.getString("datasheet_link"),
                            rs.getFloat("price"),
                            rs.getInt("quantity"));

                    component.setId(rs.getLong("id"));
                    components.add(component);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении компонентов для проекта: " + e.getMessage(), e);
        }

        return components;
    }

    public void removeProjectByComponentId(long projectId, long componentId) {
        String sqlRequest = String.format("DELETE FROM %s WHERE project_id = ? AND component_id = ?", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, projectId);
            pstmt.setLong(2, componentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeComponentByProjectId(long componentId, long projectId) {
        String sqlRequest = String.format("DELETE FROM %s WHERE component_id = ? AND project_id = ?", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, componentId);
            pstmt.setLong(2, projectId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Component_usage mapResultSetToComponent(ResultSet rs) throws SQLException {
        Component_usage componentUsage = new Component_usage(
        rs.getLong("project_id"),
        rs.getLong("component_id"));

        componentUsage.setId(rs.getLong("id"));

        return componentUsage;
    }
}
