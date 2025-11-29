package com.monolatte.kontur.model.Notes;

public abstract class BaseNote {
    protected long id;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
