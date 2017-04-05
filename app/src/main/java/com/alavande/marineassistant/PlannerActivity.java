package com.alavande.marineassistant;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PlannerActivity extends AppCompatActivity implements View.OnClickListener {

    private com.getbase.floatingactionbutton.FloatingActionButton addNoteBtn;
    private RecyclerView noteView;
    private List<NoteEntity> notes;
    private Context context;
    private MyDatabaseHelper helper;
    private SQLiteDatabase db;
    private NoteAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    public static PlannerActivity instance = null;

    private final String FIRST_INSERT_NOTE = "insert into note values " +
            "('Welcome to note','Marine assistance contains a built-in note " +
            "which helps user record everything they feel important or save their " +
            "feeling of anything.', '29/03/2017 11:00');";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        instance = this;

        dismissStatusBar();

        context = this;

        addNoteBtn = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.floating_btn);
        noteView = (RecyclerView) findViewById(R.id.note_view);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        setRecycleView();
        addNoteBtn.setOnClickListener(this);
    }

    public List<NoteEntity> retriveNoteFromDatabase() {
        helper = new MyDatabaseHelper(context);
        db = helper.getReadableDatabase();

        List<NoteEntity> noteEntities = new ArrayList<NoteEntity>();

        Cursor cursor = db.rawQuery("select * from note", null);

        cursor.moveToFirst();

        do {
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String time = cursor.getString(cursor.getColumnIndex("time"));

            NoteEntity noteEntity = new NoteEntity(time, title, content);
            noteEntities.add(noteEntity);

        } while (cursor.moveToNext());

        db.close();
        helper.close();

        return noteEntities;
    }

    public void initNoteInDatabase(){

        helper = new MyDatabaseHelper(context);
        db = helper.getReadableDatabase();
        try {
            db.beginTransaction();
            Cursor cursor = db.rawQuery("Select * from note", null);
            if (cursor.getCount() == 0) {

                db.execSQL(FIRST_INSERT_NOTE);
                db.setTransactionSuccessful();
//                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Fail to insert record to table", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }

        db.close();
        helper.close();

//        if (cursor.getCount() > 0) {
//            Toast.makeText(this, "Read data from database: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        notes.clear();
//        notes = retriveNoteFromDatabase();
//        adapter.notifyDataSetChanged();
//        setRecycleView();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        notes.clear();
//        notes = retriveNoteFromDatabase();
//        adapter.notifyDataSetChanged();
//        setRecycleView();
    }

    @Override
    public void onClick(View view) {
//        Toast.makeText(this, "Floating Button Clicked", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.setClass(this, AddNoteActivity.class);
        Bundle data = new Bundle();
        data.putString("key", "add");

        intent.putExtra("data", data);
        startActivity(intent);
        finish();
    }

    private void setRecycleView(){
        noteView.setLayoutManager(layoutManager);
        noteView.setItemAnimator(new DefaultItemAnimator());

        initNoteInDatabase();

        notes = retriveNoteFromDatabase();
        adapter = new NoteAdapter(notes);
        noteView.setAdapter(adapter);
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
