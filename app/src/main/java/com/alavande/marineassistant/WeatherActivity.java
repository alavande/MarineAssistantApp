package com.alavande.marineassistant;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private TextView windspeedInfo;
    private TextView tideInfo;
    private TextView tide;
    private TextView windspeed;
    private TextView weatherInfo;
    private ImageView weatherpic;
    private Button button_1;
    private static final int weather = 1;
    private static final int sendtide = 1;
    private static final int Wind = 1;
    Handler handler1 = new Handler() {

        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == sendtide) {
                tide.setText(data);
            }
        }


    };
    Handler handler3 = new Handler() {

        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");


            if (msg.what == Wind) {
                windspeedInfo.setText(data);
            }
        }


    };
    Handler handler2 = new Handler() {

        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {

            String data = msg.getData().getString("msg");

            if (msg.what == weather) {
                weatherInfo.setText(data);
            }
        }


    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        button_1 = (Button) findViewById(R.id.button_1);
        tideInfo = (TextView) findViewById(R.id.tideInfo);
        tide = (TextView) findViewById(R.id.tide);
        windspeed = (TextView) findViewById(R.id.windspeed);
        windspeedInfo = (TextView) findViewById(R.id.windspeedInfo);
        weatherInfo = (TextView) findViewById(R.id.weatherInfo);
        weatherpic = (ImageView) findViewById(R.id.weatherpic);
//        button_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendRequestWithOKHttp();
            }
        }).start();
    }
    private String buildWeatherAPIString(String lat,String lon){
        String key="&appid=4411135c80bf7b41e26384708252cfa6";
        String link="http://api.openweathermap.org/data/2.5/weather?lat=";
        String anotherPart="&lon=";
        link=link+lat+anotherPart+lon+key;
        return link;
    }
    private String buildTideAPIString(String lat,String lon){
        String key="&key=92946a48-2c35-4af6-b8c0-4027352f4c18";
        String link="https://www.worldtides.info/api?datum=LAT&extremes&lat=";
        String anotherPart="&lon=";
        link=link+lat+anotherPart+lon+key;
        return link;
    }
    private void sendRequestWithOKHttp() {
        try {
            OkHttpClient client = new OkHttpClient();
            String weatherURLString= buildWeatherAPIString("31","130");
            Request request1 = new Request.Builder().url(weatherURLString).build();
            Response response1 = client.newCall(request1).execute();
            String responseDate1 = response1.body().string();
            parseWeatherJSON(responseDate1);
            String tideURLString= buildTideAPIString("31","130");
            Request request2 = new Request.Builder().url(tideURLString).build();
            Response response2 = client.newCall(request2).execute();
            String responseDate2 = response2.body().string();
            parseTideJSON(responseDate2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseWeatherJSON(String responseData) {
        String toPrint = "this is a fuck";
        String windspeed = "this is a fuck";
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject main = jsonObject.getJSONObject("main");
            JSONObject wind = jsonObject.getJSONObject("wind");
            windspeed =wind.getString("speed");
            windspeed=windspeed+"m/s";
            String String1=main.getString("temp_min");
            String String2=main.getString("temp_max");
            toPrint="Min "+String1+" C"+"\n"+"Max "+String2+" C";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Message message = Message.obtain();
        Message message2 = Message.obtain();
        Bundle bundle1 = new Bundle();
        bundle1.putString("msg", toPrint);
        Bundle bundle2 = new Bundle();
        bundle2.putString("msg", windspeed);
        message.setData(bundle1);
        message.what = weather;
        handler2.sendMessage(message);
        message2.setData(bundle2);
        message2.what = Wind;
        handler3.sendMessage(message2);
    }

    private void parseTideJSON(String responseData) {
        String toPrint = "this is a fuck";
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray extremes = jsonObject.getJSONArray("extremes");
            JSONObject extreme0 = extremes.getJSONObject(0);
            JSONObject extreme1 = extremes.getJSONObject(1);
            JSONObject extreme2 = extremes.getJSONObject(2);
            JSONObject extreme3 = extremes.getJSONObject(3);
            String string0 =extreme0.getString("date");
            String height0 =extreme0.getString("height");
            String type0 =extreme0.getString("type");
            String[] array0 = string0.split("T");
            array0 = array0[1].split("\\+");
            string0=array0[0]+" "+height0+" "+type0;

            String string1 =extreme1.getString("date");
            String height1 =extreme1.getString("height");
            String type1 =extreme1.getString("type");
            String[] array1 = string1.split("T");
            array1 = array1[1].split("\\+");
            string1=array1[0]+" "+height1+" "+type1;

            String string2 =extreme2.getString("date");
            String height2 =extreme2.getString("height");
            String type2 =extreme2.getString("type");
            String[] array2 = string2.split("T");
            array2 = array2[1].split("\\+");
            string2=array2[0]+" "+height2+" "+type2;

            String string3 =extreme3.getString("date");
            String height3 =extreme3.getString("height");
            String type3 =extreme3.getString("type");
            String[] array3 = string3.split("T");
            array3 = array3[1].split("\\+");
            string3=array3[0]+" "+height3+" "+type3;

            toPrint=string0+"  "+string1+"\n"+string2+"  "+string3;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("msg", toPrint);
        message.setData(bundle);
        message.what = sendtide;
        handler1.sendMessage(message);
    }
}
