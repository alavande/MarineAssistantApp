package com.alavande.marineassistant;

import android.*;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener, FloatingSearchView.OnSearchListener,
        GoogleMap.OnMyLocationButtonClickListener, NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener{

    private ChainTourGuide mTourGuideHandler;

    private DrawerLayout drawerLayout;
    private RelativeLayout popupWindow;
    private NavigationView navigationView;
    private LinearLayout llBottomSheet;

    private BottomSheetBehavior bottomSheetBehavior;
    private AutoCompleteTextView startPoint, endPoint;

    private ImageView carMode, shipMode;

    private Polyline polyline;
    private PolylineOptions polylineOptions;

    private MapFragment mapFragment;
    private GoogleMap map;
    private Marker marker, hospitalMarker, policeMarker, searchMarker,
            boatAccessMarker, boatMooringMarker, secondBoatAccessMarker, secondBoatMooringMarker;
    private Marker searchHospitalMarker, searchPoliceMarker, searchBoatAccessMarker,
            searchBoatMooringMarker, searchSecondBoatAccessMarker, searchSecondBoatMooringMarker;
    private Marker startMarker, endMarker, currentMarker;

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private View mapView;
    private LatLng currentLatLng;
    private PlaceAutocompleteAdapter autocompleteAdapter;

    private FloatingSearchView searchView;

    private Context context;
    private Handler handler;

    private Hospital nearestHostpital;
    private Police nearestPolice;
    private BoatAccess nearestBoatAccess, secondBoatAccess;
    private BoatMooring nearestBoatMooring, secondBoatMooring;

    private TextView bottomTitle, bottomContent, phoneText, loccationText;
    private AutoCompleteTextView autoCompleteTextView;
    private Button navigationBtn, startNavigationBtn;

    private List<Marker> markerList;

    private static final LatLngBounds BOUNDS_GREATER_VICTORIA = new LatLngBounds(
            new LatLng(-38.055358, 140.966645),
            new LatLng(-37.502666, 149.878801));

    private double returnRange = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        dismissStatusBar();

        context = this;
        handler = new Handler();

        // components in popup window
        carMode = (ImageView) findViewById(R.id.car_mode);
        carMode.setOnClickListener(this);
        shipMode = (ImageView) findViewById(R.id.ship_mode);
        shipMode.setOnClickListener(this);
        shipMode.setVisibility(View.GONE);

        popupWindow = (RelativeLayout) findViewById(R.id.popup_window);
        popupWindow.setVisibility(View.GONE);

        startPoint = (AutoCompleteTextView) findViewById(R.id.start_text);
        startPoint.setOnItemClickListener(startPointClickListener);
        endPoint = (AutoCompleteTextView) findViewById(R.id.end_text);
        endPoint.setOnItemClickListener(endPointClickListener);

        // components in bottom sheet
        llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        navigationBtn = (Button) findViewById(R.id.navigation_btn);
        navigationBtn.setOnClickListener(this);
        startNavigationBtn = (Button) findViewById(R.id.start_navigation_btn);
        startNavigationBtn.setOnClickListener(this);

        bottomTitle = (TextView) findViewById(R.id.map_info_title_text);
        bottomContent = (TextView) findViewById(R.id.map_info_content_text);
        phoneText = (TextView) findViewById(R.id.map_info_phone_text);
        loccationText = (TextView) findViewById(R.id.map_info_location_text);

        // components in main layout
        drawerLayout = (DrawerLayout) findViewById(R.id.left_drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        searchView = (FloatingSearchView) findViewById(R.id.search_view);
        searchView.attachNavigationDrawerToMenuButton(drawerLayout);
        searchView.setOnSearchListener(this);

        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_search_text);
        autoCompleteTextView.setCursorVisible(false);
        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCompleteTextView.setCursorVisible(true);
            }
        });
        autoCompleteTextView.setOnItemClickListener(mAutocompleteClickListener);

        // map show in main layout
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        markerList = new ArrayList<Marker>();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.877203, 145.043997), 10));
        // check permission and build google client api
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                map.setMyLocationEnabled(true);
                map.setBuildingsEnabled(true);
                resetMyLocationButton();
            } else {
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            map.setMyLocationEnabled(true);
            map.setBuildingsEnabled(true);
            resetMyLocationButton();
        }

        // set on each onclick listener for map
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerClickListener(this);

        // check if this is first time enter in this activity
        MyDatabaseHelper helper = new MyDatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from first_load;", null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String view = cursor.getString(cursor.getColumnIndex("view"));
            if (view.equals("map")) {
                int num = cursor.getInt(cursor.getColumnIndex("num"));
                Log.i("num", num+"");
                if (num == 0) {
                    // show instruction if this is first time
                    runOverlay();
                    // update database
                    ContentValues cv = new ContentValues();
                    cv.put("num", 1);
                    db.update("first_load", cv, "view = ?", new String[]{"map"});
                }
                break;
            }
        }
    }

    public void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        // set adapters
        autocompleteAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,
                client, BOUNDS_GREATER_VICTORIA, null);
        autoCompleteTextView.setAdapter(autocompleteAdapter);
        startPoint.setAdapter(autocompleteAdapter);
        endPoint.setAdapter(autocompleteAdapter);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (marker != null) {
            marker.remove();
        }

        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        addMarkerToMap(currentLatLng);
        addSearchMarkerToMap(currentLatLng);

//        MarkerOptions options = new MarkerOptions();
//        options.title(marker.getTitle());
//        options.snippet(marker.getSnippet());
//        options.position(marker.getPosition());
//        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_marker));
//        currentMarker = map.addMarker(options);
//        marker.setVisible(false);
        currentMarker = marker;
//        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.current_marker));

        // get nearest data for current location
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchHospital();
                searchPolice();
                searchBoatAccess();
                searchBoatMooring();
            }
        }).start();

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    // check and request permission
    private void checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the location permission, please accept")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (client == null) {
                            buildGoogleApiClient();
                        }
                        map.setMyLocationEnabled(true);
