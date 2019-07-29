package com.geckosolutions.recordrack.logic;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

/**
 * Created by anthony1 on 1/18/16.
 * This class handles all insert, update or delete operations on the database.
 */
public class DatabaseThread extends Thread
{
    private static int count = 0;
    private static DatabaseThread thread;
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

    public static DatabaseThread getDatabaseThread()
    {
        if(thread == null)
        {
            thread = new DatabaseThread();
            thread.setName("DBThread");
            thread.start();
            thread.setPriority();
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

    public void postDatabaseTask(Runnable task)
    {
        if(handler == null) //|| getMeLooper() == null)
            handler = new Handler(getMeLooper());

        handler.post(task);
    }

    private void setPriority()
    {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND+Process.THREAD_PRIORITY_MORE_FAVORABLE);
        Log.d(Thread.currentThread().getName(),""+ Process.getThreadPriority(Process.myTid()));
    }
}
