package com.geckosolutions.recordrack.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.geckosolutions.recordrack.logic.DataUploadClass;
import com.geckosolutions.recordrack.logic.UtilityClass;

/**
 * Created by anthony1 on 9/13/17.
 * Receives changes in the internet connection state of the device
 */

public class InternetReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getExtras()!=null)
        {
            //NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo ni =  ((ConnectivityManager)UtilityClass.getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if(ni!=null && ni.isConnected())
            {
                DataUploadClass.retryDataUpload();
            }
            System.out.println("network satte: ");
        }
    }
}
