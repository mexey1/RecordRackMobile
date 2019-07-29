package com.geckosolutions.recordrack.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.geckosolutions.recordrack.custom.DebtPaymentView;
import com.geckosolutions.recordrack.logic.Logger;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by anthony1 on 4/22/17.
 * This adapter backs the view pager displaying the payment history for each debt transaction.
 */

public class DebtPaymentViewPagerAdapter extends PagerAdapter
{
    private JSONArray arrays[];
    private JSONArray array;
    private int transactionPosition;
    private final String TAG ="DebtPaymentViewPagerAda";


    public DebtPaymentViewPagerAdapter(FragmentManager manager, JSONArray array[])
    {
        //super(manager);
        this.arrays = array;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int pos)
    {
        DebtPaymentView debtPaymentView = null;

        try
        {
            debtPaymentView = new DebtPaymentView(viewGroup,array.getJSONObject(pos).toString());
            Log.d(TAG,"Object is: "+array.getJSONObject(pos).toString());
            Logger.log(TAG,"Object is: "+array.getJSONObject(pos).toString());
            System.out.println(array.getJSONObject(pos).toString());
            //populateViews(fragment);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        viewGroup.addView(debtPaymentView.getDebtPaymentView());
        return debtPaymentView.getDebtPaymentView();
    }

    @Override
    public int getCount()
    {
        return array.length();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view)
    {

    }

    public void setTransactionPosition(int position)
    {
        transactionPosition = position;
        array = arrays[position];
        notifyDataSetChanged();
    }
}
