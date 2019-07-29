package com.geckosolutions.recordrack.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.SalesViewPagerAdapter;

import java.lang.ref.WeakReference;

/**
 * this class inflates the sales_view_pager layout. It is the layout that is displayed when Sales option
 * is selected from the drawer. The layout would contain the NewSalesFragment and
 * SalesSearchFragment
 * Created by anthony1 on 6/12/16.
 */
public class SalesViewPager extends Fragment
{
    private ViewPager pager;
    private LinearLayout primaryView;
    private static WeakReference<ViewPager> viewPagerWeakReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        primaryView = (LinearLayout)inflater.inflate(R.layout.sales_view_pager,group,false);
        init();
        viewPagerWeakReference = new WeakReference<ViewPager>(pager);
        return primaryView;
    }

    private void init()
    {
        //set background color when Sales info is clicked
        primaryView.findViewById(R.id.sales_info).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                primaryView.findViewById(R.id.new_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                primaryView.findViewById(R.id.search_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                pager.setCurrentItem(0);
            }
        });

        //set background color when Search sales is clicked
        primaryView.findViewById(R.id.search_sales).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                primaryView.findViewById(R.id.new_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                primaryView.findViewById(R.id.sales_info).setBackgroundColor(getResources().getColor(R.color.orange1));
                pager.setCurrentItem(1);
            }
        });

        //set background color when New sales is clicked
        primaryView.findViewById(R.id.new_sales).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                primaryView.findViewById(R.id.search_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                primaryView.findViewById(R.id.sales_info).setBackgroundColor(getResources().getColor(R.color.orange1));
                pager.setCurrentItem(2);
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
                    primaryView.findViewById(R.id.sales_info).setBackgroundColor(getResources().getColor(R.color.orange));
                    primaryView.findViewById(R.id.search_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                    primaryView.findViewById(R.id.new_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                }
                else if(position == 1)
                {
                    Fragment fragment = ((SalesViewPagerAdapter)pager.getAdapter()).getItem(1);
                    ((SalesSearchFragment)fragment).refresh();
                    primaryView.findViewById(R.id.search_sales).setBackgroundColor(getResources().getColor(R.color.orange));
                    primaryView.findViewById(R.id.new_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                    primaryView.findViewById(R.id.sales_info).setBackgroundColor(getResources().getColor(R.color.orange1));

                    fragment = null;
                }
                else if(position == 2)
                {
                    primaryView.findViewById(R.id.search_sales).setBackgroundColor(getResources().getColor(R.color.orange1));
                    primaryView.findViewById(R.id.new_sales).setBackgroundColor(getResources().getColor(R.color.orange));
                    primaryView.findViewById(R.id.sales_info).setBackgroundColor(getResources().getColor(R.color.orange1));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
        pager.setAdapter(new SalesViewPagerAdapter(getFragmentManager()));
    }

    /**
     * method to get a reference to this view pager
     * @return a weak reference to the view pager
     */
    public static WeakReference<ViewPager> getViewPager()
    {
        return viewPagerWeakReference;
    }
}
