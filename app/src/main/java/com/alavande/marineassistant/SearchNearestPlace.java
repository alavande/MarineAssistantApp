package com.alavande.marineassistant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/03/29.
 */

public class SearchNearestPlace {

    private SQLiteAssetHelper helper;
    private SQLiteDatabase db;

    public List<Hospital> searchNearestHospital(Context context){

        helper = new MyDatabaseHelper(context);
        db = helper.getReadableDatabase();

        List<Hospital> hospitals = new ArrayList<Hospital>();

        Cursor cursor = db.rawQuery("select * from hospital", null);

            cursor.moveToFirst();

            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndex("name"));
                int postcode = cursor.getInt(cursor.getColumnIndex("postcode"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("logitude"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                String phone = cursor.getString(cursor.getColumnIndex("phone"));
                String street = cursor.getString(cursor.getColumnIndex("street"));
                String suburb = cursor.getString(cursor.getColumnIndex("suburb"));

                Hospital hospital = new Hospital(postcode, name, longitude, latitude, phone, street, suburb);
                hospitals.add(hospital);

            }
            db.close();
            helper.close();

        return hospitals;
    }

    public List<Police> searchNearestPolice(Context context){

        helper = new MyDatabaseHelper(context);
        db = helper.getReadableDatabase();

        List<Police> polices = new ArrayList<Police>();

        Cursor cursor = db.rawQuery("select * from police", null);
        cursor.moveToFirst();

        while(cursor.moveToNext()){

            String station = cursor.getString(cursor.getColumnIndex("station"));
            String psa = cursor.getString(cursor.getColumnIndex("PSA"));
            String area = cursor.getString(cursor.getColumnIndex("area"));
            int phoneNum = 0;
            try {
                phoneNum = cursor.getInt(cursor.getColumnIndex("phone"));
            } catch (Exception e) {
            }
            int postcode = cursor.getInt(cursor.getColumnIndex("postcode"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));


            Police police = new Police(psa, station, postcode,area, phoneNum, latitude, longitude);
            polices.add(police);

        }
        db.close();
        helper.close();
        return polices;
    }

    public List<BoatAccess> searchNearestBoatAccess(Context context){

        helper = new MyDatabaseHelper(context);
        db = helper.getReadableDatabase();

        List<BoatAccess> boatAccesses = new ArrayList<BoatAccess>();

        Cursor cursor = db.rawQuery("select * from boat", null);
        cursor.moveToFirst();

        while(cursor.moveToNext()){

            String name = cursor.getString(cursor.getColumnIndex("name"));
            String location = cursor.getString(cursor.getColumnIndex("location"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));


            BoatAccess boatAccess = new BoatAccess(name, location, type, latitude, longitude);
            boatAccesses.add(boatAccess);

        }
        db.close();
        helper.close();
        return boatAccesses;
    }

    public List<BoatMooring> searchNearestBoatMooring(Context context){

        helper = new MyDatabaseHelper(context);
        db = helper.getReadableDatabase();

        List<BoatMooring> boatMoorings = new ArrayList<BoatMooring>();

        Cursor cursor = db.rawQuery("select * from mooring", null);
        cursor.moveToFirst();

        while(cursor.moveToNext()){

            String name = cursor.getString(cursor.getColumnIndex("name"));
            String location = cursor.getString(cursor.getColumnIndex("location"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));


            BoatMooring boatMooring = new BoatMooring(longitude, latitude, name, location, type);
            boatMoorings.add(boatMooring);

        }
        db.close();
        helper.close();
        return boatMoorings;
    }
}