//                        map.setTrafficEnabled(true);
                        map.setBuildingsEnabled(true);
                        resetMyLocationButton();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public View resetMyLocationButton(){

        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        // and next place it, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                locationButton.getLayoutParams();
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 300);

        return locationButton;
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

    }

    @Override
    public void onSearchAction(String currentQuery) {

//        boolean result = searchOnMap(currentQuery);
//        if (!result) {
//            return;
//        }
//        LatLng latLng = marker.getPosition();
//        searchHospitalAtOtherPlace(latLng);
//        searchPoliceAtOtherPlace(latLng);
//        searchBoatAccessAtOtherPlace(latLng);
//        searchBoatMooringAtOtherPlace(latLng);
//        removeOtherMarkers(null, null);
//        zoomMapToFitMarkers(latLng, searchHospitalMarker, searchPoliceMarker,
//                searchBoatAccessMarker, searchSecondBoatAccessMarker,
//                searchBoatMooringMarker, searchSecondBoatMooringMarker);
    }

//    public boolean searchOnMap(String keyWord){
//
//        if (polyline != null) {
//            polyline.remove();
//        }
//
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//
//        List<Address> addresses = null;
//
//        try {
//            addresses = geocoder.getFromLocationName(keyWord, 1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (addresses.size() !=0) {
//
//            if (addresses.get(0).getCountryCode() == null) {
//                Toast.makeText(this, "No result found or not in australia, please check your key words.", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            if (!addresses.get(0).getCountryCode().equals("AU")) {
//                Toast.makeText(this, "No result found or not in australia, please check your key words.", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            try{
//                if (Integer.parseInt(addresses.get(0).getPostalCode()) < 3000 || Integer.parseInt(addresses.get(0).getPostalCode()) >= 4000){
//                    Toast.makeText(this, "No result found, please check your key words.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Address address = addresses.get(0);
//                    LatLng searchLatLng = new LatLng(address.getLatitude(), address.getLongitude());
//
//                    MarkerOptions options = new MarkerOptions();
//                    options.position(searchLatLng);
//
//                    options.title(address.getAddressLine(0) + ", " + address.getLocality() + ", " + address.getPostalCode());
//                    marker = map.addMarker(options);
//
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 17));
////                    Toast.makeText(this, address.getCountryCode(), Toast.LENGTH_SHORT).show();
//                }
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "Number Format Exception", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        }  else {
//            Toast.makeText(this, "No result found, please check your key words.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        return true;
//    }

    @Override
    public boolean onMyLocationButtonClick() {

        if (marker != null) {
            marker.remove();
        }

        if (polyline != null) {
            polyline.remove();
        }

//        if (searchMarker != null) {
//            searchMarker.remove();
//        }

        if (startMarker != null) {
            startMarker.remove();
        }

        if (endMarker != null) {
            endMarker.remove();
        }

        removeOtherMarkers(null, null);
        removeSearchMarkers();
        new AsyncTask<LatLng, Void, MarkerOptions>(){
            @Override
            protected MarkerOptions doInBackground(LatLng... latLngs) {

                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

                List<Address> addresses = null;
                String address, city, state, country, postalCode;

                try {
                    addresses = geocoder.getFromLocation(latLngs[0].latitude, latLngs[0].longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(currentLatLng);

                if (addresses != null) {

                    Address site = addresses.get(0);

                    address = site.getAddressLine(0);
                    city = site.getLocality();
                    state = site.getAdminArea();
                    country = site.getCountryName();
                    postalCode = site.getPostalCode();

                    markerOptions.title(address + ", " + city + ", " + postalCode);
                    markerOptions.snippet( state + ", " + country);
                } else {
                    markerOptions.title("Current Position");
                    markerOptions.snippet("No detail provided");
                }

                return markerOptions;
            }

            @Override
            protected void onPostExecute(MarkerOptions options) {
                marker = map.addMarker(options);
                for (Marker m : markerList) {
                    if (!m.isVisible()){
                        m.setVisible(true);
                    }
                }

            }
        }.execute(currentLatLng);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
        startMarker = marker;
        return true;
    }

    public void addMarkerToMap(final LatLng addLatLng){

//        if (marker != null) {
//            marker.remove();
//        }

//        if (searchMarker != null) {
//            searchMarker.remove();
//        }

        new AsyncTask<LatLng, Void, MarkerOptions>() {
            @Override
            protected MarkerOptions doInBackground(LatLng... latLngs) {

                LatLng latLng = latLngs[0];
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

                List<Address> addresses = null;
                String address, city, state, country, postalCode;

                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (addresses != null) {

                    Address site = addresses.get(0);

                    address = site.getAddressLine(0);
                    city = site.getLocality();
                    state = site.getAdminArea();
                    country = site.getCountryName();
                    postalCode = site.getPostalCode();

                    markerOptions.title(address + ", " + city + ", " + postalCode);
                    markerOptions.snippet( state + ", " + country);
                } else {
                    markerOptions.title("Current Position");
                    markerOptions.snippet("No detail provided");
                }
                return markerOptions;
            }

            @Override
            protected void onPostExecute(MarkerOptions markerOptions) {
                super.onPostExecute(markerOptions);
                marker = map.addMarker(markerOptions);
//                currentMarker = marker;
                if (searchHospitalMarker != null && searchPoliceMarker != null) {
                    zoomMapToFitMarkers(markerOptions.getPosition(), searchHospitalMarker, searchPoliceMarker,
                            searchBoatAccessMarker, searchSecondBoatAccessMarker, searchBoatMooringMarker, searchSecondBoatMooringMarker);
                } else {

                }
            }
        }.execute(addLatLng);
    }

    public void addSearchMarkerToMap(final LatLng addLatLng){

//        if (marker != null) {
//            marker.remove();
//        }

//        if (searchMarker != null) {
//            searchMarker.remove();
//        }

        new AsyncTask<LatLng, Void, MarkerOptions>() {
            @Override
            protected MarkerOptions doInBackground(LatLng... latLngs) {

                LatLng latLng = latLngs[0];
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

                List<Address> addresses = null;
                String address, city, state, country, postalCode;

                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (addresses != null) {

                    Address site = addresses.get(0);

                    address = site.getAddressLine(0);
                    city = site.getLocality();
                    state = site.getAdminArea();
                    country = site.getCountryName();
                    postalCode = site.getPostalCode();

                    markerOptions.title(address + ", " + city + ", " + postalCode);
                    markerOptions.snippet( state + ", " + country);
                } else {
                    markerOptions.title("Current Position");
                    markerOptions.snippet("No detail provided");
                }
                return markerOptions;
            }

            @Override
            protected void onPostExecute(MarkerOptions markerOptions) {
                super.onPostExecute(markerOptions);
                searchMarker = map.addMarker(markerOptions);
                endMarker = searchMarker;
//                currentMarker = marker;
                if (searchHospitalMarker != null && searchPoliceMarker != null) {
                    zoomMapToFitMarkers(markerOptions.getPosition(), searchHospitalMarker, searchPoliceMarker,
                            searchBoatAccessMarker, searchSecondBoatAccessMarker, searchBoatMooringMarker, searchSecondBoatMooringMarker);
                } else {

                }
            }
        }.execute(addLatLng);
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        removeSearchMarkers();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (polyline != null) {
            polyline.remove();
        }

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        String url = null;

        Intent intent1 = new Intent();
        Bundle bundle1 = new Bundle();

        switch (item.getItemId()) {
            case R.id.nav_police:

//                searchPolice();
//                removeOtherMarkers(policeMarker, null);
//                zoomMapToFitMarker(policeMarker);
                getRange(2);
                break;
            case R.id.nav_hospital:

//                searchHospital();
//                removeOtherMarkers(hospitalMarker, null);
//                zoomMapToFitMarker(hospitalMarker);
                getRange(1);
                break;
            case R.id.nav_boat_access:

//                searchBoatAccess();
//                removeOtherMarkers(boatAccessMarker, secondBoatAccessMarker);
//                if (secondBoatAccessMarker != null) {
//                    zoomMapToFitTwoMarkers(boatAccessMarker, secondBoatAccessMarker);
//                } else {
//                    zoomMapToFitMarker(boatAccessMarker);
//                }
                getRange(3);
                break;
            case R.id.nav_boat_mooring:

//                searchBoatMooring();
//                removeOtherMarkers(boatMooringMarker, secondBoatMooringMarker);
//                if (secondBoatMooringMarker != null) {
//                    zoomMapToFitTwoMarkers(boatMooringMarker, secondBoatMooringMarker);
//                } else {
//                    zoomMapToFitMarker(boatMooringMarker);
//                }
                getRange(4);
//                Toast.makeText(this, "In progress...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about_us:
                intent.setClass(this, AboutUsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_zone:
//                intent.setClass(this, WindForecastActivity.class);
//                url = "http://www.exploreaustralia.net.au/Activities/Fishing-spots/Victoria";
//                bundle.putString("url", url);
                intent.setClass(this, MapZoneActivity.class);
                bundle.putInt("item", 1);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_wind:
                intent.setClass(this, WindForecastActivity.class);
                intent.putExtras(new Bundle());
                startActivity(intent);
                break;
            case R.id.nav_play_around:
                intent.setClass(this, MapZoneActivity.class);
                bundle.putInt("item", 2);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.nav_diving:
                intent.setClass(this, WindForecastActivity.class);
                url = "http://www.divevictoria.com.au/boat-diving/dive-site-interactive-map.html";
//                url = "https://www.google.com.au/maps?source=tldsi&hl=zh-CN";
                bundle.putString("url", url);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
        drawerLayout.closeDrawers();
        return true;
    }

    private void getRange(final int building){
        LinearLayout alerLinear = new LinearLayout(this);
        final SeekBar seekBar = new SeekBar(this);
        alerLinear.setOrientation(LinearLayout.VERTICAL);
        final TextView textView = new TextView(this);
        textView.setText("    Range: 0 km.");
        seekBar.setMax(50);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView.setText("    Range: " + i + " km.");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        alerLinear.addView(seekBar);
        alerLinear.addView(textView);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setTitle("Please select a range (km)")
                .setView(alerLinear)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {

                            returnRange = (double) seekBar.getProgress();
                            Log.i("range", returnRange+"");
//
                            if (returnRange == 0) {
                                Toast.makeText(context, "Range can not be 0", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            intent.setClass(context, MapZoneActivity.class);
                            bundle.putInt("item", 4);
                            bundle.putDouble("range", returnRange);
                            bundle.putInt("building", building);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(context, "Please input a range between in (0, 20] km.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void removeOtherMarkers(Marker marker1, Marker marker2){

        for (Marker m : markerList) {
            if (m == marker1) {
                m.setVisible(true);
                continue;
            } else if (m == marker2) {
                m.setVisible(true);
                continue;
            } else if (m != null) {
                m.setVisible(false);
            }
        }
    }

    public void removeSearchMarkers(){
        if (searchHospitalMarker != null) {
            searchHospitalMarker.remove();
        }

        if (searchPoliceMarker != null) {
            searchPoliceMarker.remove();
        }

        if (searchBoatAccessMarker != null) {
            searchBoatAccessMarker.remove();
        }

        if (searchSecondBoatAccessMarker != null) {
            searchSecondBoatAccessMarker.remove();
        }

        if (searchBoatMooringMarker != null) {
            searchBoatMooringMarker.remove();
        }

        if (searchSecondBoatMooringMarker != null) {
            searchSecondBoatMooringMarker.remove();
        }
    }

    public void zoomMapToFitMarker(Marker marker){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentLatLng);
        if (marker != null) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 200;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu);
    }

    public void zoomMapToFitTwoMarkers(Marker marker1, Marker marker2){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentLatLng);
        builder.include(marker1.getPosition());
        builder.include(marker2.getPosition());

        LatLngBounds bounds = builder.build();

        int padding = 200;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu);
    }

    public void zoomMapToFitMarkers(LatLng latLng, Marker marker1, Marker marker2,
                                    Marker marker3, Marker marker4, Marker marker5, Marker marker6){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng);
        builder.include(marker1.getPosition());
        builder.include(marker2.getPosition());
        builder.include(marker3.getPosition());
        builder.include(marker4.getPosition());
        builder.include(marker5.getPosition());
        builder.include(marker6.getPosition());

        LatLngBounds bounds = builder.build();

        int padding = 200;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu);
    }

    @Override
    public void onMapClick(LatLng latLng) {

        startPoint.setText("    " + "Current Location");
        startMarker = generateMarker(marker.getPosition(), marker.getTitle());

        if (startMarker != null) {
            startMarker.setVisible(false);
        }
        if (endMarker != null) {
            endMarker.setVisible(false);
        }

        animClose(popupWindow);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if (searchMarker != null) {
            searchMarker.remove();
        }

        if (polyline != null) {
            polyline.remove();
        }

        if (endMarker != null) {
            endMarker.remove();
        }

        if (startMarker != null) {
            startMarker.remove();
        }

        startMarker = generateMarker(marker.getPosition(), marker.getTitle());

        animOpen(popupWindow);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        navigationBtn.setVisibility(View.GONE);
        startPoint.setText("    current location");

        addSearchMarkerToMap(latLng);
        searchHospitalAtOtherPlace(latLng);
        searchPoliceAtOtherPlace(latLng);
        searchBoatAccessAtOtherPlace(latLng);
        searchBoatMooringAtOtherPlace(latLng);
        removeOtherMarkers(null, null);
        zoomMapToFitMarkers(latLng, searchHospitalMarker, searchPoliceMarker,
                searchBoatAccessMarker, searchSecondBoatAccessMarker,
                searchBoatMooringMarker, searchSecondBoatMooringMarker);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (navigationBtn.getVisibility() == View.GONE) {
            navigationBtn.setVisibility(View.VISIBLE);
        }

        if (popupWindow.getVisibility() == View.VISIBLE) {
            navigationBtn.setVisibility(View.GONE);
        }

        if (polyline != null) {
            polyline.remove();
        }

//        if (endMarker == null) {
//            endMarker = generateMarker(marker.getPosition(), marker.getTitle());
//            endMarker.setVisible(false);
//        }
//
//        if (startMarker != null) {
//            startMarker.setVisible(false);
//        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        bottomTitle.setText(marker.getTitle());
        bottomContent.setText(marker.getSnippet());
        loccationText.setText(marker.getPosition().latitude + ", " + marker.getPosition().longitude);
        if (marker.getTag() != null) {
            phoneText.setText(marker.getTag().toString());
        } else {
            phoneText.setText("No Phone Number Available.");
        }

//        if (startPoint.isFocused()) {
//            if (searchMarker != null) {
//                if (searchMarker.isVisible()) {
//                    searchMarker.setVisible(false);
//                }
//            }
//            startPoint.setText("    " + marker.getTitle());
//            startMarker.setPosition(marker.getPosition());
//            startMarker.setTitle(marker.getTitle());
//        }
//        if (endPoint.isFocused() || (popupWindow.getVisibility() ==  View.GONE)) {
//            if (startMarker != null && searchMarker != null && searchMarker.isVisible()) {
//                searchMarker.setVisible(false);
//            }
//            endPoint.setText("    " + marker.getTitle());
//            endMarker = generateMarker(marker.getPosition(), marker.getTitle());
//        }
//
//        if (!endPoint.isFocused() && !startPoint.isFocused() && popupWindow.getVisibility() == View.VISIBLE){
//            startPoint.setText(marker.getTitle());
//            startMarker = generateMarker(marker.getPosition(), marker.getTitle());
//        }

        if (endMarker != null) {
            endMarker.setVisible(false);
        }
        if (startMarker != null) {
            startMarker.setVisible(false);
        }

        if (popupWindow.getVisibility() == View.VISIBLE && startPoint.isFocused()) {
            startPoint.setText("    " + marker.getTitle());
            startMarker = generateMarker(marker.getPosition(), marker.getTitle());
        } else {
            endPoint.setText("    " + marker.getTitle());
            endMarker = generateMarker(marker.getPosition(), marker.getTitle());
        }

        return true;
    }

    private Marker generateMarker(LatLng latLng, String title) {

        MarkerOptions options = new MarkerOptions();
        options.title(title);
        options.position(latLng);

        return map.addMarker(options);
    }

//    public void findRoute(){
//
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//
//        Double distance = SphericalUtil.computeDistanceBetween(currentLatLng, searchMarker.getPosition());
//
//        if (polyline != null) {
//            polyline.remove();
//        }
//
//        if (distance > 0) {
//            PolylineOptions rectOptions = new PolylineOptions()
//                    .add(currentLatLng)
//                    .add(searchMarker.getPosition());
//
//            // Get back the mutable Polyline
//            polyline = map.addPolyline(rectOptions);
//
//            zoomMapToFitMarker(searchMarker);
//
//            double reformate = distance / 1000;
//
//            Toast.makeText(this, "Distance is: " + String.format("%.3f", reformate) + " km.", Toast.LENGTH_SHORT).show();
//        } else {
//
//            Toast.makeText(this, "You are here now.", Toast.LENGTH_SHORT).show();
//        }
//    }

    public void findRouteFromAtoB(){

    }

    public void searchHospital(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                SearchNearestPlace search = new SearchNearestPlace();

                List<Hospital> hospitals = new ArrayList<Hospital>();

                hospitals = search.searchNearestHospital(context);

                nearestHostpital = null;

                LatLng hospitalLatLng;

                double distance = -1;

                for (Hospital h : hospitals) {

                    hospitalLatLng = new LatLng(h.getLatitude(), h.getLongitude());

                    double computeDistance = SphericalUtil.computeDistanceBetween(currentLatLng, hospitalLatLng);

                    if (distance == -1) {
                        distance = computeDistance;
                    }

                    if (distance > computeDistance) {
                        distance = computeDistance;
                        nearestHostpital = h;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (nearestHostpital != null) {

                            addNeareatHospitalToMap(nearestHostpital);
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }).start();
    }

    public void addNeareatHospitalToMap(Hospital hospital){

        if (hospitalMarker != null) {
            hospitalMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(hospital.getLatitude(), hospital.getLongitude()));
        options.title(hospital.getName()+ ", Hospital");
        options.snippet(hospital.getStreet() + ", " + hospital.getPostcode() );
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_icon));

        hospitalMarker = map.addMarker(options);
        hospitalMarker.setTag(hospital.getPhone());
        markerList.add(hospitalMarker);
    }

    public void searchPolice(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                SearchNearestPlace search = new SearchNearestPlace();

                List<Police> polices = search.searchNearestPolice(context);

                nearestPolice = null;

                LatLng policeLatLng;

                double distance = -1;

                for (Police p : polices) {

                    policeLatLng = new LatLng(p.getLatitude(), p.getLongitude());

                    double computeDistance = SphericalUtil.computeDistanceBetween(currentLatLng, policeLatLng);

                    if (distance == -1) {
                        distance = computeDistance;
                    }

                    if (distance > computeDistance) {
                        distance = computeDistance;
                        nearestPolice = p;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (nearestPolice != null) {
                            addNeareatPoliceToMap(nearestPolice);
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }).start();
    }

    public void addNeareatPoliceToMap(Police police){

        if (policeMarker != null) {
            policeMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(police.getLatitude(), police.getLongitude()));
        options.title(police.getStation() + ", Police Station");
        options.snippet(police.getPsa() + ", " + police.getPostcode());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.police_icon));

        policeMarker = map.addMarker(options);
        policeMarker.setTag(police.getPhoneNum());
        markerList.add(policeMarker);

    }

    public void searchBoatAccess(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                SearchNearestPlace search = new SearchNearestPlace();

                List<BoatAccess> boatAccesses = search.searchNearestBoatAccess(context);

                nearestBoatAccess = null;

                secondBoatAccess = null;

                LatLng boatAccessLatlng;

                double distance = -1;

                for (BoatAccess b : boatAccesses) {

                    boatAccessLatlng = new LatLng(b.getLatitude(), b.getLongitude());

                    double computeDistance = SphericalUtil.computeDistanceBetween(currentLatLng, boatAccessLatlng);

                    if (distance == -1) {
                        distance = computeDistance;
                    }

                    if (distance > computeDistance) {
                        distance = computeDistance;

                        secondBoatAccess = nearestBoatAccess;
                        nearestBoatAccess = b;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (nearestBoatAccess != null) {
                            addNearestBoatAccessToMap(nearestBoatAccess);
                            markerList.add(boatAccessMarker);
                            if (secondBoatAccess != null) {
//                                secondBoatAccessMarker = addBoatAccessToMap(secondBoatAccess);
                                addSecondBoatAccessToMap(secondBoatAccess);
//                                markerList.add(secondBoatAccessMarker);
                            }
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }).start();
    }

    public void addNearestBoatAccessToMap(BoatAccess boatAccess){

        if (boatAccessMarker != null) {
            boatAccessMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatAccess.getLatitude(), boatAccess.getLongitude()));
        options.title(boatAccess.getName() + ", " + boatAccess.getType() + ", Boat Ramp");
        options.snippet(boatAccess.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.boat_point_icon));

        boatAccessMarker = map.addMarker(options);
        markerList.add(boatAccessMarker);

//        return map.addMarker(options);
    }

    public void addSecondBoatAccessToMap(BoatAccess boatAccess){

        if (secondBoatAccessMarker != null) {
            secondBoatAccessMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatAccess.getLatitude(), boatAccess.getLongitude()));
        options.title(boatAccess.getName() + ", " + boatAccess.getType() + ", Boat Ramp");
        options.snippet(boatAccess.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.boat_point_icon));

        secondBoatAccessMarker = map.addMarker(options);
        markerList.add(secondBoatAccessMarker);
    }

    public void searchBoatMooring(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                SearchNearestPlace search = new SearchNearestPlace();

                List<BoatMooring> boatMoorings = search.searchNearestBoatMooring(context);

                nearestBoatMooring = null;
                secondBoatMooring = null;

                LatLng boatMooringLatlng;

                double distance = -1;

                for (BoatMooring b : boatMoorings) {

                    boatMooringLatlng = new LatLng(b.getLatitude(), b.getLongitude());

                    double computeDistance = SphericalUtil.computeDistanceBetween(currentLatLng, boatMooringLatlng);

                    if (distance == -1) {
                        distance = computeDistance;
                    }

                    if (distance > computeDistance) {
                        distance = computeDistance;

                        secondBoatMooring = nearestBoatMooring;
                        nearestBoatMooring = b;
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (nearestBoatMooring != null) {
                            addNearestBoatMooringToMap(nearestBoatMooring);
                            if (secondBoatMooring != null){
                                addSecondBoatMooringToMap(secondBoatMooring);
                            }
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    public void addNearestBoatMooringToMap(BoatMooring boatMooring){

        if (boatMooringMarker != null) {
            boatMooringMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatMooring.getLatitude(), boatMooring.getLongitude()));
        options.title(boatMooring.getName() + ", " + boatMooring.getType() + ", Boat Mooring");
        options.snippet(boatMooring.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.mooring_icon1));

        boatMooringMarker = map.addMarker(options);
        markerList.add(boatMooringMarker);
    }

    public void addSecondBoatMooringToMap(BoatMooring boatMooring){

        if (secondBoatMooringMarker != null) {
            secondBoatMooringMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatMooring.getLatitude(), boatMooring.getLongitude()));
        options.title(boatMooring.getName() + ", " + boatMooring.getType() + ", Boat Ramp");
        options.snippet(boatMooring.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.mooring_icon1));

        secondBoatMooringMarker = map.addMarker(options);
        markerList.add(secondBoatMooringMarker);
    }

    public void searchHospitalAtOtherPlace(final LatLng searchLatLng){

                SearchNearestPlace search = new SearchNearestPlace();

                List<Hospital> hospitals = new ArrayList<Hospital>();
                hospitals = search.searchNearestHospital(context);
                nearestHostpital = null;
                LatLng hospitalLatLng;
                double distance = -1;

                for (Hospital h : hospitals) {

                    hospitalLatLng = new LatLng(h.getLatitude(), h.getLongitude());
                    double computeDistance = SphericalUtil.computeDistanceBetween(searchLatLng, hospitalLatLng);
                    if (distance == -1) {
                        distance = computeDistance;
                    }
                    if (distance > computeDistance) {
                        distance = computeDistance;
                        nearestHostpital = h;
                    }
                }
                if (nearestHostpital != null) {
                    addNeareatToOtherPlaceHospitalToMap(nearestHostpital);
                } else {
                    Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                }
    }

    public void addNeareatToOtherPlaceHospitalToMap(Hospital hospital){

        if (searchHospitalMarker != null) {
            searchHospitalMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(hospital.getLatitude(), hospital.getLongitude()));
        options.title(hospital.getName()+ ", Hospital");
        options.snippet(hospital.getStreet() + ", " + hospital.getPostcode() );
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_icon));

        searchHospitalMarker = map.addMarker(options);
        searchHospitalMarker.setTag(hospital.getPhone());
    }

    public void searchPoliceAtOtherPlace(final LatLng searchLatLng){

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                SearchNearestPlace search = new SearchNearestPlace();

                List<Police> polices = search.searchNearestPolice(context);

                nearestPolice = null;

                LatLng policeLatLng;

                double distance = -1;

                for (Police p : polices) {

                    policeLatLng = new LatLng(p.getLatitude(), p.getLongitude());

                    double computeDistance = SphericalUtil.computeDistanceBetween(searchLatLng, policeLatLng);

                    if (distance == -1) {
                        distance = computeDistance;
                    }

                    if (distance > computeDistance) {
                        distance = computeDistance;
                        nearestPolice = p;
                    }
                }

//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
                        if (nearestPolice != null) {
                            addNeareatOtherPlacePoliceToMap(nearestPolice);
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }
//                    }
//                });
//
//            }
//        }).start();
    }

    public void addNeareatOtherPlacePoliceToMap(Police police){

        if (searchPoliceMarker != null) {
            searchPoliceMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(police.getLatitude(), police.getLongitude()));
        options.title(police.getStation() + ", Police Station");
        options.snippet(police.getPsa() + ", " + police.getPostcode());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.police_icon));

        searchPoliceMarker = map.addMarker(options);
        searchPoliceMarker.setTag(police.getPhoneNum());
