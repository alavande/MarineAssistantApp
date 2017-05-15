package com.alavande.marineassistant;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapZoneActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener, View.OnClickListener, GoogleMap.OnMapClickListener {

    private MapFragment mapFragment;
    private View mapView;
    private GoogleMap map;
    private GoogleApiClient client;
    private PolylineOptions polylineOptions;
    private int[] zoneId = {1, 3, 4, 5, 7, 10, 11, 13, 14, 15, 20, 21,
            22, 23, 24, 26, 31, 35, 36, 37, 38, 40, 41, 43, 44, 49, 52,
            53, 54, 62, 63, 64, 65, 66, 68, 69, 70, 73, 74, 85, 88, 90,
            91, 95, 96, 97, 100, 102, 103, 104, 105, 108, 109, 110, 111,
            114, 116, 118, 126, 129, 130, 131, 132, 134, 136, 139, 140,
            143, 144, 145, 146, 148, 149, 150, 154};
    private List<LatLng> findPoints;
    private List<Marina> findMarina;
    private int i, building;
    private double range;
    private Bundle bundle;
    private LatLng currentLatLng;
    private Location currentLocation;
    private TextView zoneTitle;

    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout llBottomSheet, urlLayout;
    private FrameLayout shareLayout;

    private ImageView testImage;
    private TextView title, content, phone, url, water, fuel, power;
    private Marker marker;

    private List<LocationActivity> list;
    private LatLngBounds.Builder builder;

    private Context context;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_zone);

        dismissStatusBar();

        bundle = getIntent().getExtras();
        i = bundle.getInt("item");

        activity = this;

        findPoints = new ArrayList<LatLng>();
        findMarina = new ArrayList<Marina>();

        zoneTitle = (TextView) findViewById(R.id.zone_title);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.zone_map_fragment);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        if (i == 2) {
//            llBottomSheet.setVisibility(View.VISIBLE);
            llBottomSheet = (LinearLayout) findViewById(R.id.zone_bottom_sheet);
            llBottomSheet.setVisibility(View.VISIBLE);
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
        }

        if (i == 3) {
//            testImage = (ImageView) findViewById(R.id.test_img);

            shareLayout = (FrameLayout) findViewById(R.id.share_btn);
            shareLayout.setOnClickListener(this);
            shareLayout.setVisibility(View.VISIBLE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        if (i == 4){
            range = bundle.getDouble("range");
            building = bundle.getInt("building");
            context = this;
            llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
            bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            title = (TextView) findViewById(R.id.map_info_title_text);
            content = (TextView) findViewById(R.id.map_info_content_text);
            phone = (TextView) findViewById(R.id.map_info_phone_text);
            phone.setOnClickListener(this);
            url = (TextView) findViewById(R.id.map_info_location_text);

            Button start = (Button) findViewById(R.id.start_navigation_btn);
            Button navigation = (Button) findViewById(R.id.navigation_btn);
            start.setVisibility(View.GONE);
            navigation.setVisibility(View.GONE);
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        buildGoogleApiClient();
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
//        map.setMyLocationEnabled(true);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.877203, 145.043997), 10));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (i == 1) {
            map.setMyLocationEnabled(true);
        }
//        if (i == 4) {
//            map.setOnCameraIdleListener(clusterManager);
//            map.setOnMarkerClickListener(clusterManager);
//        }

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

//                        MarkerOptions options = new MarkerOptions();
//                        options.title("Current Location");
//                        options.position(currentLatLng);
//                        map.addMarker(options);


                        if (i == 1) {
                            searchZoneInDB();
                            zoneTitle.setText("  Fishing Area  ");
                        } else {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 6));
                        }

                        if (i == 3) {
                            list = (List<LocationActivity>) getIntent().getSerializableExtra("list");
                            Log.i("list size", list.size() + "");
                            addLocationEventToMap(list);
                            zoneTitle.setText("  History Events  ");
                        }

                        if (i == 4) {
//
                            searchAllInDB();
                            switch (building) {
                                case 1:
                                    zoneTitle.setText("  Near Hospitals  ");
                                    break;
                                case 2:
                                    zoneTitle.setText("  Near Police Stations  ");
                                    break;
                                case 3:
                                    zoneTitle.setText("  Near Boat Ramps  ");
                                    break;
                                case 4:
                                    zoneTitle.setText("  Near Boat Moorings  ");
                                    break;
                                default:
                                    break;
                            }
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12));
                        }
                        Log.i("location", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                    }
                });
        switch (i) {
            case 1:
//                searchZoneInDB();
//                zoneTitle.setText("  Fishing Area  ");
                break;
            case 2:
                searchMarinaInDB();
                putMarinaToMap();
                zoneTitle.setText("  Marina  ");
                break;
//            case 3:
//                list = (List<LocationActivity>) getIntent().getSerializableExtra("list");
//                Log.i("list size", list.size() + "");
//                addLocationEventToMap(list);
//                zoneTitle.setText("  History Events  ");
//                break;
            default:
                break;
        }
    }

    private void searchAllInDB(){

        MarkerOptions current = new MarkerOptions();
        current.title("current location");
        current.position(currentLatLng);
        map.addMarker(current);

//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
                SearchNearestPlace search = new SearchNearestPlace();

                    switch (building){
                        case 1:
                            List<Hospital> hospitals = search.searchNearestHospital(context);
                            for (Hospital h : hospitals) {
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, new LatLng(h.getLatitude(), h.getLongitude())) / 1000;
                                if (distance < range) {
                                    MarkerOptions options = new MarkerOptions();
                                    options.position(new LatLng(h.getLatitude(), h.getLongitude()));
                                    options.title(h.getName()+ ", Hospital");
                                    options.snippet(h.getStreet() + ", " + h.getPostcode() );
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_icon));
                                    final Marker m = map.addMarker(options);
                                    m.setTag(h.getPhone());
                                }
                            }
                            break;
                        case 2:
                            List<Police> polices = search.searchNearestPolice(context);
                            for (Police p : polices) {
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, new LatLng(p.getLatitude(), p.getLongitude())) / 1000;
                                Log.i("distance", distance+"");
                                Log.i("range", range+"");
                                if (distance < range) {
                                    MarkerOptions options = new MarkerOptions();
                                    options.position(new LatLng(p.getLatitude(), p.getLongitude()));
                                    options.title(p.getStation() + ", Police Station");
                                    options.snippet(p.getPsa() + ", " + p.getPostcode());
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.police_icon));

                                    Marker m = map.addMarker(options);
                                    m.setTag(p.getPhoneNum());
                                }
                            }
                            break;
                        case 3:
                            List<BoatAccess> accesses = search.searchNearestBoatAccess(context);
                            for (BoatAccess h : accesses) {
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, new LatLng(h.getLatitude(), h.getLongitude())) / 1000;
                                if (distance < range) {
                                    MarkerOptions options = new MarkerOptions();
                                    options.position(new LatLng(h.getLatitude(), h.getLongitude()));
                                    options.title(h.getName() + ", " + h.getType() + ", Boat Ramp");
                                    options.snippet(h.getLocation());
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.boat_point_icon));

                                    map.addMarker(options);
                                }
                            }
                            break;
                        case 4:
                            List<BoatMooring> moorings = search.searchNearestBoatMooring(context);
                            for (BoatMooring h : moorings) {
                                double distance = SphericalUtil.computeDistanceBetween(currentLatLng, new LatLng(h.getLatitude(), h.getLongitude())) / 1000;
                                if (distance < range) {
                                    MarkerOptions options = new MarkerOptions();
                                    options.position(new LatLng(h.getLatitude(), h.getLongitude()));
                                    options.title(h.getName() + ", " + h.getType() + ", Boat Mooring");
                                    options.snippet(h.getLocation());
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.mooring_icon1));

                                    map.addMarker(options);
                                }
                            }
                            break;
                        default:
                            break;
                    }
