package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.PurchasesListAdapter;
import com.geckosolutions.recordrack.interfaces.CalendarInterface;
import com.geckosolutions.recordrack.logic.CalendarClass;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.PurchaseFragmentLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 7/10/17.
 */

public class PurchasesFragment extends Fragment implements CalendarInterface
{
    private LinearLayout primaryView;
    private ListView listView;
    private ViewGroup noPurchases;
    private PurchasesListAdapter adapter;
    private TextView dateText;
    private JSONArray array;
    private WeakReference<PurchasesFragment> reference;
    private int position = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        primaryView = (LinearLayout)inflater.inflate(R.layout.purchases_layout,group,false);
        init();
        reference = new WeakReference<PurchasesFragment>(this);
        return primaryView;
    }

    private void init()
    {
        listView = (ListView) primaryView.findViewById(R.id.purchases_list);
        noPurchases = (ViewGroup)primaryView.findViewById(R.id.no_purchases);
        adapter = new PurchasesListAdapter(new WeakReference<Activity>(getActivity()));
        listView.setAdapter(adapter);
        dateText = (TextView)primaryView.findViewById(R.id.date_text);
        primaryView.findViewById(R.id.date).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new CalendarClass(reference.get(),"");
            }
        });

        performQuery();

    }

    private void performQuery()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                //database access should be off UI thread
                array = PurchaseFragmentLogic.getPurchasesForDate(position);
                //populating UI should be on main thread
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        if(array.length() > 0)
                        {
                            listView.setVisibility(View.VISIBLE);
                            noPurchases.setVisibility(View.INVISIBLE);
                            displayResults();
                        }
                        else
                        {
                            listView.setVisibility(View.INVISIBLE);
                            noPurchases.setVisibility(View.VISIBLE);
                            showNoResults();
                        }
                    }
                });
            }
        });
    }

    /**
     * when there are no results, this is the method that would be called
     */
    private void showNoResults()
    {
        adapter.clearArrayList();
    }


    /**
     * method called after a search has been performed to display the results
     */
    private void displayResults()
    {
        try
        {
            for (int i=0; i<array.length();i++)
                adapter.addItem(array.getJSONObject(i));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDatePicked(long datePicked)
    {
        position = UtilityClass.getDateDifferenceFromToday(datePicked);
        if(position == 0)
            dateText.setText("Today");
        else
            dateText.setText(UtilityClass.getDate(datePicked));
        performQuery();
    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return new WeakReference<Activity>(getActivity());
    }
}
