package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.SalesInfoListAdapter;
import com.geckosolutions.recordrack.interfaces.CalendarInterface;
import com.geckosolutions.recordrack.logic.CalendarClass;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.SalesInfoFragmentLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by anthony1 on 7/14/16.
 */
public class SalesInfoFragment extends Fragment implements CalendarInterface
{
    private LinearLayout primaryView;
    private static WeakReference<SalesInfoListAdapter> reference;
    private static WeakReference<LinearLayout> ref;
    private WeakReference<Activity> activityWeakReference;
    private WeakReference<SalesInfoFragment> fragmentWeakReference;
    private SalesInfoListAdapter adapter;
    private static final String TAG="SalesInfoFragment";
    private static int position = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        primaryView = (LinearLayout)inflater.inflate(R.layout.sales_info_layout,group,false);
        activityWeakReference = new WeakReference<Activity>(getActivity());
        fragmentWeakReference = new WeakReference<SalesInfoFragment>(this);
        init();
        return primaryView;
    }

    private static Runnable query = new Runnable()
    {
        @Override
        public void run()
        {
            if(SalesInfoFragmentLogic.areSalesAvailable(position))
            {
                final JSONArray items = SalesInfoFragmentLogic.getSalesInfoData(position);
                Log.d(TAG,"sale info "+items.toString());
                Logger.log(TAG,"sale info "+items.toString());
                final double sum = SalesInfoFragmentLogic.getSum(position);
                //populate on UI
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ref.get().findViewById(R.id.no_sales).setVisibility(View.GONE);
                        ref.get().findViewById(R.id.sales_info_list).setVisibility(View.VISIBLE);
                        ref.get().findViewById(R.id.total).setVisibility(View.VISIBLE);
                        ((TextView)ref.get().findViewById(R.id.total)).setText(UtilityClass.formatMoney(sum));
                        reference.get().addItem(items);
                    }
                });

            }
            else
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(ref.get() == null)
                        {
                            //do nothing for now, would figure it out later
                            //ref = new WeakReference<LinearLayout>(primaryView);
                        }
                        else
                        {
                            ref.get().findViewById(R.id.no_sales).setVisibility(View.VISIBLE);
                            ref.get().findViewById(R.id.sales_info_list).setVisibility(View.GONE);
                            ref.get().findViewById(R.id.total).setVisibility(View.GONE);
                            reference.get().deleteAllItems();
                        }
                    }
                });

            }
        }
    };


    private void init()
    {
        adapter = new SalesInfoListAdapter(new WeakReference<Activity>((AppCompatActivity)getActivity()));
        reference = new WeakReference<SalesInfoListAdapter>(adapter);
        ref = new WeakReference<LinearLayout>(primaryView);
        ((ListView)primaryView.findViewById(R.id.sales_info_list)).setAdapter(adapter);
        ((TextView)primaryView.findViewById(R.id.date_text)).setText(UtilityClass.getDateTime(position));

        //define what happens when date is pressed
        primaryView.findViewById(R.id.date).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new CalendarClass(fragmentWeakReference.get(),"Please select a date");
            }
        });

        //check if sales have been performed
        DatabaseThread.getDatabaseThread().postDatabaseTask(query);
    }

    @Override
    public void onDatePicked(long datePicked)
    {
        /**
         * when a date is picked and done is pressed, we retrieve the date value
         * in milliseconds.  Next, get the time in milliseconds for the current day
         * starting at time when hour =0, min =0, sec =0. Compute the difference
         * between the two long values and divide by 24*60*60*1000, this tells us how
         * many days apart the the date selected is from today.
         */
        position = UtilityClass.getDateDifferenceFromToday(datePicked);
        DatabaseThread.getDatabaseThread().postDatabaseTask(query);
        ((TextView)primaryView.findViewById(R.id.date_text)).setText(UtilityClass.getDateTime(position));

    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return activityWeakReference;
    }
}
