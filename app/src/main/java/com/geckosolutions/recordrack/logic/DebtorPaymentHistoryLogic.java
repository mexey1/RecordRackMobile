package com.geckosolutions.recordrack.logic;

import android.app.Dialog;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.DebtPaymentViewPagerAdapter;
import com.geckosolutions.recordrack.adapters.DebtTransactionViewPagerAdapter;
import com.geckosolutions.recordrack.adapters.SaleViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anthony1 on 4/22/17.
 */

public class DebtorPaymentHistoryLogic
{
    private SaleViewAdapter adapter;
    private ListView listView;
    private DialogFragment fragment;
    private Dialog dialog;
    private ViewPager paymentsPager,debtTransactionPager;
    private JSONArray[] sales,payments;
    private JSONObject debtor;
    private DebtPaymentViewPagerAdapter paymentsPagerAdapter;
    private LinearLayout positions,positions_;
    private DebtTransactionViewPagerAdapter debtTransactionPagerAdapter;
    private int paymentPosition;
    private final String TAG = "DebtorPaymentHistoryLog";


    private void init() throws JSONException
    {
        dialog = fragment.getDialog();
        adapter = new SaleViewAdapter();
        Log.d(TAG,"Fragment :"+Boolean.toString(dialog==null));
        //listView = (ListView) dialog.findViewById(R.id.items_list);
        //listView.setAdapter(adapter);
        debtTransactionPager = (ViewPager)dialog.findViewById(R.id.debt_transaction);
        debtTransactionPagerAdapter = new DebtTransactionViewPagerAdapter(fragment.getChildFragmentManager(),sales);


        paymentsPager = (ViewPager)dialog.findViewById(R.id.view_pager);
        paymentsPagerAdapter = new DebtPaymentViewPagerAdapter(fragment.getChildFragmentManager(),payments);

        //add the views to indicate position only if more than one sale exists
        if(sales.length>1)
        {
            positions = (LinearLayout) dialog.findViewById(R.id.positions);
            addPositionIndicator(sales.length,positions,0);
        }
        if(payments[0].length() >1)//if more than one payment exists
        {
            displayPositionIndicatorForPaymentsPager(0);
            ((LinearLayout) dialog.findViewById(R.id.positions_)).setVisibility(View.VISIBLE);
        }

        //set which payment should be displayed
        paymentsPagerAdapter.setTransactionPosition(0);

        paymentsPager.setAdapter(paymentsPagerAdapter);
        debtTransactionPager.setAdapter(debtTransactionPagerAdapter);

        debtTransactionPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                Log.d(TAG,"Page selected : "+position);
                paymentsPager.setAdapter(null);
                //viewPager.invalidate();
                //viewPagerAdapter = new DebtPaymentViewPagerAdapter(fragment.getChildFragmentManager(),payments);
                paymentsPagerAdapter.setTransactionPosition(position);
                paymentsPager.setAdapter(paymentsPagerAdapter);

                if(payments[position].length() >1)//if more than one payment exists
                {
                    displayPositionIndicatorForPaymentsPager(position);
                    ((LinearLayout) dialog.findViewById(R.id.positions_)).setVisibility(View.VISIBLE);
                }
                else
                    ((LinearLayout) dialog.findViewById(R.id.positions_)).setVisibility(View.GONE);

                //modify the position indicator accordingly
                if(position == sales.length-1)//end of viewpager
                {
                    positions.findViewById(position).setBackgroundResource(R.drawable.grey_circle);
                    positions.findViewById(position-1).setBackgroundResource(R.drawable.white_circle);
                }
                else if(position == 0)//disable first and last
                {
                    positions.findViewById(position).setBackgroundResource(R.drawable.grey_circle);
                    positions.findViewById(position+1).setBackgroundResource(R.drawable.white_circle);
                }
                else
                {
                    positions.findViewById(position).setBackgroundResource(R.drawable.grey_circle);
                    positions.findViewById(position+1).setBackgroundResource(R.drawable.white_circle);
                    positions.findViewById(position-1).setBackgroundResource(R.drawable.white_circle);
                }
                //viewPager.setAdapter(viewPagerAdapter);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
        Log.d(TAG,"Debtor : "+debtor.toString());

        ((TextView)dialog.findViewById(R.id.name)).setText(debtor.getString("preferred_name"));
    }

