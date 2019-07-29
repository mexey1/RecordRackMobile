package com.geckosolutions.recordrack.logic;

import android.os.Looper;

/**
 * Created by anthony1 on 1/19/18.
 */

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, final Throwable e)
    {
        e.printStackTrace();
        Logger.writeException(e);
        //Logger.writeException(e,0);
        /*LoggerThread.getLoggerThread().postLoggerTask(new Runnable()
        {
            @Override
            public void run()
            {
                Logger.writeException(e,0);
            }
        });
        */

    }
}
