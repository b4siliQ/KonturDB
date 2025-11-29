package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.Manufacturer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManufacturerDAO implements ISQLDAO<Manufacturer> {
    final private String _tableName;
    final private Connection _connect;

    public ManufacturerDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT NOT NULL)", this._tableName);
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
    public void addNote(Manufacturer manufacturer) {
        String sqlRequest = String.format("INSERT INTO %s (name, description) VALUES (?,?)", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, manufacturer.getName());
            pstmt.setString(2, manufacturer.getDescription());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try(ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        manufacturer.setId(id);
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
    public void updateNote(Manufacturer manufacturer) {
        String sqlRequest = String.format("UPDATE %s SET name = ?, description = ? WHERE id = ?", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setString(1, manufacturer.getName());
            pstmt.setString(2, manufacturer.getDescription());
            pstmt.setLong(3, manufacturer.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Manufacturer> getAllNotes() {
        List<Manufacturer> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Manufacturer note = mapResultSetToManufacturer(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    public List<Manufacturer> getUsageByComponentId(long component_id) {
        List<Manufacturer> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s WHERE component_id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, component_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Manufacturer note = mapResultSetToManufacturer(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    private Manufacturer mapResultSetToManufacturer(ResultSet rs) throws SQLException {
        Manufacturer manufacturer = new Manufacturer(
                //rs.getLong("project_id"),
                rs.getString("name"),
                rs.getString("description"));

        manufacturer.setId(rs.getLong("id"));
        return manufacturer;
    }
}
