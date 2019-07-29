package com.geckosolutions.recordrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.util.ArrayList;

/**
 * Created by anthony1 on 9/1/17.
 */

public class SpinnerAdapter extends BaseAdapter
{
    private ArrayList<String> unit;
    private LayoutInflater inflater;


    public SpinnerAdapter()
    {
        unit = new ArrayList<>();
        inflater = LayoutInflater.from(UtilityClass.getContext());
    }

    @Override
    public int getCount()
    {
        return unit.size();
    }

    @Override
    public Object getItem(int position)
    {
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
        {
            convertView = inflater.inflate(R.layout.spinner_item,parent,false);
            ((TextView)convertView).setText(unit.get(position));
        }
        else
            ((TextView)convertView).setText(unit.get(position));
        return convertView;
    }

    public void addItem(String u)
    {
        unit.add(u);
        notifyDataSetChanged();
    }

    public void clearItems()
    {
        unit.clear();
    }

}
