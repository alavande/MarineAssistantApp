package com.alavande.marineassistant;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.itheima.dialogviewpager.DepthPageTransformer;
import com.itheima.dialogviewpager.ItHeiMaDialog;

/**
 * A simple {@link Fragment} subclass.
 */

public class InstructionFragment extends Fragment implements View.OnClickListener {

    private ImageView first, second, third, fourth;

    public InstructionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_instruction, container, false);

        initData();

        first = (ImageView) view.findViewById(R.id.first_instruction_view);
        second = (ImageView) view.findViewById(R.id.second_instruction_view);
        third = (ImageView) view.findViewById(R.id.third_instruction_view);
        fourth = (ImageView) view.findViewById(R.id.fourth_instruction_view);

        first.setOnClickListener(this);
        second.setOnClickListener(this);
        third.setOnClickListener(this);
        fourth.setOnClickListener(this);

        return view;
    }

    private void initData(){
    }

    // create popup window for first instruction
    private void popupFirst(){

        FragmentManager fm = getActivity().getFragmentManager();

        ItHeiMaDialog.getInstance()
                .setImages(new int[]{R.drawable.ins1, R.drawable.ins2, R.drawable.ins3, R.drawable.ins4, R.drawable.ins5, R.drawable.ins6, R.drawable.ins7, R.drawable.ins8})
                .setPageTransformer(new DepthPageTransformer()).show(fm);
    }

    private void popupThird(){

        FragmentManager fm = getActivity().getFragmentManager();

        ItHeiMaDialog.getInstance()
                .setImages(new int[]{R.drawable.ins14, R.drawable.ins15, R.drawable.ins16, R.drawable.ins17, R.drawable.ins18})
                .setPageTransformer(new DepthPageTransformer()).show(fm);
    }

    private void popupSecond(){

        FragmentManager fm = getActivity().getFragmentManager();

        ItHeiMaDialog.getInstance()
                .setImages(new int[]{R.drawable.ins9, R.drawable.ins10, R.drawable.ins11, R.drawable.ins12, R.drawable.ins13})
                .setPageTransformer(new DepthPageTransformer()).show(fm);
    }

    private void popupFourth(){

        FragmentManager fm = getActivity().getFragmentManager();

        ItHeiMaDialog.getInstance()
                .setImages(new int[]{R.drawable.ins19, R.drawable.ins20, R.drawable.ins21})
                .setPageTransformer(new DepthPageTransformer()).show(fm);
    }

    @Override
    public void onClick(View view) {
        // click event to show different instruction window for different button
        switch (view.getId()) {
            case R.id.first_instruction_view:
                popupFirst();
                break;
            case R.id.second_instruction_view:
                popupSecond();
                break;
            case R.id.third_instruction_view:
                popupThird();
                break;
            case R.id.fourth_instruction_view:
                popupFourth();
                break;
            default:
                break;
        }
    }
}
