package com.geckosolutions.recordrack.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.geckosolutions.recordrack.fragments.MainActivityFragments;

/**
 * Created by anthony1 on 5/20/17.
 */

public class MainActivityViewPagerAdapter extends FragmentPagerAdapter
{

    private MainActivityFragments [] fragments;
    public MainActivityViewPagerAdapter(FragmentManager fm)
    {
        super(fm);
        fragments = new MainActivityFragments[getCount()];
    }

    @Override
    public Fragment getItem(int position)
    {
        MainActivityFragments fragment = new MainActivityFragments();
        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        fragment.setArguments(bundle);
        fragments[position]=fragment;
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    /**
     * method called to get a reference to the fragment at a given position
     * @param pos the position for which to retrieve a fragment
     * @return a MainActivityFragments reference
     */
    public MainActivityFragments getFragmentAt(int pos)
    {
        return fragments[pos];
    }
}
