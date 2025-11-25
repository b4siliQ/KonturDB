package com.monolatte.kontur.model.SQLManager;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.monolatte.kontur.model.Notes.Project;

public class ProjectManager implements ISQLManagerSearchable<Project> {
    final private String _tableName;
    final private Connection _connect;

    ProjectManager(String _tableName, Connection _connect) {
        this._tableName = _tableName;
        this._connect = _connect;
    }

    @Override
    public void createTable() {
        try {
            String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "project_name TEXT NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL,"
                    + "status TEXT NOT NULL)", this._tableName);
            PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " created or already exists");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void dropTable() {
        try {
            String sqlRequest = String.format("DROP TABLE IF EXISTS %s;", this._tableName);
            PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " dropped");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void addNote(Project project) {
        try {
            String sqlRequest = String.format("INSERT INTO %s (project_name, start_date, end_date, status) VALUES (?, ?, ?, ?)", this._tableName);
            PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
            pstmt.setString(1, project.getProject_name());
            pstmt.setString(2, project.getStart_date());
            pstmt.setString(3, project.getEnd_date());
            pstmt.setString(4, project.getStatus());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteNote(int id) {
        try {
            String sqlRequest = String.format("DELETE FROM %s WHERE id = ?", this._tableName);
            PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void updateNote(Project project) {
        try {
            String sqlReuqest = String.format("UPDATE %s SET project_name = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?", this._tableName);
            PreparedStatement pstmt = this._connect.prepareStatement(sqlReuqest);
            pstmt.setString(1, project.getProject_name());
            pstmt.setString(2, project.getStart_date());
            pstmt.setString(3, project.getEnd_date());
            pstmt.setString(4, project.getStatus());
            pstmt.setInt(5, project.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Project> getAllNotes() {
            List<Project> notes = new ArrayList<>();
            String sqlRequest = String.format("SELECT * FROM %s ORDER BY id DESC", this._tableName);
        try {
            PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Project project = mapResultSetToProject(rs);
                notes.add(project);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    @Override
    public List<Project> search(int searchType, String searchTerm) {
        List<Project> notes = new ArrayList<>();
        String columnName = switch (searchType) {
            case 1 -> "project_name";
            case 2 -> "start_date";
            case 3 -> "end_date";
            case 4 -> "status";
            default -> throw new RuntimeException("Invalid search type");
        };
        String sqlRequest = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, columnName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            try(ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToProject(rs));
                }
            }
        } catch (SQLException e) {
            // Логирование и обработка ошибок базы данных
            throw new RuntimeException("Ошибка выполнения поискового запроса: " + e.getMessage(), e);
        }
        return notes;
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        return new Project(
        rs.getInt("id"),
        rs.getString("project_name"),
        rs.getString("start_date"),
        rs.getString("end_date"),
        rs.getString("status"));
    }
}
