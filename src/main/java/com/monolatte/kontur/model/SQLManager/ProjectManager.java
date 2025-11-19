package com.monolatte.kontur.model.SQLManager;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.monolatte.kontur.model.SQLManager.Notes.Project;

public class ProjectManager implements ISQLManager<Project> {
    final private String _tableName;
    final private Connection _connect;

    ProjectManager(String _tableName, Connection _connect) {
        this._tableName = _tableName;
        this._connect = _connect;
    }

    @Override
    public void createTable() {
        try {
            String sqlRequest = String.format("CREATE TABLE IF NOT EXIST %s(id INTEGER PRIMARY KEY AUTOINCREMENT)"
                    + "project_name TEXT NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL"
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
            String sqlRequest = String.format("DROP TABLE IF EXIST %s;", this._tableName);
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
            String sqlReuqest = String.format("UPDATE %s SET project_name = ?, start_date = ?, end_date = ?, status = ?", this._tableName);
            PreparedStatement pstmt = this._connect.prepareStatement(sqlReuqest);
            pstmt.setString(1, project.getProject_name());
            pstmt.setString(2, project.getStart_date());
            pstmt.setString(3, project.getEnd_date());
            pstmt.setString(4, project.getStatus());
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

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setProject_name(rs.getString("project_name"));
        project.setStart_date(rs.getString("start_date"));
        project.setEnd_date(rs.getString("end_date"));
        project.setStatus(rs.getString("status"));
        return project;
    }
}
