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
    public void addNote(User_usage user_usage) {
        String sqlRequest = String.format("INSERT INTO %s (project_id, user_id) VALUES (?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, user_usage.getProject_id());
            pstmt.setLong(2, user_usage.getUser_id());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        user_usage.setId(id);
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
    public void updateNote(User_usage user_usage) {
        String sqlRequest = String.format("UPDATE %s SET project_id=?, user_id=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, user_usage.getProject_id());
            pstmt.setLong(2, user_usage.getUser_id());
            pstmt.setLong(3, user_usage.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<User_usage> getAllNotes() {
        List<User_usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User_usage note = mapResultSetToUser(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    @Override
    public List<User_usage> search(int searchType, String searchTerm) {
        List<User_usage> notes = new ArrayList<>();

        long idToSearch;
        try {
            idToSearch = Long.parseLong(searchTerm);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Для поиска по ID введите число. Получено: " + searchTerm);
            return notes;
        }

        String columnName = switch (searchType) {
            case 1 -> "project_id";
            case 2 -> "user_id";
            case 3 -> "id";
            default -> throw new RuntimeException("Invalid search type");
        };
        String sqlRequest = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, columnName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, idToSearch);
            try(ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            // Логирование и обработка ошибок базы данных
            throw new RuntimeException("Ошибка выполнения поискового запроса: " + e.getMessage(), e);
        }
        return notes;
    }

    public List<Project> getProjectsByUserId(long userId) {
        List<Project> manufacturers = new ArrayList<>();
        String sqlRequest = String.format(
                "SELECT p.* FROM Projects p " +
                        "INNER JOIN %s mu ON p.id = mu.project_id " +
                        "WHERE mu.user_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Используем ваш существующий метод маппинга
                    Project project = new Project(
                            //rs.getLong("project_id"),
                            rs.getString("name"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getString("status"));

                    project.setId(rs.getLong("id"));
                    manufacturers.add(project);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении проекта для пользователей: " + e.getMessage(), e);
        }

        return manufacturers;
    }


    public List<User> getUsersByProjectId(long projectId) {
        List<User> users = new ArrayList<>();
        String sqlRequest = String.format(
                "SELECT u.* FROM Users u " +
                        "INNER JOIN %s mu ON u.id = mu.user_id " +
                        "WHERE mu.project_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, projectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Используем ваш существующий метод маппинга
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

    private User_usage mapResultSetToUser(ResultSet rs) throws SQLException {
        User_usage componentUsage = new User_usage(
                rs.getLong("project_id"),
                rs.getLong("user_id"));

        componentUsage.setId(rs.getLong("id"));

        return componentUsage;
    }
}
