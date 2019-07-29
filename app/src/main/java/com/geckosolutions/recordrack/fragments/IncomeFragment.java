package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.IncomeListAdapter;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.interfaces.CalendarInterface;
import com.geckosolutions.recordrack.logic.CalendarClass;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.IncomeFragmentLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 9/26/16.
 */
public class IncomeFragment extends Fragment implements CalendarInterface
{
    protected CoordinatorLayout coordinatorLayout;
    protected LayoutInflater layoutInflater;

    private ListView listView;
    private IncomeListAdapter adapter;
    private Dialog dialog;
    protected static WeakReference<IncomeFragment> reference;
    private CustomEditText name,purpose,amount;
    private static int datePosition;
    private IncomeFragmentLogic logic;
    protected String tableName;
    protected int mode;//0 for income, 1 for expense
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        coordinatorLayout = (CoordinatorLayout)inflater.inflate(R.layout.income_fragment,group,false);
        layoutInflater = inflater;
        reference = new WeakReference<IncomeFragment>(this);
        mode = 0;
        tableName = "income";
        init();
        return coordinatorLayout;
    }

    protected void init()
    {
        datePosition = 0;
        logic = new IncomeFragmentLogic();
        if(mode == 0)
            listView = (ListView)coordinatorLayout.findViewById(R.id.income_list);
        else if(mode ==1)
            listView = (ListView)coordinatorLayout.findViewById(R.id.expense_list);
        adapter = new IncomeListAdapter(new WeakReference<Activity>(getActivity()),logic,mode,tableName);
        listView.setAdapter(adapter);
        reference = new WeakReference<IncomeFragment>(this);
        //define what happens when add button is pressed
        coordinatorLayout.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mode == 0)//if income
                    dialog = UtilityClass.showCustomDialog(R.layout.new_income_entry,new WeakReference<Activity>((AppCompatActivity)getActivity()));
                else if(mode == 1)
                    dialog = UtilityClass.showCustomDialog(R.layout.new_expense_entry,new WeakReference<Activity>((AppCompatActivity)getActivity()));

                name = (CustomEditText)dialog.findViewById(R.id.name);
                purpose = (CustomEditText)dialog.findViewById(R.id.purpose);
                amount = (CustomEditText)dialog.findViewById(R.id.amount);
                amount.enableTextChangedListener();
                dialog.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                            @Override
                            public void run()
                            {
                                insertEntry();
                            }
                        });
                    }
                });

                dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
            }
        });

        //define what happens when the date view is pressed
        coordinatorLayout.findViewById(R.id.date).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mode == 0)
                    new CalendarClass(reference.get(),"Select a date to view income entries");
                else if(mode == 1)
                    new CalendarClass(reference.get(),"Select a date to view expense entries");
            }
        });

        DatabaseThread.getDatabaseThread().postDatabaseTask(query);
    }

    public static void displayListView()
    {
        if(reference.get().mode == 0)
             reference.get().coordinatorLayout.findViewById(R.id.empty_income_layout).setVisibility(View.GONE);
        else if(reference.get().mode == 1)
            reference.get().coordinatorLayout.findViewById(R.id.empty_expense_layout).setVisibility(View.GONE);
        reference.get().listView.setVisibility(View.VISIBLE);
    }

    /**
     * insert entries to the database
     */
    private void insertEntry()
    {
        try
        {
            String name = reference.get().name.getText().toString().trim();
            String purpose = reference.get().purpose.getText().toString().trim();
            String amount = reference.get().amount.getText().toString().trim();
            double amount1 = UtilityClass.removeNairaSignFromString(amount);

            final JSONObject object1 = new JSONObject();
            object1.put("name",name);
            object1.put("purpose",purpose);
            object1.put("amount",amount);

            logic.insertIncomeEntry(tableName,name,purpose,amount1);
            //display the newly added income entry. Run on UI thread
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    reference.get().adapter.addItem(object1);
                    reference.get().dialog.dismiss();
                    reference.get().dialog = null;
                    reference.get().name = null;
                    reference.get().purpose = null;
                    reference.get().amount = null;
                }
            });

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * this pulls up income entries for a given date. The date is specified by datePosition
     * which is by default 0 (meaning today).
     */
    private static Runnable query = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                JSONObject object = new JSONObject();
                object.put("tableName",reference.get().tableName);
                object.put("columns", new String[]{"id","name","purpose","amount"});
                String min = null, max = null, whereArgs = null;
                min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
                max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
                whereArgs = "archived='0' AND last_edited >='"+min+"' AND last_edited<='"+max+"'";
                object.put("whereArgs",whereArgs);
                final JSONArray array = DatabaseManager.fetchData(object);

                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        reference.get().adapter.clearArrayList();
                        for(int i=0; i<array.length(); i++)
                        {
                            try
                            {
                                JSONObject jsonObject = array.getJSONObject(i);
                                String amount = UtilityClass.formatMoney(Double.parseDouble(jsonObject.getString("amount")));
                                jsonObject.remove("amount");
                                jsonObject.put("amount",amount);
                                reference.get().adapter.addItem(jsonObject);
                                jsonObject = null;
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDatePicked(long datePicked)
    {
        datePosition = UtilityClass.getDateDifferenceFromToday(datePicked);
        if(datePosition == 0)
            ((TextView)coordinatorLayout.findViewById(R.id.date_text)).setText("Today");
        else
            ((TextView)coordinatorLayout.findViewById(R.id.date_text)).setText(UtilityClass.getDate(datePicked));
        DatabaseThread.getDatabaseThread().postDatabaseTask(query);
    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return new WeakReference<Activity>(getActivity());
    }
}
