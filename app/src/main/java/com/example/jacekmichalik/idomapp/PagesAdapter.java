package com.example.jacekmichalik.idomapp;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.method.HideReturnsTransformationMethod;

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

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case PAGE_HISTORY:
                return "Historia";

            case PAGE_MACROS:
                return "Makra";

            default:
                return "????";
        }
    }
}
