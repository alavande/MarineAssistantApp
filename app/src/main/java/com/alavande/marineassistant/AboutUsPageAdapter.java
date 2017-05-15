package com.alavande.marineassistant;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by hasee on 2017/04/13.
 */

// adapter for controlling page change in about us activity
public class AboutUsPageAdapter extends FragmentPagerAdapter {

    // number of pages in activity
    private static final int PAGE_COUNT = 2;
    private Context context;

    public AboutUsPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        // return fragment for displaying
        switch (position) {
            case 0:
                Fragment aboutUsFragment = new AboutUsFragment();
                return aboutUsFragment;
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
        // return page title for each fragment
        switch (position) {
            case 0:
                return "About Us";
            case 1:
                return "Instruction";
            default:
                return "None";
        }
    }
}
