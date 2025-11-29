package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.Component;
import com.monolatte.kontur.model.Notes.Manufacturer;
import com.monolatte.kontur.model.Notes.Manufacturer_Usage;
import com.monolatte.kontur.model.Notes.Project;

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

    @Override
    public void createTable() {
        String sqlRequest = String.format("CREATE TABLE IF NOT EXISTS %s ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "component_id INTEGER NOT NULL, "
                        + "manufacturer_id INTEGER NOT NULL, "
                        + "FOREIGN KEY(component_id) REFERENCES Components(id) ON DELETE CASCADE, "
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
    public void addNote(Manufacturer_Usage manufacturer_usage) {
        String sqlRequest = String.format("INSERT INTO %s (component_id, manufacturer_id) VALUES (?,?)", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, manufacturer_usage.getComponent_id());
            pstmt.setLong(2, manufacturer_usage.getManufacturer_id());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet genKeys = pstmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        long id = genKeys.getLong(1);
                        manufacturer_usage.setId(id);
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
    public void updateNote(Manufacturer_Usage manufacturer_usage) {
        String sqlRequest = String.format("UPDATE %s SET component_id=?, manufacturer_id=? WHERE id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, manufacturer_usage.getComponent_id());
            pstmt.setLong(2, manufacturer_usage.getManufacturer_id());
            pstmt.setLong(3, manufacturer_usage.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Manufacturer_Usage> getAllNotes() {
        List<Manufacturer_Usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Manufacturer_Usage note = mapResultSetToManufacturerUsage(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    public List<Manufacturer_Usage> getUsageByComponentId(long component_id) {
        List<Manufacturer_Usage> notes = new ArrayList<>();
        String sqlRequest = String.format("SELECT * FROM %s WHERE component_id=?", this._tableName);
        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, component_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Manufacturer_Usage note = mapResultSetToManufacturerUsage(rs);
                notes.add(note);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return notes;
    }

    @Override
    public List<Manufacturer_Usage> search(String columnDescription, String searchTerm) {
        List<Manufacturer_Usage> notes = new ArrayList<>();

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
                    notes.add(mapResultSetToManufacturerUsage(rs));
                }
            }
        } catch (SQLException e) {
            // Логирование и обработка ошибок базы данных
            throw new RuntimeException("Ошибка выполнения поискового запроса: " + e.getMessage(), e);
        }
        return notes;
    }

    /**
     * Получает список всех производителей, связанных с указанным компонентом.
     * Использует INNER JOIN для объединения таблицы производителей и таблицы связей.
     *
     * @param componentId ID компонента, для которого ищем производителей
     * @return Список объектов Manufacturer
     */
    public List<Manufacturer> getManufacturersByComponentId(long componentId) {
        List<Manufacturer> manufacturers = new ArrayList<>();

        // ВАЖНО:
        // 1. this._tableName — это таблица производителей (например, 'manufacturers')
        // 2. 'manufacturer_usage' — это имя вашей связующей таблицы.
        // Если вы назвали её иначе в Manufacturer_usageDAO, поменяйте имя здесь!
        String sqlRequest = String.format(
                "SELECT m.* FROM Manufacturers m " +
                        "INNER JOIN %s mu ON m.id = mu.manufacturer_id " +
                        "WHERE mu.component_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, componentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Используем ваш существующий метод маппинга
                    Manufacturer manufacturer = new Manufacturer(
                            //rs.getLong("project_id"),
                            rs.getString("name"),
                            rs.getString("description"));

                    manufacturer.setId(rs.getLong("id"));
                    manufacturers.add(manufacturer);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении производителей для компонента: " + e.getMessage(), e);
        }

        return manufacturers;
    }

    /**
     * Получает список всех компонентов, связанных с указанным производителем.
     * Использует INNER JOIN для объединения таблицы компонентов и таблицы связей.
     *
     * @param manufacturerId ID производителя, для которого ищем компоненты
     * @return Список объектов Component
     */
    public List<Component> getComponentsByManufacturerId(long manufacturerId) {
        List<Component> components = new ArrayList<>();

        // ВАЖНО:
        // 1. this._tableName — это таблица компонентов (например, 'components')
        // 2. 'manufacturer_usage' — это имя вашей связующей таблицы.
        String sqlRequest = String.format(
                "SELECT c.* FROM Components c " +
                        "INNER JOIN %s mu ON c.id = mu.component_id " +
                        "WHERE mu.manufacturer_id = ?",
                this._tableName
        );

        try (PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1, manufacturerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Используем ваш существующий метод маппинга
                    Component component = new Component(
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("specification"),
                            rs.getString("datasheet_link"),
                            rs.getFloat("price"));

                    component.setId(rs.getLong("id"));
                    components.add(component);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении компонентов для производителя: " + e.getMessage(), e);
        }

        return components;
    }

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

    public void removeComponentByManufacturerId(long componentId, long manufacturerId) {
        String sqlRequest = String.format("DELETE FROM %s WHERE component_id = ? AND manufacturer_id = ?",
                this._tableName
        );
        try(PreparedStatement pstmt = this._connect.prepareStatement(sqlRequest)) {
            pstmt.setLong(1,componentId);
            pstmt.setLong(2, manufacturerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Manufacturer_Usage mapResultSetToManufacturerUsage(ResultSet rs) throws SQLException {
        Manufacturer_Usage componentUsage = new Manufacturer_Usage(
                rs.getLong("component_id"),
                rs.getLong("manufacturer_id"));

        componentUsage.setId(rs.getLong("id"));

        return componentUsage;
    }
}
