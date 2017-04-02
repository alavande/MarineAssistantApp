package com.alavande.marineassistant;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by hasee on 2017/03/29.
 */

public class MyDatabaseHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "emergencyDB.db";
    private static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


}
