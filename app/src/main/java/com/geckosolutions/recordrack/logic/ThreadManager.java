package com.geckosolutions.recordrack.logic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Anthony on 5/31/15.
 */
public class ThreadManager
{
    private static ExecutorService service;
    public static ExecutorService getExecutorService()
    {
        if(service == null)
        {
            service = Executors.newCachedThreadPool();
            return service;
        }
        else
            return  service;
    }
}
