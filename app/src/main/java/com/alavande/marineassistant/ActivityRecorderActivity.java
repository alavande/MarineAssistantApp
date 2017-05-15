package com.alavande.marineassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

// trip activity
public class ActivityRecorderActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Recorder> recorders = new ArrayList<Recorder>();
    private RecyclerView recyclerView;
    private FloatingActionButton addRecorder;
    private ActivityRecorderAdapter adapter;
    private RecyclerView.LayoutManager manager;
    private Context context;
    private MyDatabaseHelper helper;
    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        dismissStatusBar();

        context = this;

        // retrieve all trip recorded in database
        getRecorder();

        // initial all components
        addRecorder = (FloatingActionButton) findViewById(R.id.add_recorder);
        addRecorder.setOnClickListener(this);

        // recycle view for displaying each items retrieved
        recyclerView = (RecyclerView) findViewById(R.id.recorder_recycler_view);
        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ActivityRecorderAdapter(recorders, this, this);
        recyclerView.setAdapter(adapter);

    }

    private void getRecorder(){

        // create helper and db for database actions
        helper = new MyDatabaseHelper(this);
        db = helper.getReadableDatabase();
        // get data for cursor
        cursor = db.rawQuery("select * from recorder;", null);
        cursor.moveToFirst();
        // read data stored in cursor
        if (cursor.getCount() > 0) {
            do {
                Recorder recorder = new Recorder(cursor.getString(cursor.getColumnIndex("recorder_name")));
                recorders.add(recorder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        helper.close();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_recorder:

                // view used in alert dialog body
                final EditText inputRecorder = new EditText(this);
                // create alert dialog through builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog dialog = builder.setTitle("New Trip")
                        .setView(inputRecorder)
                        // set text and click event for positive button
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // check if user input something in edit text
                                if (inputRecorder.getText().length() == 0) {
                                    Toast.makeText(context, "Please input a recorder name.", Toast.LENGTH_SHORT).show();
                                } else {
                                    helper = new MyDatabaseHelper(context);
                                    db = helper.getReadableDatabase();
                                    // check if the trip name exist in database
                                    cursor = db.rawQuery("select * from recorder where recorder_name = '"+ inputRecorder.getText() + "';",null);
                                    // if not exist, insert new record to database
                                    if (cursor.getCount() == 0) {
                                        db.beginTransaction();
                                        db.execSQL("insert into recorder values('"+ inputRecorder.getText() +"');");
                                        db.setTransactionSuccessful();
                                        db.endTransaction();
                                        recorders.add(new Recorder(inputRecorder.getText().toString()));
                                        Intent intent = new Intent();
                                        intent.setClass(context, ActivityRecorderActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(context, "Recorder name already exist.", Toast.LENGTH_SHORT).show();
                                    }
                                    cursor.close();
                                    db.close();
                                    helper.close();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null).create();
                dialog.show();
                break;
            default:
                break;
        }
    }

    private void runOverlay(){
        Animation mEnterAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimation.setDuration(600);
        mEnterAnimation.setFillAfter(true);

        Animation mExitAnimation = new AlphaAnimation(1f, 0f);
        mExitAnimation.setDuration(600);
        mExitAnimation.setFillAfter(true);

        ToolTip toolTip = new ToolTip()
                .setTitle("Add New Trip")
                .setDescription("Add your new trip which contains all events in this trip, trip name must be unique...")
                .setTextColor(Color.parseColor("#bdc3c7"))
                .setBackgroundColor(Color.parseColor("#e74c3c"))
                .setShadow(true)
                .setGravity(Gravity.TOP)
                .setEnterAnimation(mEnterAnimation);

        TourGuide.init(this).with(TourGuide.Technique.Click)
                .setToolTip(toolTip)
                .setOverlay(new Overlay()
                        .setExitAnimation(mExitAnimation).disableClick(true))
                .playOn(addRecorder);

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
