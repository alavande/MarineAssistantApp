package com.alavande.marineassistant;

/**
 * Created by hasee on 2017/03/29.
 */

// entity class
public class BoatAccess {

    String name, location, type;
    double latitude, longitude;

    public BoatAccess(String name, String location, String type, double latitude, double longitude) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
