package com.alavande.marineassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private TextView windspeedInfo,airpressureInfo,airpressure,tideInfo,windspeed;
    private LineChart chart;
    private TextView weatherInfo,weather1,weather2,weather3,weather4;
    private ImageView weatherpic,windpic,day1pic,day2pic,day3pic,day4pic,refresh;
    private static final int weather = 1,sendtide = 1,Wind = 1,changepic = 1,pressure = 1,location=1,windeg=1,futures=1,changeweatherspics=1;
    private SpinKitView loadingView;
    private PlaceAutocompleteAdapter autocompleteAdapter;
    private GoogleApiClient client;
    private double lat, lon;
    private String time1,time2,time3,time4;
    private SimpleDateFormat format;
    private Calendar c;
    String[] tidetime= new String[]{time1,time2,time3,time4};
    private static final LatLngBounds BOUNDS_GREATER_VICTORIA = new LatLngBounds(
            new LatLng(-38.055358, 140.966645),
            new LatLng(-37.502666, 149.878801));

    private Context context;
    private AutoCompleteTextView locationText;

    public void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 1, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        client.connect();
    }

    Handler tide0handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {

            float[] data = msg.getData().getFloatArray("msg");


            if (msg.what == sendtide) {
                doSomething(data[0],data[1],data[2],data[3]);
            }
        }


    };

    Handler weatherHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == weather) {
                weatherInfo.setText(data);

            }
        }


    };
    Handler locationHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == location) {
                windspeed.setText(data);

            }
        }


    };
    Handler pressureHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == pressure) {
                airpressureInfo.setText(data);
            }
        }


    };

    Handler windHandler = new Handler() {

        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(Message msg) {

            String data = msg.getData().getString("msg");

            if (msg.what == Wind) {
                windspeedInfo.setText(data);
            }
        }


    };
    Handler picHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String data = msg.getData().getString("msg");
            if (msg.what == changepic) {
                changeWeatherImg(data,0);
            }
        }


    };
    Handler windDegreeHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String data = msg.getData().getString("msg");
            if (msg.what == windeg) {
                changeArrow(data);
            }
        }


    };
    Handler futureHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {

            String[] data = msg.getData().getStringArray("msg");
            if (msg.what == futures) {
                weather1.setText(data[0]);
                weather2.setText(data[1]);
                weather3.setText(data[2]);
                weather4.setText(data[3]);
            }
        }


    };
    Handler weatherPicHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {

            String[] data = msg.getData().getStringArray("msg");
            if (msg.what == changeweatherspics) {
                changeWeatherImg(data[0],1);
                changeWeatherImg(data[1],2);
                changeWeatherImg(data[2],3);
                changeWeatherImg(data[3],4);
            }
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        dismissStatusBar();
        context = this;

        tideInfo = (TextView) findViewById(R.id.tideInfo);
        chart = (LineChart) findViewById(R.id.chart);
        windspeed = (TextView) findViewById(R.id.windspeed);
        windspeed.setOnClickListener(this);
        windspeedInfo = (TextView) findViewById(R.id.windspeedInfo);
        weatherInfo = (TextView) findViewById(R.id.weatherInfo);
        weather1=(TextView) findViewById(R.id.weather1);
        weather2=(TextView) findViewById(R.id.weather2);
        weather3=(TextView) findViewById(R.id.weather3);
        weather4=(TextView) findViewById(R.id.weather4);
        airpressureInfo = (TextView) findViewById(R.id.airpressureInfo);
        airpressure = (TextView) findViewById(R.id.airpressure);
        weatherpic = (ImageView) findViewById(R.id.weatherpic);
        windpic= (ImageView) findViewById(R.id.windpic);
        day1pic= (ImageView) findViewById(R.id.day1pic);
        day2pic= (ImageView) findViewById(R.id.day2pic);
        day3pic= (ImageView) findViewById(R.id.day3pic);
        day4pic= (ImageView) findViewById(R.id.day4pic);
        refresh= (ImageView) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        }

        buildGoogleApiClient();
        checkLocationPermission();

        loadingView = (SpinKitView) findViewById(R.id.spin_kit);
        loadingView.setVisibility(View.GONE);
