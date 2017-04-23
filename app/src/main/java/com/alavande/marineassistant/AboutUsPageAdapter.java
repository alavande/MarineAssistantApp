package com.alavande.marineassistant;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by hasee on 2017/04/13.
 */

public class AboutUsPageAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 2;
    private Context context;

    public AboutUsPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Fragment aboutUsFragment = new AboutUsFragment();
                return aboutUsFragment;
//            case 1:
//                Fragment contactUsFragment = new ContactUsFragment();
//                return contactUsFragment;
            case 1:
                Fragment instructionFragment = new InstructionFragment();
                return instructionFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "About Us";
//            case 1:
//                return "Contact Us";
            case 1:
                return "Instruction";
            default:
                return "None";
        }
    }
}
