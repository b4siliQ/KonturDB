package com.monolatte.kontur.model.Notes;

public class ManufacturerAddresses extends BaseNote {
    private long manufacturer_id;
    private String addresses_type;
    private String city;

    public ManufacturerAddresses(long manufacturer_id, String addresses_type, String city) {
        this.manufacturer_id = manufacturer_id;
        this.addresses_type = addresses_type;
        this.city = city;
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
}
