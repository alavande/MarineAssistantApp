package com.alavande.marineassistant;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/03/29.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<NoteEntity> notes = new ArrayList<NoteEntity>();

    public NoteAdapter(List<NoteEntity> notes) {
        this.notes = notes;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        holder.titleText.setText(notes.get(position).getTitle());
        holder.contentText.setText(notes.get(position).getContent());
        holder.timeText.setText(notes.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, contentText, timeText;

        public NoteViewHolder(View itemView) {
            super(itemView);

            titleText = (TextView) itemView.findViewById(R.id.note_title);
            contentText = (TextView) itemView.findViewById(R.id.note_body);
            timeText = (TextView) itemView.findViewById(R.id.note_time);
        }
    }
}
