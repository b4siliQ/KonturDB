package com.monolatte.kontur.model.SQLManager;

public class SQLSuperManager {
    private final ComponentsManager componentsManager;
    private final Components_usageManager componentsUsageManager;
    private final ProjectManager projectManager;

    public SQLSuperManager(ComponentsManager componentsManager, Components_usageManager components_usage_manager, ProjectManager project_manager) {
        this.componentsManager = componentsManager;
        this.componentsUsageManager = components_usage_manager;
        this.projectManager = project_manager;
    }

    public ComponentsManager getComponentsManager() {
        return componentsManager;
    }

    public Components_usageManager getComponentsUsageManager() {
        return componentsUsageManager;
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }
}
