package com.alavande.marineassistant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageButton;
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

    private final String FIRST_INSERT_NOTE = "insert into note values " +
            "(1,'Welcome to note','Marine assistance contains a built-in note " +
            "which helps user record everything they feel important or save their " +
            "feeling of anything.', '2017-03-29 11:00')";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        context = this;

        addNoteBtn = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.floating_btn);
        noteView = (RecyclerView) findViewById(R.id.note_view);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        noteView.setLayoutManager(layoutManager);
        noteView.setItemAnimator(new DefaultItemAnimator());

        initNoteInDatabase();

        notes = retriveNoteFromDatabase();

        adapter = new NoteAdapter(notes);

        noteView.setAdapter(adapter);
        addNoteBtn.setOnClickListener(this);
    }

    public void addNoteToDatabase(){

        helper = new MyDatabaseHelper(context);
        db = helper.getReadableDatabase();



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
        Cursor cursor = db.rawQuery("Select * from note", null);
        try {
            if (cursor.getCount() == 0) {
                db.rawQuery(FIRST_INSERT_NOTE, null);
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Fail to insert record to table", Toast.LENGTH_SHORT).show();
        }

//        if (cursor.getCount() > 0) {
//            Toast.makeText(this, "Read data from database: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
//        }
    }


    @Override
    public void onClick(View view) {
        Toast.makeText(this, "Floating Button Clicked", Toast.LENGTH_SHORT).show();
    }
}
