package com.geckosolutions.recordrack.logic;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by anthony1 on 1/20/18.
 */

public class LoggerThread extends Thread
{
    private static int count = 0;
    private static LoggerThread thread;
    private volatile Looper meLooper = null;
    private Handler handler;
    private static volatile boolean CONTINUE;
    @Override
    public void run()
    {
        Looper.prepare();
        meLooper = Looper.myLooper();
        synchronized (thread)
        {
            notify();
        }
        Looper.loop();
    }

    private Looper getMeLooper()
    {
        synchronized (thread)
        {
            if (meLooper == null)
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                //thread.start();
            }
        }
        return meLooper;
    }

    public static LoggerThread getLoggerThread()
    {
        if(thread == null)
        {
            thread = new LoggerThread();
            thread.setName("LoggerThread");
            thread.start();
        }

        thread.setUncaughtExceptionHandler(new com.geckosolutions.recordrack.logic.UncaughtExceptionHandler());
        return thread;
    }

    /**
     * this method is used to set flags that allow communication between UI and DatabaseThread
     * @param value
     */
    public static synchronized void setContinueFlag(boolean value)
    {
        CONTINUE = value;
    }

    public static boolean getContinueFlag()
    {
        return CONTINUE;
    }

    public void postLoggerTask(Runnable task)
    {
        if(handler == null) //|| getMeLooper() == null)
            handler = new Handler(getMeLooper());

        handler.post(task);
    }
}
