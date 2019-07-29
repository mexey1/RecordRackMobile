package com.geckosolutions.recordrack.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.SaleViewAdapter;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anthony1 on 5/6/17.
 */

public class DebtTransactionView
{
    private LinearLayout parent;
    private ListView items;
    private SaleViewAdapter adapter;
    private JSONArray array;
    private TextView textView;

    public DebtTransactionView(ViewGroup viewGroup, JSONArray array)
    {
        try
        {
            LayoutInflater inflater = LayoutInflater.from(UtilityClass.getContext());
            parent = (LinearLayout) inflater.inflate(R.layout.debt_transaction_item,viewGroup,false);
            textView = (TextView)parent.findViewById(R.id.date);
            items = (ListView)parent.findViewById(R.id.items_list);
            adapter = new SaleViewAdapter(false);
            items.setAdapter(adapter);
            this.array = array;
            String date[] = UtilityClass.getDateAndTime(array.getJSONObject(0).getString("last_edited"));
            textView.setText(date[0]);
            populateViews();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


    private void populateViews()
    {
        try
        {
            for (int i=0; i<array.length(); i++)
                adapter.addItem(array.getJSONObject(i));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public View getDebtTransactionView()
    {
        return parent;
    }
}