//        Log.i("aaa", "aaaaaa");
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

                        new AsyncTask<Double, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                loadingView.setVisibility(View.VISIBLE);
                                super.onPreExecute();
                            }

                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            protected Void doInBackground(Double... doubles) {
                                sendRequestWithOKHttp(doubles[0], doubles[1]);
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                loadingView.setVisibility(View.GONE);
                                super.onPostExecute(aVoid);
                            }
                        }.execute(lat, lon);

                    }

                });

    }

    private String buildWeatherAPIString(double lat,double lon){
        String key="&cnt=5&appid=4411135c80bf7b41e26384708252cfa6&units=metric";
        String link="http://api.openweathermap.org/data/2.5/forecast/daily?lat=";
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendRequestWithOKHttp(double lat, double lon) {
        try {
            OkHttpClient client = new OkHttpClient();
            String weatherURLString= buildWeatherAPIString(lat,lon);
            System.out.print(weatherURLString);
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
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseWeatherJSON(String responseData) {

        String windspeed = "";
        String windDegree="";
        String theweather = "";
        String airpressure = "";
        String loc="";
        String deg="";
        String[] day0 =new String[5];
        String[] day1=new String[5];
        String[] day2=new String[5];
        String[] day3=new String[5];
        String[] day4=new String[5];
        String weaterday1="";
        String weaterday2="";
        String weaterday3="";
        String weaterday4="";
        String tempday0="";
        String tempday1="";
        String tempday2="";
        String tempday3="";
        String tempday4="";
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject city =jsonObject.getJSONObject("city");
            loc =city.getString("name");
            JSONArray list = jsonObject.getJSONArray("list");
            JSONObject today=list.getJSONObject(0);
            JSONObject plus1day=list.getJSONObject(1);
            JSONObject plus2day=list.getJSONObject(2);
            JSONObject plus3day=list.getJSONObject(3);
            JSONObject plus4day=list.getJSONObject(4);
            day0=formatWeather(today);
            day1=formatWeather(plus1day);
            day2=formatWeather(plus2day);
            day3=formatWeather(plus3day);
            day4=formatWeather(plus4day);
            tempday0=day0[0];
            deg=day0[4];
            //to do

            weaterday1=day1[1];
            weaterday2=day2[1];
            weaterday3=day3[1];
            weaterday4=day4[1];
            airpressure=day0[2];
            windspeed=day0[3];
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
            theweather=day0[1];
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] futureWeek = changeFutureWeek();
        tempday1=futureWeek[0]+"\n"+day1[0];
        tempday2=futureWeek[1]+"\n"+day2[0];
        tempday3=futureWeek[2]+"\n"+day3[0];
        tempday4=futureWeek[3]+"\n"+day4[0];

        Message locationMessage = Message.obtain();
        Bundle bundle0 = new Bundle();
        bundle0.putString("msg", loc);
        locationMessage.setData(bundle0);
        locationMessage.what = location;
        locationHandler.sendMessage(locationMessage);

        Message weathermessage = Message.obtain();
        Bundle bundle1 = new Bundle();
        bundle1.putString("msg", tempday0);
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

        Message message4 = Message.obtain();
        Bundle bundle4 = new Bundle();
        bundle4.putString("msg", airpressure);
        message4.setData(bundle4);
        message4.what = pressure;
        pressureHandler.sendMessage(message4);

        Message message5 = Message.obtain();
        Bundle bundle5 = new Bundle();
        bundle5.putString("msg", windDegree);
        message5.setData(bundle5);
        message5.what = windeg;
        windDegreeHandler.sendMessage(message5);

        String[] future={tempday1,tempday2,tempday3,tempday4};
        Message futureMessage = Message.obtain();
        Bundle bundle6 = new Bundle();
        bundle6.putStringArray("msg", future);
        futureMessage.setData(bundle6);
        futureMessage.what = futures;
        futureHandler.sendMessage(futureMessage);

        String[] theWeathers={weaterday1,weaterday2,weaterday3,weaterday4};
        Message weathersPics = Message.obtain();
        Bundle bundle7 = new Bundle();
        bundle7.putStringArray("msg", theWeathers);
        weathersPics.setData(bundle7);
        weathersPics.what = changeweatherspics;
        weatherPicHandler.sendMessage(weathersPics);
    }
    private String[] formatWeather(JSONObject today) throws JSONException {
        JSONObject temp=today.getJSONObject("temp");
        Double temp_min=temp.getDouble("min");
        Double temp_max=temp.getDouble("max");
        String toPrint="Min "+temp_min+" ℃"+"\n"+"Max "+temp_max+" ℃";
        JSONArray weather = today.getJSONArray("weather");
        JSONObject current_weathers=weather.getJSONObject(0);
        int weatherID=current_weathers.getInt("id");
        String theweather=getWeatherType(weatherID);
        Double pressure=today.getDouble("pressure");
        String airpressure = pressure+ " hpa";
        String windspeed =today.getString("speed");
        windspeed=windspeed+"km/h";
        String deg= today.getString("deg");
        String[] toReturn={toPrint,theweather,airpressure,windspeed,deg};
        return toReturn;
    }
    private void changeArrow(String arrow){
        switch (arrow){
            case "North":
                windpic.setImageResource(R.drawable.north);
                break;
            case "NorthEast":
                windpic.setImageResource(R.drawable.northeast);
                break;
            case "South":
                windpic.setImageResource(R.drawable.south);
                break;
            case "SouthEast":
                windpic.setImageResource(R.drawable.southeast);
                break;
            case "SouthWest":
                windpic.setImageResource(R.drawable.southwest);
                break;
            case "West":
                windpic.setImageResource(R.drawable.west);
                break;
            case "NorthWest":
                windpic.setImageResource(R.drawable.northwest);
                break;
            case "East":
                windpic.setImageResource(R.drawable.east);
                break;
        }
    }
    private void parseTideJSON(String responseData) {
        float[] toPrint = new float[4];
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray extremes = jsonObject.getJSONArray("extremes");
            for(int i=0;i<=3;i++){
                toPrint[i]=getTideInfo(extremes,i);
                getTideTime(extremes,i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i=0;i<=3;i++){
            sentMessage(toPrint,tide0handler);
        }

    }

    private float getTideInfo(JSONArray extremes, int i){
        float height=0.0f;
        try{
            JSONObject extreme= extremes.getJSONObject(i);
            height =(float)extreme.getDouble("height");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return height;
    }
    private void getTideTime(JSONArray extremes, int i){
        String time="";
        try{
            JSONObject extreme= extremes.getJSONObject(i);
            time =extreme.getString("date");
            String[] times=time.split("T");
            String timeToDivide=times[1];
            String[] timez=timeToDivide.split("\\+");
            time=timez[0];
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tidetime[i]=time;
    }
    private void sentMessage(float[] toPrint, Handler handler){
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putFloatArray("msg", toPrint);
        message.setData(bundle);
        message.what = sendtide;
        handler.sendMessage(message);
    }
    private void changeWeatherImg(String weather,int i){
        ImageView toChange=null;
        if(i==0)
            toChange=weatherpic;
        if(i==1)
            toChange=day1pic;
        if(i==2)
            toChange=day2pic;
        if(i==3)
            toChange=day3pic;
        if(i==4)
            toChange=day4pic;
        switch (weather){
            case "Clouds":
                toChange.setImageResource(R.drawable.clouds);
                break;
            case "Clear":
                toChange.setImageResource(R.drawable.clear);
                break;
            case "Rain":
                toChange.setImageResource(R.drawable.rain);
                break;
            case "Snow":
                toChange.setImageResource(R.drawable.sonw);
                break;
            case "Extreme":
                toChange.setImageResource(R.drawable.extreme);
                break;
            case "Mist":
                toChange.setImageResource(R.drawable.mist);
                break;
            case "Thunderstorm":
                toChange.setImageResource(R.drawable.thunderstorm);
                break;
            default :
                toChange.setImageResource(R.drawable.other);
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public void setData(ChartData data) {
        chart.setData((LineData) data);
        chart.invalidate();
        chart.setTouchEnabled(false);
        chart.getAxisLeft().setTextColor(Color.WHITE); // left y-axis
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getAxisRight().setTextColor(Color.WHITE); // left y-axis
        chart.getDescription().setText("");


}

    private void doSomething(float tide1,float tide2,float tide3,float tide4) {
        Entry c1e1 = new Entry(0f, tide1);
        List<Entry> valsComp1 = new ArrayList<>();
        valsComp1.add(c1e1);
        Entry c1e2 = new Entry(1f, tide2); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        Entry c1e3 = new Entry(2f, tide3); // 1 == quarter 2 ...
        valsComp1.add(c1e3);
        Entry c1e4 = new Entry(3f, tide4); // 1 == quarter 2 ...
        valsComp1.add(c1e4);
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Tide height");
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setComp1);
        LineData data = new LineData(dataSets);
        setComp1.setDrawFilled(true);
        setComp1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        data.setValueTextColor(Color.WHITE);
        setData(data);
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

    }
public String getWeatherType(int id){
    String weather="unknown";
    if (id==800){
        weather="Clear";
    }
    if (id>800&&id<=804){
        weather="Clouds";
    }
    if (id>900){
        weather="Extreme";
    }
    if (id>700&&id<800){
        weather="Mist";
    }
    if (id>=600&&id<700){
        weather="Snow";
    }
    if (id>=300&&id<600){
        weather="Rain";
    }
    if (id<300){
        weather="Thunderstorm";
    }
    return weather;
}

    IAxisValueFormatter formatter = new IAxisValueFormatter() {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return tidetime[(int) value];
        }
        public int getDecimalDigits() {  return 0; }
    };

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.windspeed:
//                final EditText locationText = new EditText(this);
                locationText = new AutoCompleteTextView(this);
                locationText.setOnItemClickListener(mAutocompleteClickListener);
                autocompleteAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,
                        client, BOUNDS_GREATER_VICTORIA, null);
                locationText.setAdapter(autocompleteAdapter);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog dialog = builder
                        .setTitle("Enter Location")
                        .setView(locationText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (locationText.getText().length() > 0) {
                                    refreshWeather();
//                                    Toast.makeText(WeatherActivity.this,
//                                            "Ok button clicked: " + locationText.getText() + " input.",
//                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(WeatherActivity.this, "Nothing entered", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                break;
            case R.id.refresh:
                refreshWeather();
                break;
            default:
                break;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)

    private String[] changeFutureWeek(){
        int[] future=getCurrentWeek();
        String[] futures=new String[4];
        for(int i=0;i<4;i++){
            futures[i]=getfutureWeek(future[i]);
        }
        return futures;
    }
    private int[] getCurrentWeek(){
        Calendar c = Calendar.getInstance();
        int current=c.get(Calendar.DAY_OF_WEEK);
        int[] future=new int[4];
        for(int i=0;i<4;i++){
            future[i]=current+i+1;
        }
        return future;
    }

    private String getfutureWeek(int week1){
        String week=String.valueOf(week1);
        if("1".equals(week)){
            week ="Sunday";
        }else if("2".equals(week)){
            week ="Monday";
        }else if("3".equals(week)){
            week ="Tuesday";
        }else if("4".equals(week)){
            week ="Wednesday";
        }else if("5".equals(week)){
            week ="Thursday";
        }else if("6".equals(week)){
            week ="Friday";
        }else if("7".equals(week)){
            week ="Saturday";
        }
        return week;
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
//            if (!checkLocation(place)) {
//                return;
//            }

            locationText.setText(place.getName());
            locationText.dismissDropDown();
            lat = place.getLatLng().latitude;
            lon = place.getLatLng().longitude;

            places.release();
        }
    };

    private void refreshWeather(){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                sendRequestWithOKHttp(lat, lon);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                loadingView.setVisibility(View.GONE);
            }
        }.execute();
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