//        markerList.add(policeMarker);

    }

    public void searchBoatAccessAtOtherPlace(LatLng latLng){

//        new Thread(new Runnable() {
//            @Override
//            public void run() {

                SearchNearestPlace search = new SearchNearestPlace();

                List<BoatAccess> boatAccesses = search.searchNearestBoatAccess(context);

                nearestBoatAccess = null;

                secondBoatAccess = null;

                LatLng boatAccessLatlng;

                double distance = -1;

                for (BoatAccess b : boatAccesses) {

                    boatAccessLatlng = new LatLng(b.getLatitude(), b.getLongitude());

                    double computeDistance = SphericalUtil.computeDistanceBetween(latLng, boatAccessLatlng);

                    if (distance == -1) {
                        distance = computeDistance;
                    }

                    if (distance > computeDistance) {
                        distance = computeDistance;

                        secondBoatAccess = nearestBoatAccess;
                        nearestBoatAccess = b;
                    }
                }

//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {

                        if (nearestBoatAccess != null) {
                            addNearestOtherPlaceBoatAccessToMap(nearestBoatAccess);
                            markerList.add(boatAccessMarker);
                            if (secondBoatAccess != null) {
//                                secondBoatAccessMarker = addBoatAccessToMap(secondBoatAccess);
                                addSecondOtherPlaceBoatAccessToMap(secondBoatAccess);
//                                markerList.add(secondBoatAccessMarker);
                            }
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }
//
//                    }
//                });
//            }
//        }).start();
    }

    public void addNearestOtherPlaceBoatAccessToMap(BoatAccess boatAccess){

        if (searchBoatAccessMarker != null) {
            searchBoatAccessMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatAccess.getLatitude(), boatAccess.getLongitude()));
        options.title(boatAccess.getName() + ", " + boatAccess.getType() + ", Boat Ramp");
        options.snippet(boatAccess.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.boat_point_icon));

        searchBoatAccessMarker = map.addMarker(options);
