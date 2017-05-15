package com.alavande.marineassistant;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by hasee on 2017/04/28.
 */

// adapter for recycle view used in activity recorder
public class ActivityRecorderAdapter extends RecyclerView.Adapter<ActivityRecorderAdapter.RecorderViewHolder>{

    List<Recorder> recorderList = new ArrayList<Recorder>();
    Context context;
    Activity activity;

    public ActivityRecorderAdapter(List<Recorder> recorderList, Context context, Activity activity) {
        this.recorderList = recorderList;
        this.context = context;
        this.activity = activity;
    }


    @Override
    public RecorderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // set view for each item displayed in recycle view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recorder_item_layout, parent, false);
        return new RecorderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecorderViewHolder holder, final int position) {

        holder.activityName.setText(recorderList.get(position).getActivityName());

        // click event for each item
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // jump to event activity
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("recorder", holder.activityName.getText().toString());
                Log.i("recorder", holder.activityName.getText().toString());
                intent.putExtras(bundle);
                intent.setClass(context, LocationEventsActivity.class);
                context.startActivity(intent);
            }
        });

        // delete click event for delete image in each record
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                AlertDialog dialog = builder.setTitle("Delete Confirm")
                        .setMessage("Do you really want to delete this record?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // retrieve all records in event table, delete events related to this trip
                                MyDatabaseHelper helper = new MyDatabaseHelper(context);
                                SQLiteDatabase db = helper.getWritableDatabase();
                                db.beginTransaction();
                                db.delete("recorder", "recorder_name = ?", new String[]{recorderList.get(position).getActivityName()});
                                db.delete("location_event", "recorder = ?", new String[]{recorderList.get(position).getActivityName()});
                                db.setTransactionSuccessful();
                                db.endTransaction();
                                // refresh activity
                                Intent intent = new Intent();
                                intent.setClass(context, ActivityRecorderActivity.class);
                                context.startActivity(intent);
                                activity.finish();
                            }
                        })
                        .setNegativeButton("Cancel", null).create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recorderList.size();
    }

    class RecorderViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView activityName;
        ImageView deleteBtn;

        public RecorderViewHolder(View itemView) {
            super(itemView);
            // initial components in each record item
            v = itemView;
            activityName = (TextView) itemView.findViewById(R.id.recorder_item_text);
            deleteBtn = (ImageView) itemView.findViewById(R.id.delete_record);
        }
    }

}
