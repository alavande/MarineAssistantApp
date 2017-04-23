package com.alavande.marineassistant;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by hasee on 2017/04/14.
 */

public class FishCheckAdapter extends RecyclerView.Adapter<FishCheckAdapter.ViewHolder> {

    List<FishEntity> fishCheckList = new ArrayList<FishEntity>();

    public FishCheckAdapter(List<FishEntity> fishCheckList) {
        this.fishCheckList = fishCheckList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fish_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.fishImage.setImageResource(fishCheckList.get(position).getImageResource());
        holder.fishNameAndDescripiton.setText(fishCheckList.get(position).getFishName());

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fishName = fishCheckList.get(position).getFishName();
                Toast.makeText(view.getContext(), "Wanna get " + fishName + "? Good luck.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return fishCheckList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        ImageView fishImage;
        TextView fishNameAndDescripiton;

        public ViewHolder(View itemView) {
            super(itemView);

            v = itemView;
            fishImage = (ImageView) v.findViewById(R.id.fish_card_image);
            fishNameAndDescripiton = (TextView) v.findViewById(R.id.fish_card_name);
        }
    }
}