//        markerList.add(searchBoatAccessMarker);

//        return map.addMarker(options);
    }

    public void addSecondOtherPlaceBoatAccessToMap(BoatAccess boatAccess){

        if (searchSecondBoatAccessMarker != null) {
            searchSecondBoatAccessMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatAccess.getLatitude(), boatAccess.getLongitude()));
        options.title(boatAccess.getName() + ", " + boatAccess.getType() + ", Boat Ramp");
        options.snippet(boatAccess.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.boat_point_icon));

        searchSecondBoatAccessMarker = map.addMarker(options);
//        markerList.add(searchSecondBoatAccessMarker);
    }

    public void searchBoatMooringAtOtherPlace(LatLng latLng){

//        new Thread(new Runnable() {
//            @Override
//            public void run() {

                SearchNearestPlace search = new SearchNearestPlace();

                List<BoatMooring> boatMoorings = search.searchNearestBoatMooring(context);

                nearestBoatMooring = null;
                secondBoatMooring = null;

                LatLng boatMooringLatlng;

                double distance = -1;

                for (BoatMooring b : boatMoorings) {

                    boatMooringLatlng = new LatLng(b.getLatitude(), b.getLongitude());

                    double computeDistance = SphericalUtil.computeDistanceBetween(latLng, boatMooringLatlng);

                    if (distance == -1) {
                        distance = computeDistance;
                    }

                    if (distance > computeDistance) {
                        distance = computeDistance;

                        secondBoatMooring = nearestBoatMooring;
                        nearestBoatMooring = b;
                    }
                }
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
                        if (nearestBoatMooring != null) {
                            addNearestOtherPlaceBoatMooringToMap(nearestBoatMooring);
                            if (secondBoatMooring != null){
                                addSecondOtherPlaceBoatMooringToMap(secondBoatMooring);
                            }
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }
//                    }
//                });
//            }
//        }).start();
    }

    public void addNearestOtherPlaceBoatMooringToMap(BoatMooring boatMooring){

        if (searchBoatMooringMarker != null) {
            searchBoatMooringMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatMooring.getLatitude(), boatMooring.getLongitude()));
        options.title(boatMooring.getName() + ", " + boatMooring.getType() + ", Boat Mooring");
        options.snippet(boatMooring.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.mooring_icon1));

        searchBoatMooringMarker = map.addMarker(options);
