package com.alavande.marineassistant;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;

public class RecorderChoseActivity extends AppCompatActivity implements View.OnClickListener{

    private CardView noteCard, recorderCard;
    private Animation mEnterAnimation, mExitAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder_chose);

        dismissStatusBar();

        noteCard = (CardView) findViewById(R.id.note_card);
        noteCard.setOnClickListener(this);
        recorderCard = (CardView) findViewById(R.id.recorder_card);
        recorderCard.setOnClickListener(this);

        mEnterAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimation.setDuration(600);
        mEnterAnimation.setFillAfter(true);

        mExitAnimation = new AlphaAnimation(1f, 0f);
        mExitAnimation.setDuration(600);
        mExitAnimation.setFillAfter(true);

        MyDatabaseHelper helper = new MyDatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from first_load;", null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String view = cursor.getString(cursor.getColumnIndex("view"));
            if (view.equals("recorder_choose")) {
                int num = cursor.getInt(cursor.getColumnIndex("num"));
                if (num == 0) {
                    runOverlay();

                    ContentValues cv = new ContentValues();
                    cv.put("num", 1);
                    db.update("first_load", cv, "view = ?", new String[]{"recorder_choose"});
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.note_card:
                intent.setClass(this, PlannerActivity.class);
                startActivity(intent);
                break;
            case R.id.recorder_card:
                intent.setClass(this, ActivityRecorderActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void runOverlay(){
        ChainTourGuide tourGuide1 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Note View")
                        .setDescription("Add notes for recording your daily life.. \n \n (Click outside for continue...)")
                        .setGravity(Gravity.BOTTOM)
                )
                // note that there is no Overlay here, so the default one will be used
                .playLater(noteCard);

        ChainTourGuide tourGuide2 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Recorder View")
                        .setDescription("Record your trips and events in each location...")
                        .setGravity(Gravity.TOP)
                        .setBackgroundColor(Color.parseColor("#c0392b"))
                )
                .setOverlay(new Overlay()
                        .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation)
                )
                .playLater(recorderCard);

        Sequence sequence = new Sequence.SequenceBuilder()
                .add(tourGuide1, tourGuide2)
                .setDefaultOverlay(new Overlay()
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation)
                )
                .setDefaultPointer(null)
                .setContinueMethod(Sequence.ContinueMethod.Overlay)
                .build();

        ChainTourGuide.init(this).playInSequence(sequence);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
