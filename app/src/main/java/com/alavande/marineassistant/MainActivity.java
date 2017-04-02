package com.alavande.marineassistant;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.contextmanager.internal.TimeFilterImpl;
import com.mingle.widget.LoadingView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    private ImageButton emergencyBtn, weatherBtn;
    private Button plannerBtn;
    private Intent intent;
    private VideoView videoView;

    private AlertDialog build;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent();



        emergencyBtn = (ImageButton) findViewById(R.id.emergency_btn);
        emergencyBtn.setOnClickListener(this);

        weatherBtn = (ImageButton) findViewById(R.id.weather_btn);
        weatherBtn.setOnClickListener(this);

        plannerBtn = (Button) findViewById(R.id.planner_btn);
        plannerBtn.setOnClickListener(this);

//        showOneDialog();
//        build.dismiss();

//        videoView = (VideoView) findViewById(R.id.video_background);
//        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.ocean);
//        videoView.setVideoURI(uri);
//        videoView.setOnCompletionListener(this);
//        MediaController mc = new MediaController(this);
//        mc.setAnchorView(videoView);
//        mc.setMediaPlayer(videoView);
//        videoView.setMediaController(mc);
//
//        videoView.setOnPreparedListener(PreparedListener);
//        videoView.requestFocus();

//        videoView.start();



    }

//    MediaPlayer.OnPreparedListener PreparedListener = new MediaPlayer.OnPreparedListener(){
//
//        @Override
//        public void onPrepared(MediaPlayer m) {
//            try {
//                if (m.isPlaying()) {
//                    m.stop();
//                    m.release();
//                    m = new MediaPlayer();
//                }
//                m.setVolume(0f, 0f);
//                m.setLooping(true);
//                m.start();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emergency_btn:
                intent.setClass(this, MapActivity.class);

                final Toast toast = Toast.makeText(this, "Loading....", Toast.LENGTH_SHORT);

                new AsyncTask<Intent, Void, Void>(){

                    @Override
                    protected Void doInBackground(Intent... intents) {

                        startActivity(intents[0]);
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {

                        toast.show();
//                        build.show();
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        toast.cancel();
//                        build.dismiss();
                    }
                }.execute(intent);

                break;
            case R.id.weather_btn:

                intent.setClass(this, WeatherActivity.class);
                startActivity(intent);
//                Toast.makeText(this, "In progress....", Toast.LENGTH_SHORT).show();
                break;
            case R.id.planner_btn:

//                intent.setClass(this, PlannerActivity.class);
//                startActivity(intent);

                Toast.makeText(this, "In progress....", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

//    public void searchMap(){
//        Uri gmmIntentUri = Uri.parse("geo:0,0?q=1600 Amphitheatre Parkway, Mountain+View, California");
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//        mapIntent.setPackage("com.google.android.apps.maps");
//        startActivity(mapIntent);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.start();
        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (videoView != null) {
            videoView.start();
        }
    }

    private void showOneDialog() {
        build = new AlertDialog.Builder(this).create();
        //自定义布局
        View view = getLayoutInflater().inflate(R.layout.loading_view_layout, null);
        //把自定义的布局设置到dialog中，注意，布局设置一定要在show之前。从第二个参数分别填充内容与边框之间左、上、右、下、的像素
        build.setView(view, 0, 0, 0, 0);
        //一定要先show出来再设置dialog的参数，不然就不会改变dialog的大小了
    }
}
