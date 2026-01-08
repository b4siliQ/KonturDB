package com.monolatte.kontur.model.Notifyers;

public abstract class Notifyer {
    private String _title;
    private String _header;
    private String _message;

    public Notifyer(String title, String header, String message) {
        this._title = title;
        this._header = header;
        this._message = message;
    }

    protected String getTitle() {
        return this._title;
    }

    protected void setTitle(String title) {
        this._title = title;
    }

    protected String getHeader() {
        return this._header;
    }

    protected void setHeader(String header) {
        this._header = header;
    }

    protected String getMessage() {
        return this._message;
    }
    protected void setMessage(String message) {
        this._message = message;
    }
}
