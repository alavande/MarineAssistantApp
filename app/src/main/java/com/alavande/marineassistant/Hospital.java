package com.alavande.marineassistant;

/**
 * Created by hasee on 2017/03/29.
 */

public class Hospital {

    private int postcode;
    private String name, phone, street, suburb;
    private double longitude, latitude;

    public Hospital(int postcode, String name, double longitude, double latitude, String phone, String street, String suburb) {
        this.postcode = postcode;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.phone = phone;
        this.street = street;
        this.suburb = suburb;
    }

    public int getPostcode() {
        return postcode;
    }

    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPhone() {
        return phone;
    }

    public String getStreet() {
        return street;
    }

    public String getSuburb() {
        return suburb;
    }
}
