package com.alavande.marineassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hasee on 2017/03/29.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<NoteEntity> notes = new ArrayList<NoteEntity>();
    MyDatabaseHelper helper;
    SQLiteDatabase db;

    public NoteAdapter(List<NoteEntity> notes) {
        this.notes = notes;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NoteViewHolder holder, final int position) {
        holder.titleText.setText(notes.get(position).getTitle());
        holder.contentText.setText(notes.get(position).getContent());
        holder.timeText.setText(notes.get(position).getTime());

//        Random r = new Random();
//        int i = r.nextInt(5) + 1;
//        Log.i("random", i + "");
//        holder.itemLayout.getBackground().setColorFilter(Color.parseColor("#1ABC9C"), PorterDuff.Mode.DARKEN);
//        holder.itemLayout.setBackgroundColor(0xffFA8072);


        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteEntity note = notes.get(position);
                helper = new MyDatabaseHelper(view.getContext());
                db = helper.getReadableDatabase();
                final String delete = "delete from note where title = '"+ note.getTitle() + "' and content = '" + note.getContent() +  "';";
                try {
                    db.beginTransaction();

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Delete Confirmation");
                    builder.setMessage("Do you really want to delete this note?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.execSQL(delete);
//                            db.setTransactionSuccessful();
                            notes.remove(position);
                            notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();

                } catch (Exception e) {
//                    Toast.makeText(view.getContext(), "delete failed.", Toast.LENGTH_SHORT).show();
                } finally {
                    db.endTransaction();
                }
            }
        });

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle data = new Bundle();
                data.putString("key", "edit");
                data.putString("title", holder.titleText.getText().toString());
                data.putString("content", holder.contentText.getText().toString());

                intent.putExtra("data", data);
                intent.setClass(view.getContext(), AddNoteActivity.class);
                view.getContext().startActivity(intent);
                PlannerActivity.instance.finish();
//                Toast.makeText(view.getContext(), "Text Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView titleText, contentText, timeText;
        ImageView deleteBtn;

        public NoteViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            titleText = (TextView) itemView.findViewById(R.id.note_title);
            contentText = (TextView) itemView.findViewById(R.id.note_body);
            timeText = (TextView) itemView.findViewById(R.id.note_time);
            deleteBtn = (ImageView) itemView.findViewById(R.id.item_delete_img);
        }
    }
}
