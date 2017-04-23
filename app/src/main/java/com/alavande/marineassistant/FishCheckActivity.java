package com.alavande.marineassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class FishCheckActivity extends AppCompatActivity {

    private RecyclerView fishView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_check);

        fishView = (RecyclerView) findViewById(R.id.fish_check_list);
//        FishCheckAdapter adapter = new FishCheckAdapter()
//        fishView.setAdapter();


    }
}
