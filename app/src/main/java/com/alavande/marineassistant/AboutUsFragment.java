package com.alavande.marineassistant;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutUsFragment extends Fragment {


    private CardView aboutUsCard;
    private TextView aboutUsText;

    public AboutUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_about_us, container, false);
        aboutUsCard = (CardView) view.findViewById(R.id.about_us_card);

        ExpandableTextView expTv1 = (ExpandableTextView) view.findViewById(R.id.expand_text_view);
        expTv1.setText(getString(R.string.dummy_text1));

        ExpandableTextView expTv2 = (ExpandableTextView) view.findViewById(R.id.expand_text_view1);
        expTv2.setText(getString(R.string.dummy_text2));

        return view;
    }

}
