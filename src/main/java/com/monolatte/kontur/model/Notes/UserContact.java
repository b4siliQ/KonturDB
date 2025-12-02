package com.monolatte.kontur.model.Notes;

public class UserContact extends BaseNote {
    private long user_id;
    private String contact_type;
    private String contact_value;

    public UserContact(long user_id, String contact_type, String contact_value) {
        this.user_id = user_id;
        this.contact_type = contact_type;
        this.contact_value = contact_value;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getContact_type() {
        return contact_type;
    }

    public void setContact_type(String contact_type) {
        this.contact_type = contact_type;
    }

    public String getContact_value() {
        return contact_value;
    }

    public void setContact_value(String contact_value) {
        this.contact_value = contact_value;
    }
}
