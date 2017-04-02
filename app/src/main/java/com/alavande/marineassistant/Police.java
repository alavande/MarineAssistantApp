package com.alavande.marineassistant;

/**
 * Created by hasee on 2017/03/29.
 */

public class Police {

    private String station, psa, area;
    private int postcode, phoneNum;
    private double latitude, longitude;

    public Police(String psa, String station, int postcode, String area, int phoneNum, double latitude, double longitude) {
        this.psa = psa;
        this.station = station;
        this.postcode = postcode;
        this.area = area;
        this.phoneNum = phoneNum;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStation() {
        return station;
    }

    public String getPsa() {
        return psa;
    }

    public int getPostcode() {
        return postcode;
    }

    public String getArea() {
        return area;
    }

    public int getPhoneNum() {
        return phoneNum;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
