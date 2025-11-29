package com.monolatte.kontur.model.Notes;

public class User_usage extends BaseNote {
    long project_id;
    long user_id;

    public User_usage(long project_id, long user_id) {
        this.project_id = project_id;
        this.user_id = user_id;
    }

    public long getProject_id() {
        return project_id;
    }

    public void setProject_id(long project_id) {
        this.project_id = project_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }
}
