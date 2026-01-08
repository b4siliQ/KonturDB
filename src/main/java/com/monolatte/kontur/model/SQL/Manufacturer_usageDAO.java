package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Manufacturer_usageDAO implements ISQLDAOSearchable<Manufacturer_Usage> {
    final private String _tableName;
    final private Connection _connect;

    public Manufacturer_usageDAO(String tableName, Connection connection) {
        this._tableName = tableName;
        this._connect = connection;
    }


    public void removeComponentByManufacturerId(long componentId, long manufacturerId) {
        String sqlRequest = String.format("DELETE FROM %s WHERE component_id = ? AND manufacturer_id = ?",
                this._tableName
        );
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, componentId);
            pstmt.setLong(2, manufacturerId);
            pstmt.executeUpdate();
            System.out.println("Связь компонента " + componentId + " и производителя " + manufacturerId + " удалена.");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении связи: " + e.getMessage());
        }
    }

    // До кучи добавим обратный метод, если вдруг понадобится в других контроллерах
    public void removeManufacturerByComponentId(long manufacturerId, long componentId) {
        String sqlRequest = String.format("DELETE FROM %s WHERE manufacturer_id = ? AND component_id = ?",
                this._tableName
        );
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, manufacturerId);
            pstmt.setLong(2, componentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createTable() {
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "component_id INTEGER NOT NULL, "
                + "manufacturer_id INTEGER NOT NULL, "
                + "FOREIGN KEY(component_id) REFERENCES Components(id) ON DELETE CASCADE, "
                + "FOREIGN KEY(manufacturer_id) REFERENCES Manufacturers(id) ON DELETE CASCADE)", this._tableName);
        try (Statement stmt = _connect.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void addNote(Manufacturer_Usage mu) {
        String sql = String.format("INSERT INTO %s (component_id, manufacturer_id) VALUES (?,?)", this._tableName);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, mu.getComponent_id());
            pstmt.setLong(2, mu.getManufacturer_id());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) { if (keys.next()) mu.setId(keys.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateNote(Manufacturer_Usage mu) {
        String sql = String.format("UPDATE %s SET component_id=?, manufacturer_id=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setLong(1, mu.getComponent_id());
            pstmt.setLong(2, mu.getManufacturer_id());
            pstmt.setLong(3, mu.getId());
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
    public List<Manufacturer_Usage> getAllNotes() {
        List<Manufacturer_Usage> notes = new ArrayList<>();
        try (Statement st = _connect.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + _tableName)) {
            while (rs.next()) {
                Manufacturer_Usage mu = new Manufacturer_Usage(rs.getLong("component_id"), rs.getLong("manufacturer_id"));
                mu.setId(rs.getLong("id"));
                notes.add(mu);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return notes;
    }

    public List<Manufacturer> getManufacturersByComponentId(long componentId) {
        List<Manufacturer> manufacturers = new ArrayList<>();
        String sql = "SELECT m.* FROM Manufacturers m INNER JOIN " + this._tableName + " mu ON m.id = mu.manufacturer_id WHERE mu.component_id = ?";
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setLong(1, componentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Manufacturer m = new Manufacturer(rs.getString("name"), rs.getString("description"));
                    m.setId(rs.getLong("id"));
                    manufacturers.add(m);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return manufacturers;
    }

    public List<Component> getComponentsByManufacturerId(long manufacturerId) {
        List<Component> components = new ArrayList<>();
        String sql = "SELECT c.* FROM Components c INNER JOIN " + this._tableName + " mu ON c.id = mu.component_id WHERE mu.manufacturer_id = ?";
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setLong(1, manufacturerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Component c = new Component(rs.getString("name"), rs.getString("type"), rs.getString("specification"), rs.getString("datasheet_link"), rs.getFloat("price"), rs.getInt("quantity"));
                    c.setId(rs.getLong("id"));
                    components.add(c);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return components;
    }

    @Override
    public List<Manufacturer_Usage> search(String col, String term) {
        List<Manufacturer_Usage> notes = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE %s LIKE ?", _tableName, col);
        try (PreparedStatement pstmt = _connect.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Manufacturer_Usage mu = new Manufacturer_Usage(rs.getLong("component_id"), rs.getLong("manufacturer_id"));
                    mu.setId(rs.getLong("id"));
                    notes.add(mu);
                }
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
}