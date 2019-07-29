package com.geckosolutions.recordrack.custom;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 4/22/17.
 */

public class DebtPaymentView

{
    private LinearLayout parent;
    private String json;
    private final String TAG = "DebtPaymentView";

    public DebtPaymentView(ViewGroup viewGroup, String object)
    {
        LayoutInflater inflater = LayoutInflater.from(UtilityClass.getContext());
        parent = (LinearLayout) inflater.inflate(R.layout.debt_payment_item,viewGroup,false);
        this.json = object;
        populateViews();
    }


    private void populateViews()
    {
        try
        {
            Log.d(TAG,"In the populate views method ");
            Logger.log(TAG,"In the populate views method ");
            JSONObject object = new JSONObject(json);
            String date = object.getString("created");
            String debt = object.getString("total_debt");
            String amountPaid = object.getString("amount_paid");
            String balance = object.getString("balance");
            Log.d(TAG,"Data to be populated: "+object.toString());
            Logger.log(TAG,"Data to be populated: "+object.toString());

            Log.d(TAG,"Date: "+date);
            Logger.log(TAG,"Date: "+date);
            //System.out.println("Date :"+date);
            String dat[] =UtilityClass.getDateAndTime(date);
            ((TextView)parent.findViewById(R.id.date)).setText(dat[0]);
            ((TextView)parent.findViewById(R.id.debt)).setText(UtilityClass.formatMoney(Double.parseDouble(debt)));
            ((TextView)parent.findViewById(R.id.amount_paid)).setText(UtilityClass.formatMoney(Double.parseDouble(amountPaid)));
            ((TextView)parent.findViewById(R.id.balance)).setText(UtilityClass.formatMoney(Double.parseDouble(balance)));

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public View getDebtPaymentView()
    {
        return parent;
    }

}