    /**
     * this method is called to display and initialize position indicator for payments fragment
     * @param p position of transaction whose payments are to be shown.
     */
    private void displayPositionIndicatorForPaymentsPager(int p)
    {
        paymentPosition = p;
        positions_ = (LinearLayout) dialog.findViewById(R.id.positions_);
        positions_.removeAllViews();
        addPositionIndicator(payments[p].length(),positions_,sales.length);


        paymentsPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                //modify the position indicator accordingly
                if(position == payments[paymentPosition].length()-1)//end of viewpager
                {
                    positions_.getChildAt(position).setBackgroundResource(R.drawable.grey_circle);
                    positions_.getChildAt(position-1).setBackgroundResource(R.drawable.white_circle);
                }
                else if(position == 0)//disable first and last
                {
                    positions_.getChildAt(position).setBackgroundResource(R.drawable.grey_circle);
                    positions_.getChildAt(position+1).setBackgroundResource(R.drawable.white_circle);
                }
                else
                {
                    positions_.getChildAt(position).setBackgroundResource(R.drawable.grey_circle);
                    positions_.getChildAt(position+1).setBackgroundResource(R.drawable.white_circle);
                    positions_.getChildAt(position-1).setBackgroundResource(R.drawable.white_circle);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * method to create and display position indicator
     * @param count number of position indicator to create
     * @param parent layout to add position indicators to
     * @param id this is 0 for debt transactions and length of debt transactions for payment pager
     */
    private void addPositionIndicator(int count, ViewGroup parent,int id)
    {
        for(int i=0; i<count;i++)
        {
            int h = UtilityClass.convertToPixels(10);
            int m = UtilityClass.convertToPixels(5);
            View view = new View(UtilityClass.getContext());
            if(i==0)
                view.setBackgroundDrawable(UtilityClass.getContext().getResources().getDrawable(R.drawable.grey_circle));
            else
                view.setBackgroundDrawable(UtilityClass.getContext().getResources().getDrawable(R.drawable.white_circle));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(h,h);
            params.setMargins(m,m,m,m);
            view.setLayoutParams(params);
            view.setId(id++);
            parent.addView(view);
        }
    }

    /*private void displayResults()
    {
        try
        {
            System.out.println("sales "+sales);
            init();
            for (int i=0; i<sales.length(); i++)
                adapter.addItem(sales.getJSONObject(0));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }*/

    /**
     * this method defines what happens when the View button is pressed for a given debtor
     * @param debtor
     */
    public void onViewPressed(DialogFragment dialogFragment, JSONObject debtor)
    {
        try
        {
            Log.d(TAG, "is Dialog null?" +Boolean.toString(dialogFragment==null));
            Log.d(TAG,"is Debtor object null? "+Boolean.toString(this.debtor==null));
            this.debtor = debtor;

            if(dialogFragment != null)
                fragment = dialogFragment;
            else
                return;
            String table = null, transactionID;
            JSONObject object = new JSONObject();
            object.put("tableName","debt_transaction");
            object.put("columns", new String[]{"id","transaction_table", "transaction_id"});
            //object.put("whereArgs","debtor_id="+tag);
            object.put("whereArgs","debtor_id="+debtor.getString("id"));
            JSONArray array = DatabaseManager.fetchData(object);

            sales = new JSONArray[array.length()];
            payments = new JSONArray[array.length()];
            for(int i=0; i< array.length(); i++)
            {
                table = array.getJSONObject(i).getString("transaction_table");
                transactionID = array.getJSONObject(i).getString("transaction_id");
                //if(table.length() > 0 && table.equalsIgnoreCase("sales"))
                sales[i] = UtilityClass.fetchDataForSaleTransaction(transactionID);
                Log.d(TAG,"sales: "+sales[i].toString());
                table = null;

                //retrieve payments
                object = null;
                object = new JSONObject();
                object.put("tableName","debt_payment");
                object.put("columns", new String[]{"total_debt","amount_paid","balance","created"});
                object.put("whereArgs","debt_transaction_id="+array.getJSONObject(i).getString("id"));
                payments[i] = DatabaseManager.fetchData(object);
            }

            //display results on UI thread
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        init();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public static double getTotalDebtPayment(int datePosition)
    {
        String min = null, max = null, whereArgs = null;
        min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
        max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
        whereArgs = "archived='0' AND last_edited >='"+min+"' AND last_edited<='"+max+"'";
        return DatabaseManager.sumUpRowsWithWhere("debt_payment","amount_paid",whereArgs);
    }
}
