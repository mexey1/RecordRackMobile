package com.geckosolutions.recordrack.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.geckosolutions.recordrack.custom.DebtPaymentView;
import com.geckosolutions.recordrack.custom.DebtTransactionView;
import com.geckosolutions.recordrack.logic.Logger;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by anthony1 on 5/6/17.
 * This class handles the debt transaction(s) associated with a particular debtor
 */

public class DebtTransactionViewPagerAdapter extends PagerAdapter
{
    private JSONArray arrays[];
    private final String TAG="DebtTransactionViewPage";
    public DebtTransactionViewPagerAdapter(FragmentManager manager, JSONArray array[])
    {
        //super(manager);
        this.arrays = array;
    }

    @Override
    public int getCount()
    {
        return arrays.length;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int pos)
    {
        DebtTransactionView debtTransactionView = null;
        debtTransactionView = new DebtTransactionView(viewGroup,arrays[pos]);
        Logger.log(TAG,"saled: "+arrays[pos].toString());
        Log.d(TAG,"saled: "+arrays[pos].toString());
        System.out.println("saled: "+arrays[pos].toString());
            //populateViews(fragment);
        viewGroup.addView(debtTransactionView.getDebtTransactionView());
        return debtTransactionView.getDebtTransactionView();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {

    }
}
