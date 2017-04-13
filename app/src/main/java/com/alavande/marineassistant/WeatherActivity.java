package com.alavande.marineassistant;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.SnapshotApi;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private TextView windspeedInfo;
    private TextView tideInfo;
    private TextView tide0;
    private TextView tide1;
    private TextView tide2;
    private TextView tide3;
    private TextView windspeed;
    private TextView weatherInfo;
    private ImageView weatherpic;
    private static final int weather = 1;
    private static final int sendtide = 1;
    private static final int Wind = 1;
    private static final int changepic = 1;
    private GoogleApiClient client;
    private LocationManager locationManager;//
    private LocationRequest locationRequest;
    private Location location;

    private double lat, lon;

    private LatLng currentLatLng;
    private String provider;//


    public void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 1, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .build();
        client.connect();
    }

    Handler tide0handler = new Handler() {


        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == sendtide) {
                tide0.setText(data);
            }
        }


    };
    Handler tide1handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == sendtide) {
                tide1.setText(data);
            }
        }


    };
    Handler tide2handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == sendtide) {
                tide2.setText(data);
            }
        }


    };
    Handler tide3handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == sendtide) {
                tide3.setText(data);
            }
        }


    };
    Handler weatherHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == weather) {
                weatherInfo.setText(data);
            }
        }


    };

    Handler windHandler = new Handler() {

        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");

            if (msg.what == Wind) {
                windspeedInfo.setText(data);
            }
        }


    };
    Handler picHander = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == changepic) {
                changeWeatherImg(data);
            }
        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        tideInfo = (TextView) findViewById(R.id.tideInfo);
        tide0 = (TextView) findViewById(R.id.tide0);
        tide1 = (TextView) findViewById(R.id.tide1);
        tide2 = (TextView) findViewById(R.id.tide2);
        tide3 = (TextView) findViewById(R.id.tide3);
        windspeed = (TextView) findViewById(R.id.windspeed);
        windspeedInfo = (TextView) findViewById(R.id.windspeedInfo);
        weatherInfo = (TextView) findViewById(R.id.weatherInfo);
        weatherpic = (ImageView) findViewById(R.id.weatherpic);
