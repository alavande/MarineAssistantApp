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

        notes = getNotesFromDatabase();

        NoteAdapter adapter = new NoteAdapter(notes);

        noteView.setAdapter(adapter);
        addNoteBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "Floating Button Clicked", Toast.LENGTH_SHORT).show();
    }

    public List<NoteEntity> getNotesFromDatabase() {

        MyDatabaseHelper helper = new MyDatabaseHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        final List<NoteEntity> noteList = new ArrayList<NoteEntity>();

        Cursor cursor = db.rawQuery("select * from note", null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {

                String time = cursor.getString(cursor.getColumnIndex("time"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String content = cursor.getString(cursor.getColumnIndex("content"));

                NoteEntity note = new NoteEntity(time, title, content);
                noteList.add(note);
            }
        }

        if (noteList.size() == 0) {
            NoteEntity note = new NoteEntity("2017-03-29 11:00", "Welcome to note",
                    "Marine assistance contains a built-in note which helps user record everything they feel important or save their feeling of anything.");
            noteList.add(note);
        }

        db.close();
        helper.close();
        return noteList;
    }
}
