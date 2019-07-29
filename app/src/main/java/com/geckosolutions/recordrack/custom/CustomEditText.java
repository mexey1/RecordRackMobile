package com.geckosolutions.recordrack.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.interfaces.CustomEdittextInterface;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

/**
 * Created by anthony1 on 8/28/15.
 */
public class CustomEditText extends  android.support.v7.widget.AppCompatEditText
{
    private EditText editText;
    private int count =0;
    private String previous = "";
    private boolean shouldRun = true;
    private CustomEdittextInterface edittextInterface;
    private final String TAG="CustomEditText";

    public CustomEditText(Context context)
    {
        super(context);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
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

    /**
     * this method if called, sets a text changed listener for currency textfields i.e it attempts
     * to prepend the currency sign to the beginning of the number being entered.
     */
    public void enableTextChangedListener()
    {
            editText = this;
            this.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {

                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    if(edittextInterface !=null)
                        edittextInterface.onTextChanged(s.toString());
                    if(!previous.equals(s.toString().trim()))
                        shouldRun = true;
                    else
                        shouldRun = false;
                    if(s.length() >= 1 && shouldRun)
                    {
                        if(s.length()==1 && count==1)
                        {
                            editText.setText("");
                            count = 0;
                            return;
                        }
                        Logger.log(TAG,"yo here "+s.toString());
                        Log.d(TAG,"yo here "+s.toString());
                        double withoutnaira = UtilityClass.removeNairaSignFromString(s.toString());
                        String withnaira = UtilityClass.formatMoney(withoutnaira);
                        previous = s.toString().trim();
                        //editText.setText(getResources().getString(R.string.naira) + s.toString());
                        count = 1;
                        Logger.log(TAG,"formatting "+withnaira);
                        Log.d(TAG,"formatting "+withnaira);
                        editText.setText(withnaira);
                        editText.setSelection(editText.getText().toString().length());
                    }
                    else if(s.length() == 1 && count == 0)
                    {
                        editText.setText("");
                        count = 0;
                    }
                }
         });
    }

    public void setCustomEditTextChangeListener(CustomEdittextInterface l)
    {
        edittextInterface = l;
    }
}
