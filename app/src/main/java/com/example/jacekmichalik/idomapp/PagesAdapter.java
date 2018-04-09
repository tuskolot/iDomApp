package com.example.jacekmichalik.idomapp;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList;
import com.example.jacekmichalik.idomapp.FloorMapPackage.SecurItemFragment;

public class PagesAdapter extends FragmentPagerAdapter {

    final public static int PAGE_HISTORY = 0;
    final public static int PAGE_MACROS = 1;

    public PagesAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case PAGE_HISTORY:
                return new SysInfoFragment();

            case PAGE_MACROS:
                return new MacrosFragment();

            default: {
                int floor_idx = position -2;
                String floor_name = MainActivity.IDOM.getFloorName(floor_idx);
                return SecurItemFragment.instanceMe(floor_name);
            }
        }
    }


    @Override
    public int getCount() {
        int n = 2;
        if (MainActivity.IDOM != null)
            n = n + MainActivity.IDOM.floorArray.size();
        return n;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case PAGE_HISTORY:
                return "Historia";

            case PAGE_MACROS:
                return "Makra";

            default:
                return MainActivity.IDOM.floorArray.get(position - 2).toString();
        }
    }
}
