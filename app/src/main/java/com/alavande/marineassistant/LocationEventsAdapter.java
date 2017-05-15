package com.alavande.marineassistant;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/04/28.
 */

// adapter for location events
public class LocationEventsAdapter extends RecyclerView.Adapter<LocationEventsAdapter.LocationEventsViewHolder> {

    List<LocationActivity> locationActivities = new ArrayList<LocationActivity>();
    String parent;

    public LocationEventsAdapter(List<LocationActivity> locationActivities, String parent) {
        this.locationActivities = locationActivities;
        this.parent = parent;
    }

    @Override
    public LocationEventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_events_layout, parent, false);

        return new LocationEventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocationEventsViewHolder holder, int position) {
        holder.eventName.setText(locationActivities.get(position).getEventName());
        holder.eventTime.setText(locationActivities.get(position).getTime());
        holder.parentTrip.setText("in " + parent);
    }

    @Override
    public int getItemCount() {
        return locationActivities.size();
    }

    class LocationEventsViewHolder extends  RecyclerView.ViewHolder {
        View v;
        TextView eventName, eventTime, parentTrip;
        public LocationEventsViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            eventName = (TextView) itemView.findViewById(R.id.event_name);
            eventTime = (TextView) itemView.findViewById(R.id.event_time);
            parentTrip = (TextView) itemView.findViewById(R.id.parent_trip);
        }
    }
}
