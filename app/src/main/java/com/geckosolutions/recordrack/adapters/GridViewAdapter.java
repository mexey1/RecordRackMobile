package com.geckosolutions.recordrack.adapters;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.UtilityClass;

/**
 * Created by anthony1 on 8/5/17.
 */

public class GridViewAdapter implements ListAdapter
{
    private LayoutInflater inflater;
    private String labels [] = {"Dashboard","Sales","Purchases","Stock","Price list","Debtors","Income","Expenses","Note"};
    private int colors []  = new int[]{R.color.pomegranate,R.color.orange1,R.color.blue1,R.color.emerald,R.color.midnight_blue,R.color.red,R.color.applegreen,R.color.battleship_grey,R.color.wisteria};
    private String icons [] = UtilityClass.getContext().getResources().getStringArray(R.array.drawer_item_icons);
    private final int COUNT =9;

    public GridViewAdapter()
    {
        inflater = LayoutInflater.from(UtilityClass.getContext());
    }


    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return COUNT;
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
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            ViewGroup item  = (ViewGroup) inflater.inflate(R.layout.grid_view_item,parent,false);
            ((TextView)item.findViewById(R.id.icon)).setText(icons[position]);
            ((TextView)item.findViewById(R.id.icon)).setTextColor(UtilityClass.getContext().getResources().getColor(colors[position]));
            ((TextView)item.findViewById(R.id.label)).setText(labels[position]);
            convertView = item;
        }
        else
        {
            ((TextView)convertView.findViewById(R.id.icon)).setText(icons[position]);
            ((TextView)convertView.findViewById(R.id.icon)).setTextColor(UtilityClass.getContext().getResources().getColor(colors[position]));
            ((TextView)convertView.findViewById(R.id.label)).setText(labels[position]);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
