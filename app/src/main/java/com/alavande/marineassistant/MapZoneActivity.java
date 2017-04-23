package com.alavande.marineassistant;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.SnapshotApi;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

public class MapZoneActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener, View.OnClickListener, GoogleMap.OnMapClickListener {

    private MapFragment mapFragment;
    private View mapView;
    private GoogleMap map;
    private GoogleApiClient client;
    private Polyline polyline;
    private int[] zoneId = {1, 3, 4, 5, 7, 10, 11, 13, 14, 15, 20, 21,
            22, 23, 24, 26, 31, 35, 36, 37, 38, 40, 41, 43, 44, 49, 52,
            53, 54, 62, 63, 64, 65, 66, 68, 69, 70, 73, 74, 85, 88, 90,
            91, 95, 96, 97, 100, 102, 103, 104, 105, 108, 109, 110, 111,
            114, 116, 118, 126, 129, 130, 131, 132, 134, 136, 139, 140,
            143, 144, 145, 146, 148, 149, 150, 154};
    private List<LatLng> findPoints;
    private List<Marina> findMarina;
    private int i;
    private LatLng currentLatLng;
    private TextView zoneTitle;

    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout llBottomSheet, urlLayout;

    private TextView title, content, phone, url, water, fuel, power;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_zone);

        findPoints = new ArrayList<LatLng>();
        findMarina = new ArrayList<Marina>();

        zoneTitle = (TextView) findViewById(R.id.zone_title);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.zone_map_fragment);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        llBottomSheet = (LinearLayout) findViewById(R.id.zone_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        title = (TextView) findViewById(R.id.zone_map_info_title_text);
        content = (TextView) findViewById(R.id.zone_map_info_content_text);
        url = (TextView) findViewById(R.id.zone_map_info_url_text);
        phone = (TextView) findViewById(R.id.zone_map_info_phone_text);
        power = (TextView) findViewById(R.id.zone_map_info_power_text);
        fuel = (TextView) findViewById(R.id.zone_map_info_fuel_text);
        water = (TextView) findViewById(R.id.zone_map_info_water_text);

        urlLayout = (LinearLayout) findViewById(R.id.zone_marina_url);
        urlLayout.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        i = bundle.getInt("item");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        buildGoogleApiClient();
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
//        map.setMyLocationEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Awareness.SnapshotApi.getLocation(client)
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        if (!locationResult.getStatus().isSuccess()) {
//                            Log.e(TAG, "Could not get location.");
                            return;
                        }
                        Location location = locationResult.getLocation();
                        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 6));
                        Log.i("location", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                    }
                });
        switch (i) {
            case 1:
                searchZoneInDB();
                zoneTitle.setText("  Fishing Area  ");
                break;
            case 2:
                searchMarinaInDB();
                putMarinaToMap();
                zoneTitle.setText("  Marina  ");
                break;
            default:
                break;
        }
    }

    public void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    private void drawZoneOnMap(){
        PolylineOptions options = new PolylineOptions();
        if (findPoints.size() > 0) {
            for (int i = 0; i < findPoints.size(); i++) {
                options.add(findPoints.get(i));
            }
            options.add(findPoints.get(0));
//            polyline = map.addPolyline(options);
            map.addPolyline(options);
        }

    }

    private void searchMarinaInDB(){
        MyDatabaseHelper helper = new MyDatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from marina;", null);
        cursor.moveToFirst();
        while (cursor.moveToNext()){
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

            LatLng latLng = new LatLng(latitude, longitude);
            Log.i("latlng", latLng.toString());
            String name = cursor.getString(cursor.getColumnIndex("name"));

            String water = cursor.getString(cursor.getColumnIndex("water"));
            if (water.length() == 0) {
                water = "No Water Information";
            }
            String power = cursor.getString(cursor.getColumnIndex("power"));
            if (power.length() == 0) {
                power = "No Power Information";
            }
            String website = cursor.getString(cursor.getColumnIndex("website"));
            if (website.length() == 0) {
                website = "No Website Information";
            }
            String phone = cursor.getString(cursor.getColumnIndex("phone"));
            if (phone.length() == 0) {
                phone = "No Phone Information";
            }
            String fuel = cursor.getString(cursor.getColumnIndex("fuel"));
            if (fuel.length() == 0) {
                fuel = "No Fuel Information";
            }
            if (name.length() == 0) {
                name = "No Name Information";
            }
            String type = "Marina";
            Marina marina = new Marina(latLng, name, type, phone, fuel, water, power, website);

            findMarina.add(marina);
        }
        db.close();
        helper.close();
    }

    private void putMarinaToMap(){
        for (Marina marina : findMarina) {
            MarkerOptions options = new MarkerOptions();
            options.title(marina.getType());
            options.snippet(marina.getName()+","+marina.getFuel()+","+marina.getPhone()+","+marina.getPower()+","+marina.getWater()+","+marina.getWebsite());
            Log.i("create", marina.getLatLng().toString());
            options.position(marina.getLatLng());
            map.addMarker(options);
        }

    }

    private void searchZoneInDB(){
        MyDatabaseHelper helper = new MyDatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor;
        for (int i : zoneId) {
            if (findPoints.size() > 0) {
                findPoints.clear();
            }
            cursor = db.rawQuery("select * from zone where schedulere = "
                    + i + " and position = 'In Water';", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    Log.i("info", cursor.getString(cursor.getColumnIndex("uniqueid")));
                    LatLng latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude")));
                    findPoints.add(latLng);
                } while (cursor.moveToNext());

                drawZoneOnMap();
            }
        }
        db.close();
        helper.close();
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

    @Override
    public boolean onMarkerClick(Marker marker) {

        this.marker = marker;

        title.setText(this.marker.getTitle());
        String[] details = this.marker.getSnippet().split(",");
//        options.snippet(marina.getName()+","+marina.getFuel()+","+marina.getPhone()+","+marina.getPower()+","+marina.getWater()+","+marina.getWebsite());
        String name = details[0];
        String fuel = details[1];
        String phone = details[2];
        String power = details[3];
        String water = details[4];
        String website = details[5];
        Log.i("snip", this.marker.getSnippet());
        content.setText(name);
        this.phone.setText(phone);
        this.fuel.setText(fuel);
        this.url.setText(website);
        this.water.setText(water);
        this.power.setText(power);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        return false;
    }

    @Override
    public void onClick(View view) {
        Log.i("url", "url layout clicked");
        String website = url.getText().toString();
        Intent intent = new Intent();
        if (website.equals("No Website Information")) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("url", website);
        intent.putExtras(bundle);
        intent.setClass(this, WindForecastActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
