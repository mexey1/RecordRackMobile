package com.geckosolutions.recordrack.interfaces;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 6/11/17.
 */

public interface CalendarInterface
{
    /**
     * this method is overriden to define what happens when the user chooses a date
     * @param datePicked the date chosen by the user
     */
    public void onDatePicked(long datePicked);
    public WeakReference<Activity> getActivityWeakReference();
}
