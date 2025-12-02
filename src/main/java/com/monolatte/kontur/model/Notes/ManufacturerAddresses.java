package com.monolatte.kontur.model.Notes;

public class ManufacturerAddresses extends BaseNote {
    private long manufacturer_id;
    private String addresses_type;
    private String city;
    private String full_address;

    public ManufacturerAddresses(long manufacturer_id, String addresses_type, String city, String full_address) {
        this.manufacturer_id = manufacturer_id;
        this.addresses_type = addresses_type;
        this.city = city;
        this.full_address = full_address;
    }

    public long getManufacturer_id() {
        return manufacturer_id;
    }

    public void setManufacturer_id(long manufacturer_id) {
        this.manufacturer_id = manufacturer_id;
    }

    public String getAddresses_type() {
        return addresses_type;
    }

    public void setAddresses_type(String addresses_type) {
        this.addresses_type = addresses_type;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFull_address() {
        return full_address;
    }

    public void setFull_address(String full_address) {
        this.full_address = full_address;
    }
}
