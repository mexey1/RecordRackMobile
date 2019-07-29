package com.geckosolutions.recordrack.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.geckosolutions.recordrack.logic.BluetoothPrint;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

/**
 * Created by anthony1 on 9/13/17.
 */

public class BluetoothReceiver extends BroadcastReceiver
{

    private BluetoothDevice prev;
    private final String TAG="BluetoothReceiver";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if(intent.getAction() == BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        {
            //write code to hide the discovery progress bar. has to be done from ui thread
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run()
                {
                    BluetoothPrint.setDiscoveryFinished();
                }
            });

            Logger.log(TAG,"Discovery finished");
            Log.d(TAG, "Discovery finished");
        }


        //bluetooth device was found
        if(device!=null && intent.getAction() == BluetoothDevice.ACTION_FOUND && device.getName()!=null && UtilityClass.getBluetoothDevicesListAdapter() != null)
        {
            //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(!UtilityClass.getBluetoothDevicesListAdapter().isDeviceInList(device))
                UtilityClass.getBluetoothDevicesListAdapter().addItem(device);
            Logger.log(TAG,"device name "+device.getName());
            Log.d(TAG,"device name "+device.getName());
        }
        else  if(device!=null && BluetoothDevice.BOND_BONDING == device.getBondState())
        {
            Logger.log(TAG,"bonding with device"+device.getName());
            Log.d(TAG,"bonding with device"+device.getName());
            prev = device;
            BluetoothPrint.setBondingDevice(device);
        }

        //this isn't working now...would revisit this
        if(device!=null && prev!=null && BluetoothDevice.BOND_BONDED == device.getBondState() && prev.getName().equalsIgnoreCase(device.getName()))
        {
            //String string = device.BOND_BONDED;
            Logger.log(TAG,"Device bonded "+device.getName());
            Log.d(TAG,"Device bonded "+device.getName());
            //dismiss dialog
            BluetoothPrint.dismissDialog(device.getName());
        }
    }
}
