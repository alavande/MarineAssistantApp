package com.alavande.marineassistant;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.mingle.widget.LoadingView;

import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    private ImageButton emergencyBtn, weatherBtn;
    private Button plannerBtn;
    private Intent intent;
    private VideoView videoView;
    private ImageView logo;

    private AlertDialog build;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dismissStatusBar();

        intent = new Intent();

        // initial fields, set on click events for buttons
        logo = (ImageView) findViewById(R.id.app_logo);
        logo.setOnClickListener(this);

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

//        setupWindowAnimations();

        // check if this is first time open enter in application
        MyDatabaseHelper helper = new MyDatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from first_load;", null);
        cursor.moveToFirst();
        do {
            String view = cursor.getString(cursor.getColumnIndex("view"));
            Log.i("enter", view);
            if (view.equals("main")) {
                int num = cursor.getInt(cursor.getColumnIndex("num"));
                Log.i("num", num+"");
                // if first time, display instruction
                if (num == 0) {
                    runOverlay();
                    ContentValues cv = new ContentValues();
                    cv.put("num", 1);
                    db.update("first_load", cv, "view = ?", new String[]{"main"});
                }
                break;
            }
        } while (cursor.moveToNext());
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
        // on click events for each button
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
                final Toast toast2 = Toast.makeText(this, "Loading....", Toast.LENGTH_SHORT);
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {

                        toast2.show();
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        intent.setClass(MainActivity.this, WeatherActivity.class);
                        startActivity(intent);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        toast2.cancel();
                        super.onPostExecute(aVoid);
                    }
                }.execute();

//                Toast.makeText(this, "In progress....", Toast.LENGTH_SHORT).show();
                break;
            case R.id.planner_btn:

                intent.setClass(this, RecorderChoseActivity.class);
                startActivity(intent);

//                Toast.makeText(this, "In progress....", Toast.LENGTH_SHORT).show();
                break;
            case R.id.app_logo:
                intent.setClass(this, AboutUsActivity.class);
                startActivity(intent);
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

    // instruction function
    private void runOverlay(){

        // instruction animation
        Animation mEnterAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimation.setDuration(600);
        mEnterAnimation.setFillAfter(true);

        Animation mExitAnimation = new AlphaAnimation(1f, 0f);
        mExitAnimation.setDuration(600);
        mExitAnimation.setFillAfter(true);

        // first instruction
        ChainTourGuide tourGuide1 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Recorder Button")
                        .setDescription("Record your notes and location events...")
                        .setGravity(Gravity.TOP)
                )
                // note that there is no Overlay here, so the default one will be used
                .playLater(plannerBtn);

        ChainTourGuide tourGuide2 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Map Button")
                        .setDescription("Search location, marina and lots of things in map page...")
                        .setGravity(Gravity.TOP)
                        .setBackgroundColor(Color.parseColor("#c0392b"))
                )
                .playLater(emergencyBtn);

        ChainTourGuide tourGuide3 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Weather Button")
                        .setDescription("Get marine weather information....")
                        .setGravity(Gravity.TOP)
                )
                // note that there is no Overlay here, so the default one will be used
                .playLater(weatherBtn);

        ChainTourGuide tourGuide4 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("App Logo")
                        .setDescription("Click to view about us page and instruction page...")
                        .setGravity(Gravity.BOTTOM)
                )
                // note that there is no Overlay here, so the default one will be used
                .playLater(logo);

        // use sequence for controlling instruction order
        Sequence sequence = new Sequence.SequenceBuilder()
                .add(tourGuide2, tourGuide3, tourGuide1, tourGuide4)
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
