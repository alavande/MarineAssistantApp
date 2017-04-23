package com.alavande.marineassistant;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hasee on 2017/04/23.
 */

public class Marina {

    private LatLng latLng;
    private String name, type, phone, fuel, water, power, website;

    public Marina(LatLng latLng, String name, String type, String phone, String fuel, String water, String power, String website) {
        this.latLng = latLng;
        this.name = name;
        this.type = type;
        this.phone = phone;
        this.fuel = fuel;
        this.water = water;
        this.power = power;
        this.website = website;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPhone() {
        return phone;
    }

    public String getFuel() {
        return fuel;
    }

    public String getWater() {
        return water;
    }

    public String getPower() {
        return power;
    }

    public String getWebsite() {
        return website;
    }
}
