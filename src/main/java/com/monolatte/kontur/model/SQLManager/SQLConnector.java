package com.monolatte.kontur.model.SQLManager;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLConnector {
    private static final String _FOLDERNAME = "KonturSolutions";
    private static final String _FILENAME = "KonturDB.db";
    private static final String _databasePath;

    static {
        try {
            _databasePath = getDatabasePath();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getDatabasePath() {
        String appDataPath = System.getenv("APPDATA");
        if (appDataPath == null || appDataPath.isEmpty()) {
            appDataPath = System.getProperty("user.home");
        }

        File appDir = Paths.get(appDataPath, _FOLDERNAME).toFile();

        if (!appDir.exists()) {
            if (appDir.mkdirs()) {
                System.out.printf(
                        "Folder %s in appdata is online!%n\n",
                        _FOLDERNAME
                );
            } else {
                throw new RuntimeException(String.format(
                        "\"Warning! Cannot init %s in appdata folder at path: %s. Check permissions\"",
                        _FOLDERNAME
                ));
            }
        }

        String fullPath = Paths.get(appDir.getAbsolutePath(), _FILENAME).toString();
        return "jdbc:sqlite:" + fullPath;
    }

    public static Connection getConnection() {
        Connection connect;
        try {
            connect = DriverManager.getConnection(_databasePath);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return connect;
    }
}
