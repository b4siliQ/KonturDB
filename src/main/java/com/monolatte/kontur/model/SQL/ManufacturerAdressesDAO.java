package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.ManufacturerAddresses;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManufacturerAdressesDAO implements ISQLDAOSearchable<ManufacturerAddresses> {
    final private String _tableName;
    final private Connection _connect;

    public ManufacturerAdressesDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }

    // Тот самый метод, который искал компилятор!
    public ManufacturerAddresses getManufacturerAddressesByManufacturerId(long manufacturerId) {
        String sql = String.format("SELECT * FROM %s WHERE manufacturer_id = ? LIMIT 1", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sql)) {
            pstmt.setLong(1, manufacturerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAddress(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске адреса: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createTable() {
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "addresses_type TEXT NOT NULL, "
                + "city TEXT NOT NULL, "
                + "manufacturer_id INTEGER NOT NULL, "
                + "FOREIGN KEY(manufacturer_id) REFERENCES Manufacturers(id) ON DELETE CASCADE)", this._tableName);
        try (Statement stmt = _connect.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void addNote(ManufacturerAddresses addr) {
        String sql = String.format("INSERT INTO %s (manufacturer_id, addresses_type, city) VALUES (?,?,?)", this._tableName);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, addr.getManufacturer_id());
            pstmt.setString(2, addr.getAddresses_type());
            pstmt.setString(3, addr.getCity());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) { if (keys.next()) addr.setId(keys.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateNote(ManufacturerAddresses addr) {
        String sql = String.format("UPDATE %s SET manufacturer_id=?, addresses_type=?, city=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setLong(1, addr.getManufacturer_id());
            pstmt.setString(2, addr.getAddresses_type());
            pstmt.setString(3, addr.getCity());
            pstmt.setLong(4, addr.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void deleteNote(long id) {
        try (PreparedStatement pstmt = _connect.prepareStatement("DELETE FROM " + _tableName + " WHERE id=?")) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<ManufacturerAddresses> getAllNotes() {
        List<ManufacturerAddresses> notes = new ArrayList<>();
        try (Statement stmt = _connect.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM " + _tableName)) {
            while (rs.next()) notes.add(mapResultSetToAddress(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    @Override
    public List<ManufacturerAddresses> search(String col, String term) {
        List<ManufacturerAddresses> notes = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE %s LIKE ?", _tableName, col);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) notes.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    @Override
    public void dropTable() {
        try (Statement stmt = _connect.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + _tableName);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<String[]> getAddressesWithJoinSelection() {
        List<String[]> data = new ArrayList<>();
        String sql = "SELECT a.city, a.addresses_type, m.name as manufacturer_name " +
                "FROM " + this._tableName + " a " +
                "INNER JOIN Manufacturers m ON a.manufacturer_id = m.id";
        try (Statement stmt = this._connect.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString("city"), rs.getString("addresses_type"), rs.getString("manufacturer_name")});
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return data;
    }

    private ManufacturerAddresses mapResultSetToAddress(ResultSet rs) throws SQLException {
        ManufacturerAddresses addr = new ManufacturerAddresses(
                rs.getLong("manufacturer_id"),
                rs.getString("addresses_type"),
                rs.getString("city")
        );
        addr.setId(rs.getLong("id"));
        return addr;
    }
}