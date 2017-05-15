package com.alavande.marineassistant;

import android.*;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;

public class LocationEventsActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager manager;
    private FloatingActionButton addLocationActivity;
    private com.getbase.floatingactionbutton.FloatingActionButton showOnMap;
    private List<LocationActivity> activities;
    private LocationEventsAdapter adapter;
    private Context context;
    private String recorder;
    private GoogleApiClient client;
    private MyDatabaseHelper helper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_events);

        dismissStatusBar();

        // initial fields
        context = this;
        activities = new ArrayList<LocationActivity>();

        addLocationActivity = (FloatingActionButton) findViewById(R.id.add_location_activity);
        addLocationActivity.setOnClickListener(this);
        showOnMap = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.show_on_map);
        showOnMap.setOnClickListener(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        recorder = bundle.getString("recorder");
        Log.i("recorder1", recorder);
        buildGoogleApiClient();

        // check if this is first time enter in this activity
        MyDatabaseHelper helper = new MyDatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from first_load;", null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String view = cursor.getString(cursor.getColumnIndex("view"));
            if (view.equals("event")) {
                int num = cursor.getInt(cursor.getColumnIndex("num"));
                if (num == 0) {
                    // show instruction if this is first time
                    runOverlay();
                    // update database
                    ContentValues cv = new ContentValues();
                    cv.put("num", 1);
                    db.update("first_load", cv, "view = ?", new String[]{"event"});
                }
                break;
            }
        }

        getLocationEvents();

        TextView parentTrip = (TextView) findViewById(R.id.trip_name_top);
        parentTrip.setText("From " + recorder);

        adapter = new LocationEventsAdapter(activities, recorder);
        recyclerView = (RecyclerView) findViewById(R.id.location_activities_recycler_view);
        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }


    // create google api client for handling all api used
    public void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 1, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .build();
        client.connect();
    }

    // retrieve all location events data in database
    private void getLocationEvents() {
        helper = new MyDatabaseHelper(this);
        db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from location_event where recorder = '" + recorder + "';", null);
        Log.i("count", cursor.getCount() + "");
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                String recorder = cursor.getString(cursor.getColumnIndex("recorder"));
                String name = cursor.getString(cursor.getColumnIndex("event_name"));
                String time = cursor.getString(cursor.getColumnIndex("event_time"));
                lat = cursor.getDouble(cursor.getColumnIndex("lat"));
                lon = cursor.getDouble(cursor.getColumnIndex("lon"));
                LocationActivity locationActivity = new LocationActivity(recorder, name, time, lat, lon);
                activities.add(locationActivity);
            } while (cursor.moveToNext());
        }
        if (activities.size() == 0) {
            showOnMap.setVisibility(View.GONE);
        }
        cursor.close();
        db.close();
        helper.close();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_location_activity:
                getLocation();
                final EditText inputEvent = new EditText(this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog dialog = builder
                        .setTitle("Add New Activity")
                        .setView(inputEvent)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                helper = new MyDatabaseHelper(context);
                                db = helper.getWritableDatabase();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                Date date = new Date(System.currentTimeMillis());
                                String time = sdf.format(date);
                                String name = inputEvent.getText().toString();
                                if (name.length() == 0) {
                                    Toast.makeText(context, "Please input an event.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                db.beginTransaction();
                                cursor = db.rawQuery("select * from location_event where lat = '" + lat + "' and lon = '" + lon +
                                        "' and recorder = '" + recorder + "';", null);
                                ContentValues cv = new ContentValues();
                                if (cursor.getCount() > 0) {
                                    cursor.moveToFirst();
                                    String newName = cursor.getString(cursor.getColumnIndex("event_name")) + ", " + name;
                                    cv.put("event_name", newName);
                                    cv.put("event_time", time);
                                    db.update("location_event", cv, "lat = ? and lon = ? and recorder = ?", new String[] {lat + "", lon + "", recorder});
                                    cursor.close();
                                } else {
                                    cv.put("recorder", recorder);
                                    cv.put("event_name", name);
                                    cv.put("event_time", time);
                                    cv.put("lat", lat);
                                    cv.put("lon", lon);
                                    db.insert("location_event", null, cv);
                                }
                                db.setTransactionSuccessful();
                                db.endTransaction();
                                db.close();
                                helper.close();
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString("recorder", recorder);
                                intent.putExtras(bundle);
                                intent.setClass(context, LocationEventsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                break;
            case R.id.show_on_map:
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("item", 3);
                    intent.putExtras(bundle);
                    intent.putExtra("list", (Serializable) activities);
                    intent.setClass(this, MapZoneActivity.class);
                    startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void getLocation() {

        // check location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission if not have
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        // get current latitude and longitude data of current location
        Awareness.SnapshotApi.getLocation(client)
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        if (!locationResult.getStatus().isSuccess()) {
//                            Log.e(TAG, "Could not get location.");
                            return;
                        }
                        Location location = locationResult.getLocation();
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void runOverlay(){
        Animation mEnterAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimation.setDuration(600);
        mEnterAnimation.setFillAfter(true);

        Animation mExitAnimation = new AlphaAnimation(1f, 0f);
        mExitAnimation.setDuration(600);
        mExitAnimation.setFillAfter(true);

        ChainTourGuide tourGuide1 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Show on Map")
                        .setDescription("Show all your recorded events on map...")
                        .setGravity(Gravity.BOTTOM)
                )
                // note that there is no Overlay here, so the default one will be used
                .playLater(showOnMap);

        ChainTourGuide tourGuide2 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Add New Event Button")
                        .setDescription("Click add button to record more events in different location...")
                        .setGravity(Gravity.TOP)
                        .setBackgroundColor(Color.parseColor("#c0392b"))
                )
                .setOverlay(new Overlay()
                        .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation)
                )
                .playLater(addLocationActivity);

        Sequence sequence = new Sequence.SequenceBuilder()
                .add(tourGuide1, tourGuide2)
                .setDefaultOverlay(new Overlay()
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation)
                )
                .setDefaultPointer(null)
                .setContinueMethod(Sequence.ContinueMethod.Overlay)
                .build();

        ChainTourGuide.init(this).playInSequence(sequence);
    }

    private void dismissStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
    }
}
