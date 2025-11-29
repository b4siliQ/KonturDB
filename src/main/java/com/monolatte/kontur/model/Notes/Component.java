package com.monolatte.kontur.model.Notes;

public class Component extends BaseNote {
    String name;
    String type;
    String specification;
    String datasheet_link;
    float price;

    public Component(String name, String type, String specification, String datasheet_link, float price) {
        this.name = name;
        this.type = type;
        this.specification = specification;
        this.datasheet_link = datasheet_link;
        this.price = price;
    }

    @Override
    public String toString() { return this.name; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getDatasheet_link() {
        return datasheet_link;
    }

    public void setDatasheet_link(String datasheet_link) {
        this.datasheet_link = datasheet_link;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}

