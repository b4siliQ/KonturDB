package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.ManufacturerAddresses;
import com.monolatte.kontur.model.Notes.Manufacturer;

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

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "addresses_type TEXT NOT NULL, "
                        + "city TEXT NOT NULL, "
                        + "manufacturer_id INTEGER NOT NULL, "
                        + "FOREIGN KEY(manufacturer_id) REFERENCES Manufacturers(id) ON DELETE CASCADE)",
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
    public void addNote(ManufacturerAddresses manufacturerAddresses) {
        String sqlRequest = String.format("INSERT INTO %s (user_id, contact_type, contact_value) VALUES (?,?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, manufacturerAddresses.getManufacturer_id());
            pstmt.setString(2, manufacturerAddresses.getAddresses_type());
            pstmt.setString(3, manufacturerAddresses.getCity());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        manufacturerAddresses.setId(id);
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
    public void updateNote(ManufacturerAddresses manufacturerAddresses) {
        String sqlRequest = String.format("UPDATE %s SET user_id=?, contact_type=?, contact_value=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, manufacturerAddresses.getManufacturer_id());
            pstmt.setString(2, manufacturerAddresses.getAddresses_type());
            pstmt.setString(3, manufacturerAddresses.getCity());
            pstmt.setLong(4, manufacturerAddresses.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ManufacturerAddresses> getAllNotes() {
        List<ManufacturerAddresses> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ManufacturerAddresses note = mapResultSetToUserContact(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    @Override
    public List<ManufacturerAddresses> search(String columnDescription, String searchTerm) {
        List<ManufacturerAddresses> notes = new ArrayList<>();

        long idToSearch;
        try {
            idToSearch = Long.parseLong(searchTerm);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Для поиска по ID введите число. Получено: " + searchTerm);
            return notes;
        }

        String sqlRequest = String.format("SELECT * FROM %s WHERE %s LIKE ?", this._tableName, columnDescription);
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, idToSearch);
            try(ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToUserContact(rs));
                }
            }
        } catch (SQLException e) {
            // Логирование и обработка ошибок базы данных
            throw new RuntimeException("Ошибка выполнения поискового запроса: " + e.getMessage(), e);
        }
        return notes;
    }

    public ManufacturerAddresses getManufacturerAddressesByManufacturerId(long manufacturerId) {
        ManufacturerAddresses manufacturerAddress = null;

        // SQL запрос для связи 1:1
        String sqlRequest = String.format(
                "SELECT m.* FROM Manufacturer_Addresses m " +
                        "INNER JOIN %s mu ON m.id = mu.id " +
                        "WHERE mu.manufacturer_id = ? LIMIT 1",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, manufacturerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Используем if, так как ожидаем только одну запись
                if (rs.next()) {
                    manufacturerAddress = new ManufacturerAddresses(
                            rs.getLong("manufacturer_id"),
                            rs.getString("addresses_type"),
                            rs.getString("city"),
                            rs.getString("full_address")
                    );
                    // Устанавливаем ID из базы данных
                    manufacturerAddress.setId(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении адреса производителя ID " + manufacturerId + ": " + e.getMessage(), e);
        }

        return manufacturerAddress;
    }


    public List<Manufacturer> getManufacturerByManufacturerAddressesId(long addressesId) {
        List<Manufacturer> manufacturers = new ArrayList<>();
        String sqlRequest = String.format(
                "SELECT m.* FROM Manufacturers m " +
                        "INNER JOIN %s mu ON m.id = mu.manufacturer_id " +
                        "WHERE mu.id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, addressesId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Manufacturer manufacturer = new Manufacturer(
                            rs.getString("name"),
                            rs.getString("description"));

                    manufacturer.setId(rs.getLong("id"));
                    manufacturers.add(manufacturer);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении пользователей для проекта: " + e.getMessage(), e);
        }

        return manufacturers;
    }

    public void removeAddressesByManufacturerId(long userId) {
        String sqlRequest = String.format("DELETE FROM %s WHERE user_id = ?",
                this._tableName
        );
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ManufacturerAddresses mapResultSetToUserContact(ResultSet rs) throws SQLException {
        ManufacturerAddresses manufacturerAddresses = new ManufacturerAddresses(
                rs.getLong("manufacturer_id"),
                rs.getString("addresses_type"),
                rs.getString("city"),
                rs.getString("full_address")
        );

        manufacturerAddresses.setId(rs.getLong("id"));

        return manufacturerAddresses;
    }
}
