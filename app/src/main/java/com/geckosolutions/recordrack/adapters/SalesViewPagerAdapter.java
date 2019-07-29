package com.geckosolutions.recordrack.adapters;

import android.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.geckosolutions.recordrack.fragments.NewSalesFragment;
import com.geckosolutions.recordrack.fragments.SalesInfoFragment;
import com.geckosolutions.recordrack.fragments.SalesSearchFragment;

/**
 * Created by anthony1 on 6/12/16.
 */
public class SalesViewPagerAdapter extends FragmentPagerAdapter
{
    private NewSalesFragment newSalesFragment;
    private SalesSearchFragment salesSearchFragment;
    private SalesInfoFragment salesInfoFragment;

    public SalesViewPagerAdapter(android.support.v4.app.FragmentManager manager)
    {
        super(manager);
    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment = null;
        if(position == 0)
        {
            if(salesInfoFragment == null)
                salesInfoFragment = new SalesInfoFragment();
            fragment = salesInfoFragment;
        }
        else if(position == 1)
        {
            if(salesSearchFragment == null)
                salesSearchFragment = new SalesSearchFragment();

             fragment = salesSearchFragment;

        }
        else if(position == 2)
        {
            if(newSalesFragment == null)
                newSalesFragment = new NewSalesFragment();

            fragment =  newSalesFragment;
        }

        return fragment;
    }

    @Override
    public int getCount()
    {
        return 3;
    }
}