//                return null;
//            }
//        }.execute();

    }

    public void addLocationEventToMap(List<LocationActivity> list){

        polylineOptions = new PolylineOptions();
        builder = new LatLngBounds.Builder();
        for (LocationActivity la : list) {

//            View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
//                    .inflate(R.layout.custom_marker, null);
//            TextView markerTitle = (TextView) customMarkerView.findViewById(R.id.marker_title);
//            TextView markerTime = (TextView) customMarkerView.findViewById(R.id.marker_time);
//            markerTitle.setText(la.getEventName());
//            markerTime.setText(la.getTime());

            MarkerOptions options = new MarkerOptions();
            options.title(la.getEventName());
            options.snippet(la.getTime());
            options.position(new LatLng(la.getLat(), la.getLon()));
//            options.icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(la, customMarkerView)));
//            Log.i("position", new LatLng(la.getLat(), la.getLon()).toString());
            builder.include(new LatLng(la.getLat(), la.getLon()));
            Marker m = map.addMarker(options);
//            m.hideInfoWindow();
            polylineOptions.add(new LatLng(la.getLat(), la.getLon()));
        }
        Log.i("points", polylineOptions.getPoints().size()+"");
//        map.addPolyline(polylineOptions);
        if (list.size() > 1) {
            map.addPolyline(polylineOptions);
            LatLngBounds bounds = builder.build();
            int padding = 200;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cu);
        }
    }

