package com.geckosolutions.recordrack.custom;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;


/**
 * Created by anthony1 on 9/2/17.
 */

public class CustomSpinner extends AppCompatSpinner
{
    private OnItemSelectedListener listener;
    private int pos;
    public CustomSpinner(Context context) {
        super(context);
    }
    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void setSelection(int position)
    {
        super.setSelection(position);
        if(listener !=null)
            listener.onItemSelected(null,null,position,0);
        pos = position;
    }

    public void setListener(OnItemSelectedListener listener)
    {
        this.listener = listener;
    }

    public int getSelectedPosition()
    {
        return pos;
    }
}
