package com.geckosolutions.recordrack.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.PurchaseViewPagerAdapter;
import com.geckosolutions.recordrack.adapters.SalesViewPagerAdapter;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 7/10/17.
 */

public class PurchaseViewPager extends Fragment
{
    private LinearLayout primaryView;
    private ViewPager pager;
    private WeakReference<ViewPager> viewPagerWeakReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        primaryView = (LinearLayout)inflater.inflate(R.layout.purchase_view_pager,group,false);
        init();
        viewPagerWeakReference = new WeakReference<ViewPager>(pager);
        return primaryView;
    }

    private void init()
    {
        pager = (ViewPager) primaryView.findViewById(R.id.view_pager);
        pager.setAdapter(new PurchaseViewPagerAdapter(getFragmentManager()));

        //set background color when Purchase info is clicked
        primaryView.findViewById(R.id.purchases).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                primaryView.findViewById(R.id.new_purchase).setBackgroundColor(getResources().getColor(R.color.orange1));
                pager.setCurrentItem(0);
            }
        });

        //set background color when New purchase is clicked
        primaryView.findViewById(R.id.new_purchase).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                primaryView.findViewById(R.id.purchases).setBackgroundColor(getResources().getColor(R.color.orange1));
                pager.setCurrentItem(1);
            }
        });

        //listen for page swipes
        pager = (ViewPager)primaryView.findViewById(R.id.view_pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                if(position == 0)
                {
                    primaryView.findViewById(R.id.purchases).setBackgroundColor(getResources().getColor(R.color.orange));
                    primaryView.findViewById(R.id.new_purchase).setBackgroundColor(getResources().getColor(R.color.orange1));
                }
                else if(position == 1)
                {
                    //Fragment fragment = ((SalesViewPagerAdapter)pager.getAdapter()).getItem(1);
                    //((SalesSearchFragment)fragment).refresh();
                    primaryView.findViewById(R.id.new_purchase).setBackgroundColor(getResources().getColor(R.color.orange));
                    primaryView.findViewById(R.id.purchases).setBackgroundColor(getResources().getColor(R.color.orange1));

                    //fragment = null;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }
}
