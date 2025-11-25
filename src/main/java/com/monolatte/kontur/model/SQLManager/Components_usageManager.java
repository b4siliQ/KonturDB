package com.monolatte.kontur.model.SQLManager;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import com.monolatte.kontur.model.Notes.Component_usage;

public class Components_usageManager implements ISQLManager<Component_usage> {
    final private String _tableName;
    final private Connection _connect;

    public Components_usageManager(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " project_id INTEGER NOT NULL REFERENCES projects(id), component_id INTEGER NOT NULL REFERENCES components(id),"
                + "quantity INTEGER NOT NULL)", this._tableName);
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
        String sqlRequest = String.format("INSERT INTO %s (project_id, component_id, quantity) VALUES (?,?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, component_usage.getProject_id());
            pstmt.setLong(2, component_usage.getComponent_id());
            pstmt.setInt(3, component_usage.getQuantity());

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
        String sqlRequest = String.format("UPDATE %s SET project_id=?, component_id=?, quantity=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, component_usage.getProject_id());
            pstmt.setLong(2, component_usage.getComponent_id());
            pstmt.setInt(3, component_usage.getQuantity());
            pstmt.setLong(4, component_usage.getId());
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

    public List<Component_usage> getUsageByProjectId(int project_id) {
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

    private Component_usage mapResultSetToComponent(ResultSet rs) throws SQLException {
        Component_usage componentUsage = new Component_usage(
        rs.getLong("project_id"),
        rs.getLong("component_id"),
        rs.getInt("quantity"));

        componentUsage.setId(rs.getLong("id"));

        return componentUsage;
    }
}
