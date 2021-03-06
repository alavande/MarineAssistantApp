package com.alavande.marineassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WindForecastActivity extends AppCompatActivity {

    private WebView windForecastView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind_forecast);

        String url = null;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle.getString("url") != null) {
            url = bundle.getString("url");
        } else {
            url = "http://wind.willyweather.com.au/vic.html";
        }
        windForecastView = (WebView) findViewById(R.id.wind_forecast_webview);
        WebSettings settings = windForecastView.getSettings();
        settings.setJavaScriptEnabled(true);
        windForecastView.setWebViewClient(new WebViewClient());
        windForecastView.loadUrl(url);
    }
}
