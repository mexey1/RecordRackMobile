package com.geckosolutions.recordrack.adapters;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.SharedLayoutNew;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by anthony1 on 8/31/17.
 */

public class SharedLayoutGridAdapter //extends BaseAdapter
{
    private ArrayList<String> list;
    private ArrayList<Long> itemIds;
    private LayoutInflater inflater;
    private ArrayList<View> views;
    private Hashtable<View,Integer> table;
    private int pos;

    public SharedLayoutGridAdapter()
    {
        list = new ArrayList<>();
        itemIds = new ArrayList<>();
        views = new ArrayList<>();
        table = new Hashtable<>();
        inflater = LayoutInflater.from(UtilityClass.getContext());
    }

    /*@Override
    public int getCount()
    {
        return list.size();
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
            convertView = inflater.inflate(R.layout.shared_layout_item,parent,false);
            ((TextView)convertView.findViewById(R.id.item)).setText(list.get(position));
        }
        else
            ((TextView)convertView.findViewById(R.id.item)).setText(list.get(position));

        views.add(position,convertView);
        return convertView;
    }*/

    //@Override
    public Object getItem(int position)
    {
        return views.get(position);
    }

    public int getCount()
    {
        return list.size();
    }

    public void addItem(String item,long itemID,View view)
    {
        list.add(item);
        itemIds.add(itemID);
        table.put(view,pos++);
        //notifyDataSetChanged();
    }

    public long getItemID(View view)
    {
        int pos = table.get(view);
        return itemIds.get(pos);
    }
}
