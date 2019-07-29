package com.geckosolutions.recordrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.ModifiedStockActivity;
import com.geckosolutions.recordrack.logic.UtilityClass;

/**
 * Created by anthony1 on 1/9/17.
 */

public class UnitRelationshipAdapter extends BaseAdapter {
    @Override
    public int getCount()
    {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    public void addItem()
    {

    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(UtilityClass.getContext());
        LinearLayout layout=  (LinearLayout)inflater.inflate(R.layout.unit_relationship_item,parent,false);
        layout.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView(parent.getWidth()));
        populateViews(layout,position);
        return layout;
    }

    private void populateViews(View view, int position)
    {
        if(getCount() == 1)
            ((TextView)view.findViewById(R.id.unit1)).setText(ModifiedStockActivity.getBaseUnit());
    }
}
