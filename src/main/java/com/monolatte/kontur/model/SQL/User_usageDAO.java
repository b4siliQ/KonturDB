package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.User_usage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User_usageDAO implements ISQLDAO<User_usage> {
    final private String _tableName;
    final private Connection _connect;

    public User_usageDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " project_id INTEGER NOT NULL REFERENCES projects(id), user_id INTEGER NOT NULL REFERENCES user(id))", this._tableName);
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

    public List<User_usage> getUsageByProjectId(long project_id) {
        List<User_usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s WHERE project_id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, project_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User_usage note = mapResultSetToUser(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    private User_usage mapResultSetToUser(ResultSet rs) throws SQLException {
        User_usage componentUsage = new User_usage(
                rs.getLong("project_id"),
                rs.getLong("user_id"));

        componentUsage.setId(rs.getLong("id"));

        return componentUsage;
    }
}
