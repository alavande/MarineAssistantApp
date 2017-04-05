package com.alavande.marineassistant;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

        doneNoteBtn = (FloatingActionButton) findViewById(R.id.done_note_btn);
        doneNoteBtn.setOnClickListener(this);

        titleText = (EditText) findViewById(R.id.note_title);
        contentText = (EditText) findViewById(R.id.note_content);

        data = getIntent().getBundleExtra("data");
        mode = data.getString("key");
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
//                Toast.makeText(this, "Note add to database successfully.", Toast.LENGTH_SHORT).show();
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
//            Toast.makeText(this, insert, Toast.LENGTH_SHORT).show();
//            Log.i("Insert", insert);
            db.execSQL(action);
//            db.rawQuery(insert, null);
            db.setTransactionSuccessful();
//            Toast.makeText(this, "Insert or Update success", Toast.LENGTH_SHORT).show();
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

}
