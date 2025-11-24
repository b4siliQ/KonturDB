package com.monolatte.kontur.model.SQLManager;

import java.sql.Connection;

public class SQLSuperManager {
    private static class Singleton {
        private static final SQLSuperManager INSTANCE = new SQLSuperManager();
    }

    private final ComponentsManager _componentsManager;
    private final Components_usageManager _componentsUsageManager;
    private final ProjectManager _projectManager;

    private final String _tableName = "KonturDB";

    private SQLSuperManager() {
        Connection sharedConnection = SQLConnector.getConnection();

        this._componentsManager = new ComponentsManager(this._tableName, sharedConnection);
        this._componentsUsageManager = new Components_usageManager(this._tableName, sharedConnection);
        this._projectManager = new ProjectManager(this._tableName, sharedConnection);
    }

    public ComponentsManager get_componentsManager() {
        return _componentsManager;
    }

    public Components_usageManager getComponentsUsageManager() {
        return _componentsUsageManager;
    }

    public ProjectManager getProjectManager() {
        return _projectManager;
    }

    public static SQLSuperManager getInstance() {
        return Singleton.INSTANCE;
    }
}
