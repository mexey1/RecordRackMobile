package com.geckosolutions.recordrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geckosolutions.recordrack.logic.UtilityClass;

import java.util.ArrayList;

/**
 * Created by anthony1 on 5/13/16.
 */
public class CustomSpinnerAdapter extends BaseAdapter
{
    private int resource;
    private ArrayList<String> contents;
    private String first = "Item";

    public CustomSpinnerAdapter(Context context, int resource)
    {
        this.resource = resource;
        contents = new ArrayList();
    }

   /* @Override
    public void add(String item)
    {
        contents.add(item);
        notifyDataSetChanged();
    }*/

    @Override
    public int getCount()
    {
        int count = 1;
        if(contents.size() > 0)
            count = contents.size();

        return count;
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
        {
            LayoutInflater inflater = LayoutInflater.from(UtilityClass.getContext());
            TextView textView = (TextView)inflater.inflate(resource,parent,false);
            if(contents.size() == 0)
                textView.setText(first);
            else
                textView.setText(contents.get(position));

            return textView;
        }
        else
        {
            if(contents.size() == 0)
                ((TextView) convertView).setText(first);
            else
                ((TextView) convertView).setText(contents.get(position));
        }
            return convertView;
    }
}
