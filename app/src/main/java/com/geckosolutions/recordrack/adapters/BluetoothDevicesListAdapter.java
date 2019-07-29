package com.geckosolutions.recordrack.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomTextView;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.util.ArrayList;

/**
 * Created by anthony1 on 9/15/17.
 */

public class BluetoothDevicesListAdapter extends BaseAdapter
{
    private ArrayList<BluetoothDevice> devices;
    private LayoutInflater inflater;

    public  BluetoothDevicesListAdapter()
    {
        devices = new ArrayList<>();
        inflater = LayoutInflater.from(UtilityClass.getContext());
        UtilityClass.setBluetoothDevicesListAdapter(this);
    }
    @Override
    public int getCount()
    {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
            convertView = inflater.inflate(R.layout.bluetooth_device_layout,parent,false);
        populateViews((ViewGroup) convertView,position);
        return convertView;
    }

    private void populateViews(ViewGroup viewGroup, int position)
    {
        String name = devices.get(position).getName();
        ((CustomTextView)viewGroup.findViewById(R.id.device_name)).setText(name);
    }

    public void addItem(BluetoothDevice device)
    {
        devices.add(device);
        notifyDataSetChanged();
    }

    public boolean isDeviceInList(BluetoothDevice device)
    {
        boolean result = false;
        for (BluetoothDevice device1 : devices)
        {
            if(device.getAddress().equals(device1.getAddress()))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    public BluetoothDevice getBluetoothDevice(int position)
    {
        return devices.get(position);
    }

    public void clearItems()
    {
        devices.clear();
        notifyDataSetChanged();
    }
}