//    private Bitmap createCustomMarker(LocationActivity la, View view){

//        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
//        customMarkerView.buildDrawingCache();
//        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(returnedBitmap);
//        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
//        Drawable drawable = customMarkerView.getBackground();
//        if (drawable != null)
//            drawable.draw(canvas);
//        customMarkerView.draw(canvas);
//
//        return returnedBitmap;
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
//        view.buildDrawingCache();
//        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//
//        return bitmap;
//    }

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
        cursor = db.rawQuery("select * from zone where position = 'In Water';", null);
        cursor.moveToFirst();
        LatLngBounds.Builder builder1 = new LatLngBounds.Builder();
        builder1.include(currentLatLng);
        do {
            MarkerOptions options = new MarkerOptions();
            LatLng latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude")));
            options.title("Fish Point");
            options.snippet(latLng.latitude + ", " + latLng.longitude);
            options.position(latLng);
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.fish_marker1));
            double distance = SphericalUtil.computeDistanceBetween(currentLatLng, new LatLng(latLng.latitude, latLng.longitude)) / 1000;
//            if (distance < 50) {
                Marker m = map.addMarker(options);
                builder1.include(m.getPosition());
//            }

//            map.addMarker(options);
        } while (cursor.moveToNext());
        LatLngBounds bounds = builder1.build();
        int padding = 200;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//        map.animateCamera(cu);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 11));
//        for (int i : zoneId) {
//            if (findPoints.size() > 0) {
//                findPoints.clear();
//            }
//            cursor = db.rawQuery("select * from zone where schedulere = "
//                    + i + " and position = 'In Water';", null);
//            cursor.moveToFirst();
//            if (cursor.getCount() > 0) {
//                do {
//                    Log.i("info", cursor.getString(cursor.getColumnIndex("uniqueid")));
//                    LatLng latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex("latitude")),
//                            cursor.getDouble(cursor.getColumnIndex("longitude")));
//                    findPoints.add(latLng);
//                } while (cursor.moveToNext());
//
//                drawZoneOnMap();
//            }
//        }
        cursor.close();
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
        switch (i) {
            case 4:
                llBottomSheet.setVisibility(View.VISIBLE);
                title.setText(marker.getTitle());
                content.setText(marker.getSnippet());
                if (marker.getTag() != null) {
                    phone.setText(marker.getTag().toString());
                } else {
                    phone.setText("No Phone Number Available");
                }
                url.setText(marker.getPosition().latitude + ", " + marker.getPosition().longitude);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case 3:
                break;
            case 1:
//                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.fish_marker1));
                break;
            case 2:
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
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.zone_marina_url:
                Log.i("url", "url layout clicked");
                String website = url.getText().toString();
                if (website.equals("No Website Information")) {
                    return;
                }
                bundle.putString("url", website);
                intent.putExtras(bundle);
                intent.setClass(this, WindForecastActivity.class);
                startActivity(intent);
                break;
            case R.id.share_btn:
                getScreenShot();
                break;
            case R.id.map_info_phone_text:
                if (!phone.getText().toString().equals("No Phone Number Available")) {
                    Uri dialUri = Uri.parse("tel:" + phone.getText());
                    intent = new Intent(Intent.ACTION_DIAL, dialUri);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No Phone Number Available.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    private void getScreenShot(){

//        bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
//        mapView.setDrawingCacheEnabled(true);
//        bitmap = mapView.getDrawingCache();

        final Toast toast = Toast.makeText(this, "invoking share...", Toast.LENGTH_SHORT);
        toast.show();
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                try {
                    FileOutputStream out = new FileOutputStream("/mnt/sdcard/Download/TeleSensors.png");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

                    Uri imgUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                    Log.i("uri", imgUri+"");

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, imgUri);
                    intent.setType("image/*");
                    startActivity(intent);
                    toast.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        map.snapshot(callback);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (bottomSheetBehavior == null) {
            return;
        }
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if (i == 4) {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                intent.setClass(this, MapActivity.class);
                startActivity(intent);
                finish();
            }
        } else if (i == 1){
            intent.setClass(this, MapActivity.class);
            startActivity(intent);
            finish();
        }else {
            super.onBackPressed();
        }

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
