package com.monolatte.kontur.model.SQL;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import com.monolatte.kontur.model.Notes.Project;

public class ProjectDAO implements ISQLDAOSearchable<Project> {
    final private String _tableName;
    final private Connection _connect;

    public ProjectDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "project_name TEXT NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL,"
                + "status TEXT NOT NULL)", this._tableName);
        try (Statement stmt = this._connect.createStatement()) { stmt.executeUpdate(sql); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void dropTable() {
        try (Statement stmt = this._connect.createStatement()) { stmt.executeUpdate("DROP TABLE IF EXISTS " + this._tableName); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void addNote(Project project) {
        String sql = String.format("INSERT INTO %s (project_name, start_date, end_date, status) VALUES (?,?,?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, project.getProject_name());
            pstmt.setString(2, project.getStart_date());
            pstmt.setString(3, project.getEnd_date());
            pstmt.setString(4, project.getStatus());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) { if (rs.next()) project.setId(rs.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void deleteNote(long id) {
        try (PreparedStatement pstmt = this._connect.prepareStatement("DELETE FROM " + this._tableName + " WHERE id=?")) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateNote(Project project) {
        String sql = String.format("UPDATE %s SET project_name=?, start_date=?, end_date=?, status=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setString(1, project.getProject_name());
            pstmt.setString(2, project.getStart_date());
            pstmt.setString(3, project.getEnd_date());
            pstmt.setString(4, project.getStatus());
            pstmt.setLong(5, project.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Project> getAllNotes() {
        List<Project> notes = new ArrayList<>();
        try (Statement stmt = this._connect.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + this._tableName + " ORDER BY id DESC")) {
            while (rs.next()) notes.add(mapResultSetToProject(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    @Override
    public List<Project> search(String column, String term) {
        List<Project> notes = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, column);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) notes.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    public Project getNoteById(long id) {
        try (PreparedStatement pstmt = this._connect.prepareStatement("SELECT * FROM " + this._tableName + " WHERE id=?")) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) { if (rs.next()) return mapResultSetToProject(rs); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    // --- Вкладка 3: Некоррелированный подзапрос (Сложный расчет цен) ---
    public List<Project> getProjectsWithAboveAverageCost() {
        List<Project> projects = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE id IN (SELECT project_id FROM Component_Usage " +
                "GROUP BY project_id HAVING SUM((SELECT price FROM Components WHERE id=component_id)) > " +
                "(SELECT AVG(price) FROM Components))", this._tableName);
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) projects.add(mapResultSetToProject(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return projects;
    }

    // --- Вкладка 3: Коррелированный подзапрос (EXISTS) ---
    public List<Project> getProjectsWithExpensiveComponents() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM " + this._tableName + " p WHERE EXISTS " +
                "(SELECT 1 FROM Component_Usage cu JOIN Components c ON cu.component_id = c.id " +
                "WHERE cu.project_id = p.id AND c.price > 1000)";
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) projects.add(mapResultSetToProject(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return projects;
    }

    // --- Вкладка 1: Проекты (Обновлено: Логика CASE для статусов) ---
    public List<String[]> getProjectsSpecialSelection() {
        List<String[]> data = new ArrayList<>();
        // Уникальный запрос: Склеиваем данные и вычисляем "Срочность" на лету
        String sql = "SELECT id, (project_name || ' [' || status || ']') as title, " +
                "start_date, " +
                "CASE " +
                " WHEN end_date < date('now') AND status != 'COMPLETED' THEN '⚠️ ПРОСРОЧЕНО' " +
                " WHEN status = 'COMPLETED' THEN '✅ В архиве' " +
                " ELSE '⚙️ В работе' " +
                "END as urgency " +
                "FROM " + this._tableName;
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString("id"), rs.getString("title"), rs.getString("start_date"), rs.getString("urgency")});
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return data;
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project p = new Project(rs.getString("project_name"), rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"));
        p.setId(rs.getLong("id"));
        return p;
    }
}