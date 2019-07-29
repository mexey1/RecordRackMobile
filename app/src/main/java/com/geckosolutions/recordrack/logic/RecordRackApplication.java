package com.geckosolutions.recordrack.logic;

import android.app.Application;

/**
 * Created by anthony1 on 1/19/18.
 */

public class RecordRackApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }



}
