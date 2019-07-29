package com.geckosolutions.recordrack.logic;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

/**
 * Created by anthony1 on 9/16/17.
 */

public class ReceiptPrintingThread extends Thread
{
    private static int count = 0;
    private static ReceiptPrintingThread thread;
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

    public static ReceiptPrintingThread getReceiptPrintingThread()
    {
        if(thread == null)
        {
            thread = new ReceiptPrintingThread();
            thread.setName("Print");
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

    public void postPrintingTask(Runnable task)
    {
        if(handler == null) //|| getMeLooper() == null)
            handler = new Handler(getMeLooper());

        handler.post(task);
    }

    private void setPriority()
    {
        postPrintingTask(new Runnable()
        {
            @Override
            public void run()
            {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND+Process.THREAD_PRIORITY_LESS_FAVORABLE);
                Log.d(Thread.currentThread().getName(),""+ Process.getThreadPriority(Process.myTid()));
            }
        });
    }
}
