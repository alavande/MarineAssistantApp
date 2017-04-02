package com.alavande.marineassistant;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener, FloatingSearchView.OnSearchListener,
        GoogleMap.OnMyLocationButtonClickListener, NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private DrawerLayout drawerLayout;
    private CoordinatorLayout coordinator;
    private NavigationView navigationView;
    private LinearLayout llBottomSheet;

    private BottomSheetBehavior bottomSheetBehavior;

    private Polyline polyline;

    private MapFragment mapFragment;
    private GoogleMap map;
    private Marker marker, hospitalMarker, policeMarker, searchMarker,
            boatAccessMarker, boatMooringMarker, secondBoatAccessMarker, secondBoatMooringMarker;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private View mapView;
    private LatLng currentLatLng;

    private FloatingSearchView searchView;

    private Context context;
    private Handler handler;

    private Hospital nearestHostpital;
    private Police nearestPolice;
    private BoatAccess nearestBoatAccess, secondBoatAccess;
    private BoatMooring nearestBoatMooring, secondBoatMooring;

    private TextView bottomTitle, bottomContent, phoneText, loccationText;
    private Button navigationBtn;
    private PopupWindow popupWindow;

    private List<Marker> markerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        context = this;
        handler = new Handler();

        coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        navigationBtn = (Button) findViewById(R.id.navigation_btn);
        navigationBtn.setOnClickListener(this);

        bottomTitle = (TextView) findViewById(R.id.map_info_title_text);
        bottomContent = (TextView) findViewById(R.id.map_info_content_text);
        phoneText = (TextView) findViewById(R.id.map_info_phone_text);
        loccationText = (TextView) findViewById(R.id.map_info_location_text);

        drawerLayout = (DrawerLayout) findViewById(R.id.left_drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        searchView = (FloatingSearchView) findViewById(R.id.search_view);
        searchView.attachNavigationDrawerToMenuButton(drawerLayout);
        searchView.setOnSearchListener(this);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        markerList = new ArrayList<Marker>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

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

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    public void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (marker != null) {
            marker.remove();
        }

        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        addMarkerToMap(currentLatLng);

        new Thread(new Runnable() {
            @Override
            public void run() {
                searchHospital();
                searchPolice();
                searchBoatAccess();
                searchBoatMooring();
            }
        }).start();

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));

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

        searchOnMap(currentQuery);
    }

    public void searchOnMap(String keyWord){
        if (marker != null) {
            marker.remove();
        }

        if (polyline != null) {
            polyline.remove();
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(keyWord + "Victoria, Australia", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses.size() !=0) {

            if (Integer.parseInt(addresses.get(0).getPostalCode()) < 3000 || Integer.parseInt(addresses.get(0).getPostalCode()) >= 4000){
                Toast.makeText(this, "No result found, please check your key words.", Toast.LENGTH_SHORT).show();
            } else {
                Address address = addresses.get(0);
                LatLng searchLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                MarkerOptions options = new MarkerOptions();
                options.position(searchLatLng);

                options.title(address.getAddressLine(0) + ", " + address.getLocality() + ", " + address.getPostalCode());
                marker = map.addMarker(options);

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 17));
            }

        }  else {
            Toast.makeText(this, "No result found, please check your key words.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {

        if (marker != null) {
            marker.remove();
        }

        if (polyline != null) {
            polyline.remove();
        }

        addMarkerToMap(currentLatLng);

        removeOtherMarkers(null, null);

        for (Marker m : markerList) {
            if (!m.isVisible()){
                m.setVisible(true);
            }
        }

        return true;
    }

    public void addMarkerToMap(LatLng addLatLng){

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
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 17));
            }
        }.execute(addLatLng);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (polyline != null) {
            polyline.remove();
        }

        switch (item.getItemId()) {
            case R.id.nav_police:

//                searchPolice();
                removeOtherMarkers(policeMarker, null);
                zoomMapToFitMarker(policeMarker);
                break;
            case R.id.nav_hospital:

//                searchHospital();
                removeOtherMarkers(hospitalMarker, null);
                zoomMapToFitMarker(hospitalMarker);
                break;
            case R.id.nav_boat_access:

//                searchBoatAccess();
                removeOtherMarkers(boatAccessMarker, secondBoatAccessMarker);
                if (secondBoatAccessMarker != null) {
                    zoomMapToFitTwoMarkers(boatAccessMarker, secondBoatAccessMarker);
                } else {
                    zoomMapToFitMarker(boatAccessMarker);
                }
                break;
            case R.id.nav_boat_mooring:

//                searchBoatMooring();
                removeOtherMarkers(boatMooringMarker, secondBoatMooringMarker);
                if (secondBoatMooringMarker != null) {
                    zoomMapToFitTwoMarkers(boatMooringMarker, secondBoatMooringMarker);
                } else {
                    zoomMapToFitMarker(boatMooringMarker);
                }
//                Toast.makeText(this, "In progress...", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        drawerLayout.closeDrawers();
        return true;
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

    public void zoomMapToFitMarker(Marker marker){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentLatLng);
        builder.include(marker.getPosition());

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

    @Override
    public void onMapClick(LatLng latLng) {

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if (marker != null) {
            marker.remove();
        }

        addMarkerToMap(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        searchMarker = marker;

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

        return true;
    }

    public void findRoute(){

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        Double distance = SphericalUtil.computeDistanceBetween(currentLatLng, searchMarker.getPosition());

        if (polyline != null) {
            polyline.remove();
        }

        if (distance > 0) {
            PolylineOptions rectOptions = new PolylineOptions()
                    .add(currentLatLng)
                    .add(searchMarker.getPosition());

            // Get back the mutable Polyline
            polyline = map.addPolyline(rectOptions);

            zoomMapToFitMarker(searchMarker);

            double reformate = distance / 1000;

            Toast.makeText(this, "Distance is: " + String.format("%.3f", reformate) + " km.", Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, "You are here now.", Toast.LENGTH_SHORT).show();
        }
    }

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
                            addNeareatBoatAccessToMap(nearestBoatAccess);
                            if (secondBoatAccess != null) {
                                addSecondBoatAccessToMap(secondBoatAccess);
                            }
                        } else {
                            Toast.makeText(context, "Nothing found.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }).start();
    }

    public void addNeareatBoatAccessToMap(BoatAccess boatAccess){

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
                            addNeareatBoatMooringToMap(nearestBoatMooring);
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

    public void addNeareatBoatMooringToMap(BoatMooring boatMooring){

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


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.navigation_btn:

//                findRoute();
                Toast.makeText(this, "In progress...", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

//    public void popupWindowForFacilities(){
//
//        popupWindow = new PopupWindow(MapActivity.this);
//        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//        popupWindow.setHeight(600);
//        popupWindow.setContentView(LayoutInflater.from(MapActivity.this).inflate(R.layout.popup_window_layout, null));
//        popupWindow.setOutsideTouchable(false);
//        popupWindow.setFocusable(true);
//        popupWindow.showAtLocation(getCurrentFocus(), 0, 0, 0);
//
//    }


}
