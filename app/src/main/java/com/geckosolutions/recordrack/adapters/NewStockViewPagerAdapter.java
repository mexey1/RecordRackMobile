package com.geckosolutions.recordrack.adapters;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by anthony1 on 5/27/17.
 */

public class NewStockViewPagerAdapter extends PagerAdapter
{
    private ArrayList<ViewGroup> arrayList;
    private final String TAG="NewStockViewPagerAdapter";

    public NewStockViewPagerAdapter()
    {
        arrayList = new ArrayList<>();
    }

    //-----------------------------------------------------------------------------
    // Used by ViewPager.  "Object" represents the page; tell the ViewPager where the
    // page should be displayed, from left-to-right.  If the page no longer exists,
    // return POSITION_NONE.
    @Override
    public int getItemPosition(Object object)
    {
        if(arrayList.contains(object))
            return arrayList.indexOf(object);
        else
            return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int pos)
    {
        ViewGroup group = arrayList.get(pos);
        viewGroup.addView(group);
        return group;
    }



    @Override
    public int getCount()
    {
        return arrayList.size();
    }

    // Used by ViewPager.
    @Override
    public boolean isViewFromObject (View view, Object object)
    {
        return view == object;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object)
    {
        container.removeView (arrayList.get (position));
    }


    public int addView(ViewGroup viewGroup)
    {
        int pos = addView(viewGroup, arrayList.size());
        notifyDataSetChanged();
        return pos;
    }

    public int addView(ViewGroup viewGroup, int pos)
    {
        arrayList.add(pos,viewGroup);
        notifyDataSetChanged();
        return pos;
    }

    // Removes "view" from "views".
    // Retuns position of removed view.
    // The app should call this to remove pages; not used by ViewPager.
    public int removeView (ViewPager pager, View v)
    {
        return removeView (pager, arrayList.indexOf (v));
    }

    //-----------------------------------------------------------------------------
    // Removes the "view" at "position" from "views".
    // Retuns position of removed view.
    // The app should call this to remove pages; not used by ViewPager.
    public int removeView (ViewPager pager, int position)
    {
        // ViewPager doesn't have a delete method; the closest is to set the adapter
        // again.  When doing so, it deletes all its views.  Then we can delete the view
        // from from the adapter and finally set the adapter to the pager again.  Note
        // that we set the adapter to null before removing the view from "views" - that's
        // because while ViewPager deletes all its views, it will call destroyItem which
        // will in turn cause a null pointer ref.
        pager.setAdapter (null);
        arrayList.remove (position);
        pager.setAdapter (this);

        return position;
    }

    // Returns the "view" at "position".
    // The app should call this to retrieve a view; not used by ViewPager.
    public View getView (int position)
    {
        return arrayList.get (position);
    }
}
