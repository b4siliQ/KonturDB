package com.monolatte.kontur.model.SQLManager;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLConnector {
    public String database = "jdbc:sqlite:components.db";
    public Connection getConnection() {
        Connection connect = null;
        try {
            connect = DriverManager.getConnection(database);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return connect;
    }
}
