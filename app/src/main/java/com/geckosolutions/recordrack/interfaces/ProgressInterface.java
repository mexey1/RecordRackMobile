package com.geckosolutions.recordrack.interfaces;

import android.widget.ProgressBar;

/**
 * Created by anthony1 on 1/31/18.
 */

public interface ProgressInterface
{
    public ProgressBar bar = null;

    /**
     * this method is implemented so a worker thread can update the progress
     * @param p the progress made
     */
    public void setProgress(int p);

    /**
     * maximum value attainable
     * @param max max value 
     */
    public void setMax(int max);

    public void dismissDialog();
}
