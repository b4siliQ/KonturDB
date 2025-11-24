package com.monolatte.kontur.model.SQLManager.Notes;

public class Component {
    int id;
    String name;
    String type;
    String specification;
    String datasheet_link;
    int price;

    public Component(int id, String name, String type, String specification, String datasheet_link, int price) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.specification = specification;
        this.datasheet_link = datasheet_link;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Component [id=" + id + ", name=" + name + ", type=" + type + ", specification="
                + specification + ", datasheet_link=" + datasheet_link + ", price=" + price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

