package com.geckosolutions.recordrack.custom;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

/**
 * Created by anthony1 on 8/30/15.
 */
public class CharacterInputFilter implements InputFilter
{
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=start; i<end;i++)
        {
            if(Character.isLetterOrDigit(source.charAt(i)))
            {
                if(Character.isLetter(source.charAt(i)))
                    stringBuilder.append(Character.toUpperCase(source.charAt(i)));
                else
                    stringBuilder.append(source.charAt(i));
            }
        }
        return stringBuilder;
    }
}
