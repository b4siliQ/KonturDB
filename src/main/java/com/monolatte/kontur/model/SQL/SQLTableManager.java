package com.monolatte.kontur.model.SQL;

import java.sql.Connection;
import java.util.List;

public class SQLTableManager {
    private static class Singleton {
        private static final SQLTableManager INSTANCE = new SQLTableManager();
    }

    private final ComponentsDAO _componentsDAO;
    private final Components_usageDAO _componentsUsageManager;
    private final ProjectDAO _projectDAO;
    private final List<ISQLDAOBase> _allManagers;

    private SQLTableManager() {
        Connection sharedConnection = SQLConnector.getConnection();

        this._componentsDAO = new ComponentsDAO("Components", sharedConnection);
        this._componentsUsageManager = new Components_usageDAO("Component_Usage", sharedConnection);
        this._projectDAO = new ProjectDAO("Projects", sharedConnection);

        _allManagers = List.of(
                this._componentsDAO,
                this._projectDAO,
                this._componentsUsageManager
        );
    }

    public ComponentsDAO getComponentsManager() {
        return _componentsDAO;
    }

    public Components_usageDAO getComponentsUsageManager() {
        return _componentsUsageManager;
    }

    public ProjectDAO getProjectManager() {
        return _projectDAO;
    }

    public List<ISQLDAOBase> getAllManagers() { return this._allManagers; }

    public static SQLTableManager getInstance() {
        return Singleton.INSTANCE;
    }
}
