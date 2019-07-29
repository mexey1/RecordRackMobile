package com.geckosolutions.recordrack.interfaces;

/**
 * Created by anthony1 on 9/2/17.
 * This interface is used by the CustomEdittext to notify the interested party that the text has changed.
 * This method is overriden to let the implementing class do whatever with the new text in the edittext
 */

public interface CustomEdittextInterface
{
    public void onTextChanged(String g);
}