//        markerList.add(boatMooringMarker);
    }

    public void addSecondOtherPlaceBoatMooringToMap(BoatMooring boatMooring){

        if (searchSecondBoatMooringMarker != null) {
            searchSecondBoatMooringMarker.remove();
        }

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(boatMooring.getLatitude(), boatMooring.getLongitude()));
        options.title(boatMooring.getName() + ", " + boatMooring.getType() + ", Boat Ramp");
        options.snippet(boatMooring.getLocation());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.mooring_icon1));

        searchSecondBoatMooringMarker = map.addMarker(options);
//        markerList.add(secondBoatMooringMarker);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.navigation_btn:

                if (endMarker == null) {
                    Toast.makeText(this, "Please find your destination.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (popupWindow.getVisibility() != View.VISIBLE) {
                    animOpen(popupWindow);
                }
                if (startMarker == null) {
                    startPoint.setText("    Current Location");
                    startMarker = marker;
                } else {
                    startPoint.setText("    " + startMarker.getTitle());
                }

                endPoint.setText("    " + endMarker.getTitle());

                navigationBtn.setVisibility(View.GONE);
//                endMarker = searchMarker;
//                findRoute();
//                Toast.makeText(this, "In progress...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.start_navigation_btn:

                if (polyline != null) {
                    polyline.remove();
                }

                if (startMarker == null) {
                    startMarker = marker;
                }
//                if (endMarker == null) {
//                    endMarker = marker;
//                }

                if (startPoint.getText().equals(endPoint.getText())) {
                    Toast.makeText(this, "Your are here now.", Toast.LENGTH_SHORT).show();
                }

//                if (startPoint.getText().equals("    Current Location")) {
//                    startMarker = currentMarker;
//                }

                if (carMode.getVisibility() == View.VISIBLE) {
                    routeDrawer(startMarker.getPosition(), endMarker.getPosition());
                } else {
                    PolylineOptions rectOptions = new PolylineOptions()
                            .add(startMarker.getPosition())
                            .add(endMarker.getPosition());

                    // Get back the mutable Polyline
                    polyline = map.addPolyline(rectOptions);
                }

                zoomMapToFitTwoMarkers(startMarker, endMarker);
                double distance = SphericalUtil.computeDistanceBetween(startMarker.getPosition(), endMarker.getPosition());
                double time = (distance / 1000) / 40;
                int hour = (int) time;
                int minus = (int) ((time - hour) * 60);
                Log.i("time", hour + "hour, " + minus + "minus");
                Toast.makeText(this, "Estimate time: " + hour + " hour, " + minus + " minus", Toast.LENGTH_SHORT).show();

                navigationBtn.setVisibility(View.VISIBLE);
                animClose(popupWindow);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
            case R.id.car_mode:
                carMode.setVisibility(View.GONE);
                shipMode.setVisibility(View.VISIBLE);
                break;
            case R.id.ship_mode:
                carMode.setVisibility(View.VISIBLE);
                shipMode.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(navigationView)){
            drawerLayout.closeDrawers();
        } else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = autocompleteAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("1", "Autocomplete item selected: " + item.description);
            if (polyline != null) {
                polyline.remove();
            }
            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(client, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i("2", "Called getPlaceById to get Place details for " + item.placeId);

        }
    };

    private boolean checkLocation(Place place){

        Geocoder geocoder = new Geocoder(context);
        try {
            Address address = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1).get(0);
            String countryCode = address.getCountryCode();
            Log.i("countryCode", countryCode);
            if (!countryCode.equals("AU")) {
                Toast.makeText(context, "Not in Australia", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                int postcode = Integer.parseInt(address.getPostalCode());
                Log.i("postcode", postcode+"");
                if (postcode < 3000 || postcode > 3999) {
                    Toast.makeText(context, "Not in Victoria", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Unknown Location", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("3", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            // Format details of the place for display and show it in a TextView.

            if (!checkLocation(place)) {
                return;
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            // Display the third party attributions if set.
//            if (marker != null) {
//                marker.remove();
//            }

            if (searchMarker != null) {
                searchMarker.remove();
            }

            MarkerOptions options = new MarkerOptions();
            options.position(place.getLatLng());

            options.title(place.getName().toString());
            options.snippet(place.getAddress().toString());
            searchMarker = map.addMarker(options);
            if (place.getPhoneNumber().toString().length() != 0){
                searchMarker.setTag(place.getPhoneNumber());
            }

            LatLng latLng = searchMarker.getPosition();
            endMarker = searchMarker;
            animOpen(popupWindow);

            bottomTitle.setText(endMarker.getTitle());
            bottomContent.setText(endMarker.getSnippet());
            loccationText.setText(endMarker.getPosition().latitude + ", " + endMarker.getPosition().longitude);
            if (endMarker.getTag() != null) {
                phoneText.setText(endMarker.getTag().toString());
            } else {
                phoneText.setText("No Phone Number Available.");
            }

            endPoint.setText(endMarker.getTitle());

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            navigationBtn.setVisibility(View.GONE);


            searchHospitalAtOtherPlace(latLng);
            searchPoliceAtOtherPlace(latLng);
            searchBoatAccessAtOtherPlace(latLng);
            searchBoatMooringAtOtherPlace(latLng);
            removeOtherMarkers(null, null);
            zoomMapToFitMarkers(latLng, searchHospitalMarker, searchPoliceMarker,
                    searchBoatAccessMarker, searchSecondBoatAccessMarker,
                    searchBoatMooringMarker, searchSecondBoatMooringMarker);

            autoCompleteTextView.setText("");
            autoCompleteTextView.setCursorVisible(false);
            Log.i("4", "Place details received: " + place.getName());

            places.release();
        }
    };

    private AdapterView.OnItemClickListener startPointClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = autocompleteAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("1", "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(client, placeId);
            placeResult.setResultCallback(startPointUpdatePlaceDetailsCallback);
            Log.i("2", "Called getPlaceById to get Place details for " + item.placeId);

        }
    };

    private ResultCallback<PlaceBuffer> startPointUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("3", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            if (!checkLocation(place)) {
                return;
            }

            // Display the third party attributions if set.
            if (startMarker != null) {
                startMarker.remove();
            }
            if (searchMarker != null) {
                searchMarker.remove();
            }
            MarkerOptions options = new MarkerOptions();
            options.position(place.getLatLng());

            options.title(place.getName().toString());
            options.snippet(place.getAddress().toString());

            startMarker = map.addMarker(options);
            if (place.getPhoneNumber().toString().length() != 0){
                startMarker.setTag(place.getPhoneNumber());
            }

            Log.i("4", "Place details received: " + place.getName());

            places.release();
        }
    };

    private AdapterView.OnItemClickListener endPointClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = autocompleteAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("1", "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(client, placeId);
            placeResult.setResultCallback(endPointUpdatePlaceDetailsCallback);
            Log.i("2", "Called getPlaceById to get Place details for " + item.placeId);

        }
    };

    private ResultCallback<PlaceBuffer> endPointUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("3", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            if (!checkLocation(place)) {
                return;
            }

            // Display the third party attributions if set.
            if (endMarker != null){
                endMarker.remove();
            }
            if (searchMarker != null) {
                searchMarker.remove();
            }
            MarkerOptions options = new MarkerOptions();
            options.position(place.getLatLng());

            options.title(place.getName().toString());
            options.snippet(place.getAddress().toString());

            endMarker = map.addMarker(options);
            if (place.getPhoneNumber().toString().length() != 0){
                endMarker.setTag(place.getPhoneNumber());
            }

            Log.i("4", "Place details received: " + place.getName());

            places.release();
        }
    };

    private void animOpen(final  View view){
        view.setVisibility(View.VISIBLE);
        ValueAnimator va = createDropAnim(view,0,500);
        va.start();
    }

    private void animClose(final  View view){
        int origHeight = view.getHeight();
        ValueAnimator va = createDropAnim(view,origHeight,0);
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        va.start();
    }

    private ValueAnimator createDropAnim(final  View view, int start, int end) {
        ValueAnimator va = ValueAnimator.ofInt(start, end);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return  va;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            polyline = map.addPolyline(lineOptions);
        }
    }

    private void routeDrawer(LatLng start, LatLng end){
        final String URL1 = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        final String URL2 = "&destination=";
        final String KEY = "&key=AIzaSyA_pHQ6ovzm1IA5YyvQdrFy9tjJlnxtNx4";
        String startPoint = start.latitude + "," + start.longitude;
        String endPoint = end.latitude + "," + end.longitude;
        String url = URL1 + startPoint + URL2 + endPoint + KEY;

        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }

    private void runOverlay(){
        Animation mEnterAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimation.setDuration(600);
        mEnterAnimation.setFillAfter(true);

        Animation mExitAnimation = new AlphaAnimation(1f, 0f);
        mExitAnimation.setDuration(600);
        mExitAnimation.setFillAfter(true);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        startNavigationBtn.setVisibility(View.VISIBLE);

        ChainTourGuide tourGuide1 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Search Bar with Menu")
                        .setDescription("Search location on the map, menu button for more operations...")
                        .setGravity(Gravity.BOTTOM)
                )
                // note that there is no Overlay here, so the default one will be used
                .playLater(autoCompleteTextView);

        ChainTourGuide tourGuide2 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Navigation Button")
                        .setDescription("To get popup window for locations...")
                        .setGravity(Gravity.TOP | Gravity.LEFT)
                        .setBackgroundColor(Color.parseColor("#c0392b"))
                )
                .setOverlay(new Overlay()
                        .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startNavigationBtn.setVisibility(View.GONE);
                                popupWindow.setVisibility(View.VISIBLE);
                                mTourGuideHandler.next();
                            }
                        })
                )
                .playLater(startNavigationBtn);

        ChainTourGuide tourGuide3 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Start Navigation Button")
                        .setDescription("To start your navigation....")
                        .setGravity(Gravity.TOP)
                )
                // note that there is no Overlay here, so the default one will be used
                .playLater(navigationBtn);

        ChainTourGuide tourGuide4 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Car Mode")
                        .setDescription("To change navigation mode from car to ship...")
                        .setGravity(Gravity.BOTTOM)
                ).setOverlay(new Overlay()
                        .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.setVisibility(View.GONE);
                        startNavigationBtn.setVisibility(View.VISIBLE);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        mTourGuideHandler.next();
                    }
                }))
                // note that there is no Overlay here, so the default one will be used
                .playLater(carMode);

        Sequence sequence = new Sequence.SequenceBuilder()
                .add(tourGuide1, tourGuide2, tourGuide3, tourGuide4)
                .setDefaultOverlay(new Overlay()
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mTourGuideHandler.next();
                            }
                        })
                )
                .setDefaultPointer(null)
                .setContinueMethod(Sequence.ContinueMethod.OverlayListener)
                .build();

        mTourGuideHandler = ChainTourGuide.init(this).playInSequence(sequence);
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
