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

    private void popupFirst(){

        FragmentManager fm = getActivity().getFragmentManager();

        ItHeiMaDialog.getInstance()
                .setImages(new int[]{com.itheima.dialogviewpager.R.drawable.new_user_guide_1, com.itheima.dialogviewpager.R.drawable.new_user_guide_2, com.itheima.dialogviewpager.R.drawable.new_user_guide_3, com.itheima.dialogviewpager.R.drawable.new_user_guide_4})
                .setPageTransformer(new DepthPageTransformer()).show(fm);
    }

    private void popupSecond(){

        FragmentManager fm = getActivity().getFragmentManager();

        ItHeiMaDialog.getInstance()
                .setImages(new int[]{com.itheima.dialogviewpager.R.drawable.new_user_guide_1, com.itheima.dialogviewpager.R.drawable.new_user_guide_2, com.itheima.dialogviewpager.R.drawable.new_user_guide_3, com.itheima.dialogviewpager.R.drawable.new_user_guide_4})
                .setPageTransformer(new DepthPageTransformer()).show(fm);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.first_instruction_view:
                popupFirst();
                break;
            case R.id.second_instruction_view:
                popupSecond();
                break;
            case R.id.third_instruction_view:
                popupFirst();
                break;
            case R.id.fourth_instruction_view:
                popupSecond();
                break;
            default:
                break;
        }
    }
}
