package com.alavande.marineassistant;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by hasee on 2017/04/28.
 */

// entity class
public class LocationActivity implements Serializable {

    private String recorder, eventName, time;
    private double lat, lon;

    public LocationActivity(String recorder, String eventName, String time, double lat, double lon) {
        this.recorder = recorder;
        this.eventName = eventName;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    public String getRecorder() {
        return recorder;
    }

    public void setRecorder(String recorder) {
        this.recorder = recorder;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
