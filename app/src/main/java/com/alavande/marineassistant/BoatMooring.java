package com.alavande.marineassistant;

/**
 * Created by hasee on 2017/04/02.
 */

// entity class
public class BoatMooring {

    private double longitude, latitude;
    private String name, location, type;

    public BoatMooring(double longitude, double latitude, String name, String location, String type) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
        this.location = location;
        this.type = type;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }
}
