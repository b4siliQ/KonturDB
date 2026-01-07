package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User_usageDAO implements ISQLDAOSearchable<User_usage> {
    final private String _tableName;
    final private Connection _connect;

    public User_usageDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "project_id INTEGER NOT NULL, "
                        + "user_id INTEGER NOT NULL, "
                        + "FOREIGN KEY(project_id) REFERENCES Projects(id) ON DELETE CASCADE, "
                        + "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE)",
                this._tableName);
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
    public void addNote(User_usage user_usage) {
        String sqlRequest = String.format("INSERT INTO %s (project_id, user_id) VALUES (?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, user_usage.getProject_id());
            pstmt.setLong(2, user_usage.getUser_id());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) user_usage.setId(genKeys.getLong(1));
                }
            }
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
    public void updateNote(User_usage user_usage) {
        String sqlRequest = String.format("UPDATE %s SET project_id=?, user_id=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, user_usage.getProject_id());
            pstmt.setLong(2, user_usage.getUser_id());
            pstmt.setLong(3, user_usage.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
    }

    @Override
    public List<User_usage> getAllNotes() {
        List<User_usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) notes.add(mapResultSetToUserUsage(rs));
        } catch (SQLException e) { throw new RuntimeException(e.getMessage()); }
        return notes;
    }

    @Override
    public List<User_usage> search(String columnDescription, String searchTerm) {
        List<User_usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s WHERE %s = ?", this._tableName, columnDescription);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setString(1, searchTerm);
            try(ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) notes.add(mapResultSetToUserUsage(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    public List<Project> getProjectsByUserId(long userId) {
        List<Project> projects = new ArrayList<>();
        // Используем INNER JOIN, чтобы найти все проекты, на которых висит этот юзер
        String sqlRequest = String.format(
                "SELECT p.* FROM Projects p " +
                        "INNER JOIN %s uu ON p.id = uu.project_id " +
                        "WHERE uu.user_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project(
                            rs.getString("project_name"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getString("status"));
                    project.setId(rs.getLong("id"));
                    projects.add(project);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения проектов юзера: " + e.getMessage());
        }
        return projects;
    }

    public List<User> getUsersByProjectId(long projectId) {
        List<User> users = new ArrayList<>();
        // SQL запрос для получения всех пользователей, привязанных к конкретному проекту
        String sqlRequest = String.format(
                "SELECT u.* FROM Users u " +
                        "INNER JOIN %s uu ON u.id = uu.user_id " +
                        "WHERE uu.project_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, projectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Создаем объект пользователя из данных БД
                    User user = new User(
                            rs.getString("name"),
                            rs.getString("description"));

                    user.setId(rs.getLong("id"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении пользователей для проекта: " + e.getMessage(), e);
        }

        return users;
    }

    // --- Твой специальный JOIN (Обновлено: Группировка + Подсчет) ---
    public List<String[]> getUsersAndProjectsJoin() {
        List<String[]> data = new ArrayList<>();
        String sql = "SELECT u.name, 'Проектов: ' || COUNT(uu.project_id) as count_info " +
                "FROM Users u " +
                "LEFT JOIN " + this._tableName + " uu ON u.id = uu.user_id " +
                "GROUP BY u.id, u.name";
        try (Statement stmt = this._connect.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString("name"), rs.getString("count_info")});
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return data;
    }

    private User_usage mapResultSetToUserUsage(ResultSet rs) throws SQLException {
        User_usage usage = new User_usage(rs.getLong("project_id"), rs.getLong("user_id"));
        usage.setId(rs.getLong("id"));
        return usage;
    }
}