//        locationRequest = new LocationRequest();
//        locationRequest.setInterval(5000);
//        locationRequest.setFastestInterval(5000);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, (com.google.android.gms.location.LocationListener) this);
        }
        buildGoogleApiClient();
        checkLocationPermission();
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                client, locationRequest, (com.google.android.gms.location.LocationListener) this);
//        location=LocationServices.FusedLocationApi.getLastLocation(
//                client);

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
                        Log.i("location", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                    }
                });

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendRequestWithOKHttp(lat,lon);
            }
        }).start();

    }

    private String buildWeatherAPIString(double lat,double lon){
        String key="&appid=4411135c80bf7b41e26384708252cfa6";
        String link="http://api.openweathermap.org/data/2.5/weather?lat=";
        String anotherPart="&lon=";
        link=link+lat+anotherPart+lon+key;
        return link;
    }
    private String buildTideAPIString(double lat,double lon){
        String key="&key=92946a48-2c35-4af6-b8c0-4027352f4c18";
        String link="https://www.worldtides.info/api?datum=LAT&extremes&lat=";
        String anotherPart="&lon=";
        link=link+lat+anotherPart+lon+key;
        return link;
    }
    private void sendRequestWithOKHttp(double lat,double lon) {
        try {
            OkHttpClient client = new OkHttpClient();
            String weatherURLString= buildWeatherAPIString(lat,lon);
            Request request1 = new Request.Builder().url(weatherURLString).build();
            Response response1 = client.newCall(request1).execute();
            String responseDate1 = response1.body().string();
            parseWeatherJSON(responseDate1);
            String tideURLString= buildTideAPIString(lat,lon);
            Request request2 = new Request.Builder().url(tideURLString).build();
            Response response2 = client.newCall(request2).execute();
            String responseDate2 = response2.body().string();
            parseTideJSON(responseDate2);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                ActivityCompat.requestPermissions(WeatherActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(WeatherActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }private void parseWeatherJSON(String responseData) {
        String toPrint = "bbb";
        String windspeed = "aaa";
        String windDegree="";
        String theweather = "";
        try {
            JSONObject jsonObject = new JSONObject(responseData);

            JSONObject main = jsonObject.getJSONObject("main");
            String String1=main.getString("temp_min");
            String String2=main.getString("temp_max");
            Double temp_min= (Double.parseDouble(String1)-273.15);
            Double temp_max= (Double.parseDouble(String2)-273.15);
            toPrint="Min "+Double.toString(Math.round(temp_min))+" C"+"\n"+"Max "+Double.toString(Math.round(temp_max))+" C";

            JSONObject wind = jsonObject.getJSONObject("wind");
            windspeed =wind.getString("speed");
            windspeed=windspeed+"km/h";

            String deg= wind.getString("deg");
            Double Degree=Double.parseDouble(deg);
            int windDeg=(int)Math.round(Degree);
            if(windDeg>337 || windDeg<=22){
                windDegree="North";
            }
            if(windDeg>22 && windDeg<=67){
                windDegree="NorthEast";
            }
            if(windDeg>67 && windDeg<=112){
                windDegree="East";
            }
            if(windDeg>112 && windDeg<=157){
                windDegree="SouthEast";
            }
            if(windDeg>157 && windDeg<=202){
                windDegree="South";
            }
            if(windDeg>202 && windDeg<=247){
                windDegree="SouthWest";
            }
            if(windDeg>247 && windDeg<=292){
                windDegree="West";
            }
            if (windDeg > 292 && windDeg <= 337) {
                windDegree = "NorthWest";
            }
            windspeed=windDegree+" "+windspeed;

            JSONObject weather = jsonObject.getJSONObject("weather");
            JSONObject weathers=weather.getJSONObject("0");
            theweather=weathers.getString("main");


        } catch (JSONException e) {
            e.printStackTrace();
        }



        Message weathermessage = Message.obtain();
        Bundle bundle1 = new Bundle();
        bundle1.putString("msg", toPrint);
        weathermessage.setData(bundle1);
        weathermessage.what = weather;
        weatherHandler.sendMessage(weathermessage);

        Message windmessage = Message.obtain();
        Bundle bundle2 = new Bundle();
        bundle2.putString("msg", windspeed);
        windmessage.setData(bundle2);
        windmessage.what = Wind;
        windHandler.sendMessage(windmessage);


        Message message3 = Message.obtain();
        Bundle bundle3 = new Bundle();
        bundle3.putString("msg", theweather);
        message3.setData(bundle3);
        message3.what = changepic;
        picHander.sendMessage(message3);

    }

    private void parseTideJSON(String responseData) {
        String[] toPrint = new String[4];
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray extremes = jsonObject.getJSONArray("extremes");
            for(int i=0;i<=3;i++){
                toPrint[i]=getTideInfo(extremes,i);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Handler[] handlers = new Handler[4];
        handlers[0]=tide0handler;
        handlers[1]=tide1handler;
        handlers[2]=tide2handler;
        handlers[3]=tide3handler;
        for(int i=0;i<=3;i++){
            sentMessage(toPrint[i],handlers[i]);
        }

    }

    private String getTideInfo(JSONArray extremes, int i){
        String string="";
        try{
            JSONObject extreme= extremes.getJSONObject(i);
            string =extreme.getString("date");
            String height =extreme.getString("height");
            String type =extreme.getString("type");
            String[] array0 = string.split("T");
            array0 = array0[1].split("\\+");
            string=array0[0]+" "+height+" "+type;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return string;
    }
    private void sentMessage(String toPrint,Handler handler){
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("msg", toPrint);
        message.setData(bundle);
        message.what = sendtide;
        handler.sendMessage(message);
    }
    private void changeWeatherImg(String weather){
        switch (weather){
            case "Clouds":
                weatherpic.setImageResource(R.drawable.clouds);
            case "Clear":
                weatherpic.setImageResource(R.drawable.clear);
            case "Rain":
                weatherpic.setImageResource(R.drawable.rain);
            case "Snow":
                weatherpic.setImageResource(R.drawable.sonw);
            default :
                weatherpic.setImageResource(R.drawable.other);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        locationRequest = new LocationRequest();
//        locationRequest.setInterval(5000);
//        locationRequest.setFastestInterval(5000);
//        if (ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, (com.google.android.gms.location.LocationListener) this);
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
