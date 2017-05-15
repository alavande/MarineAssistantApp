package com.alavande.marineassistant;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


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
//        View view =  inflater.inflate(R.layout.fragment_about_us, container, false);

        // version element
        Element versionElement = new Element();
        versionElement.setTitle("Version 1.3.3");

        // telephone element used in about us page
        Element phoneElement = new Element();

        // click phone line to invoke system phone call event
        phoneElement.setTitle("Contact Phone").setIconDrawable(R.drawable.phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri dialUri = Uri.parse("tel:8888888");
                Intent intent = new Intent(Intent.ACTION_DIAL, dialUri);
                startActivity(intent);
            }
        });

        // custom about us page
        View aboutPage = new AboutPage(getContext())
                .isRTL(false)
                .setDescription("An app that can be used in seaside...")
                .setImage(R.drawable.app_logo2)
                .addItem(versionElement)
                .addGroup("Connect with us")
                .addEmail("xxxxxxx@gmail.com") // author's email
                .addItem(phoneElement) // author's phone number
                .addWebsite("http://bit.ly/TeamSummit-MarineAssistanceApp") // application website url
                .create();


//        aboutUsCard = (CardView) view.findViewById(R.id.about_us_card);
//
//        ExpandableTextView expTv1 = (ExpandableTextView) view.findViewById(R.id.expand_text_view);
//        expTv1.setText(getString(R.string.dummy_text1));
//
//        ExpandableTextView expTv2 = (ExpandableTextView) view.findViewById(R.id.expand_text_view1);
//        expTv2.setText(getString(R.string.dummy_text2));

        return aboutPage;
    }

}
