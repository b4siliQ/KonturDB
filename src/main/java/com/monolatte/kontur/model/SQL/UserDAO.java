package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements ISQLDAO<User> {
    final private String _tableName;
    final private Connection _connect;

    public UserDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " project_id INTEGER NOT NULL REFERENCES projects(id),"
                + "name TEXT NOT NULL, description TEXT NOT NULL)", this._tableName);
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
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.executeUpdate();
            System.out.println("Table " + this._tableName + " dropped");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void addNote(User user) {
        String sqlRequest = String.format("INSERT INTO %s (project_id, name, description) VALUES (?,?,?)", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, user.getProject_id());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getDescription());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try(ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        user.setId(id);
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
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteNote(long id) {
        String sqlRequest = String.format("DELETE FROM %s WHERE id = ?", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void updateNote(User user) {
        String sqlRequest = String.format("UPDATE %s SET project_id = ?, name = ?, description = ? WHERE id = ?", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, user.getProject_id());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getDescription());
            pstmt.setLong(4, user.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<User> getAllNotes() {
        List<User> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User note = mapResultSetToUser(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    public List<User> getUsageByProjectId(long project_id) {
        List<User> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s WHERE project_id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, project_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User note = mapResultSetToUser(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("name"),
                rs.getString("description"));

        user.setId(rs.getLong("id"));
        return user;
    }
}