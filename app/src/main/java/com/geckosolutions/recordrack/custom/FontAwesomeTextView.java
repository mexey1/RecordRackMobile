package com.geckosolutions.recordrack.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.geckosolutions.recordrack.logic.UtilityClass;

/**
 * Created by anthony1 on 1/5/16.
 */
public class FontAwesomeTextView extends TextView
{
    public FontAwesomeTextView(Context context)
    {
        super(context);

        init();
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        if(!isInEditMode())
        {
            setTypeface(UtilityClass.getFontAwesomeTypeface());
        }
    }
}
