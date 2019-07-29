package com.geckosolutions.recordrack.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.geckosolutions.recordrack.logic.UtilityClass;

/**
 * Created by anthony1 on 8/28/15.
 */
public class CustomTextView  extends TextView
{

    public CustomTextView(Context context)
    {
        super(context);

        init();
    }

    public CustomTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        if(!isInEditMode())
        {
            setTypeface(UtilityClass.getLatoRegularTypeface());
        }
    }
}
