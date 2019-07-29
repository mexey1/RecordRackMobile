package com.geckosolutions.recordrack.adapters;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.fragments.NewSalesFragment;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 9/13/16.
 */
public class SaleViewAdapter extends NewSalesListAdapter
{

    private boolean options = true;
    public SaleViewAdapter()
    {
        super();
    }

    public SaleViewAdapter(boolean shouldDisplayOptions)
    {
        new SaleViewAdapter();
        options = shouldDisplayOptions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getView(position,convertView,parent);
        //if options(edit/delete) are to be hidden
        if(!options)
        {
            view.findViewById(R.id.edit).setVisibility(View.GONE);
            view.findViewById(R.id.delete).setVisibility(View.GONE);
            //this is needed to prevent the scrollview from consuming the scroll event
            ((HorizontalScrollView)view.findViewById(R.id.horizontal_scroll)).setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    return true;
                }
            });
        }
        view.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView(parent.getWidth()));
        return view;
    }

    public double getSum()
    {
        return sum;
    }
}
