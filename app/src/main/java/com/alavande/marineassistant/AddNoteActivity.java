package com.alavande.marineassistant;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;

/**
 * Created by hasee on 2017/04/02.
 */

public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton doneNoteBtn;
    private EditText titleText, contentText;
    private MyDatabaseHelper helper;
    private SQLiteDatabase db;
    private String mode;
    private Bundle data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note_layout);

        dismissStatusBar();

        // initial components
        doneNoteBtn = (FloatingActionButton) findViewById(R.id.done_note_btn);
        doneNoteBtn.setOnClickListener(this);

        titleText = (EditText) findViewById(R.id.note_title);
        contentText = (EditText) findViewById(R.id.note_content);

        // get trip name for future insert
        data = getIntent().getBundleExtra("data");
        mode = data.getString("key");
        // check if action mode for note page
        if (mode.equals("edit")){
            titleText.setText(data.getString("title"));
            contentText.setText(data.getString("content"));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.done_note_btn:
                addOrUpdata();
                break;
            default:
                break;
        }
    }

    private void addOrUpdata(){
        helper = new MyDatabaseHelper(this);
        db = helper.getReadableDatabase();

        String title = titleText.getText().toString();
        if (title.length() == 0 || title == null) {
            title = "No title";
        }
        String content = contentText.getText().toString();
        if (content.length() == 0 || content == null) {
            content = "";
        }

        // get current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String time = sdf.format(System.currentTimeMillis());
        try {
            String action = null;
            if (mode.equals("edit")) {
                String oldTitle = data.getString("title");
                String oldContent = data.getString("content");
                action = "update note set title = '" + title + "', content = '" + content +
                        "', time = '" + time + "' where title = '" + oldTitle + "' and content = '" + oldContent + "';";
            } else {
                action = "insert into note values('" + title + "', '" + content + "', '" + time + "');";
            }

            db.beginTransaction();
            db.execSQL(action);
            db.setTransactionSuccessful();
            jumpBackToPlanner();
        } catch (Exception e) {
            Toast.makeText(this, "Insert or Update failed", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onBackPressed() {
        jumpBackToPlanner();
    }

    private void jumpBackToPlanner(){
        Intent intent = new Intent();
        intent.setClass(this, PlannerActivity.class);
        startActivity(intent);
        finish();
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
