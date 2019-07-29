package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.NewSalesListAdapter;
import com.geckosolutions.recordrack.adapters.SalesSearchListAdapter;
import com.geckosolutions.recordrack.interfaces.CalendarInterface;
import com.geckosolutions.recordrack.logic.CalendarClass;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Created by anthony1 on 6/9/16.
 */
public class SalesSearchFragment extends BaseSearchFragment implements CalendarInterface
{
    private static String textToSearchFor;
    private static String startDate, endDate, whereArgs;
    private static RelativeLayout noContent;
    private static String WHEREARGS;
    private Dialog dialog;
    private int searchMode; //3 search modes are available: 1 search by customer name; 2 search by ID; 3 search by product

    private int position;
    private SearchView searchView;
    private TextView date,date2;
    private SalesSearchFragment calendarInterface;
    private static final String TAG="SalesSearchFragment";
    //private boolean limit = false;


    @Override
    protected void initSearch()
    {
        //hold a reference to this CalendarInterface
        calendarInterface = this;
        //noContent = (RelativeLayout)((LinearLayout) reference.get().findViewById(R.id.search_display)).findViewById(R.id.no_content);
        ((ListView) primaryView.findViewById(R.id.search_display)).setAdapter(new SalesSearchListAdapter(new WeakReference<Activity>(getActivity()),0));
        date = (TextView)primaryView.findViewById(R.id.date);
        date2 = (TextView)primaryView.findViewById(R.id.date2);


        setInitialSearchConditions();

        //primaryView is declared in the BaseSearchFragment class

        //define what happens when the calendar button is clicked
        primaryView.findViewById(R.id.search_menu).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new CalendarClass(calendarInterface,"Select a date to view sales transactions");
            }
        });

        searchView = (SearchView)primaryView.findViewById(R.id.search_view);

        //define what happens when the searchview is clicked
        //when the search view is clicked, we'd like to show a dialog for choosing search parameters
        searchView.setOnSearchClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = UtilityClass.showCustomDialog(R.layout.search_mode_layout,new WeakReference<Activity>(calendarInterface.getActivity()));
                initSearchOptionDialog();

            }
        });

        //define what happens when the text in the searchview changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText)
            {
                if (newText.length() == 0)
                {
                    Log.d("Text changed","Hello there");
                    //UtilityClass.showToast("Here there");
                    setInitialSearchConditions();
                    return true;
                }
                textToSearchFor = null;
                textToSearchFor = newText.trim();
                if(searchMode == 1)
                    whereArgs = "sale_transaction_id=sale_transaction.id AND name LIKE '" + textToSearchFor + "%' " +
                            "AND sale_transaction.archived=0 AND sale_transaction.last_edited BETWEEN '"+startDate+"' AND '"+endDate+"'";
                else if(searchMode ==2)
                    whereArgs = "sale_transaction_id=sale_transaction.id AND sale_transaction_id LIKE '" + textToSearchFor + "%' "+
                            "AND sale_transaction.archived=0 AND sale_transaction.last_edited BETWEEN '"+startDate+"' AND '"+endDate+"'";
                DatabaseThread.getDatabaseThread().postDatabaseTask(search);
                return true;
            }
        });


        //define what happens when the searchview is closed
        searchView.setOnCloseListener(new SearchView.OnCloseListener()
        {
            @Override
            public boolean onClose()
            {
                date2.setVisibility(View.GONE);
                date.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                searchView.setLayoutParams(params);
                params = null;
                return false;
            }
        });

    }

    private void initSearchOptionDialog()
    {
        //define what happens when user wants to search by name
        dialog.findViewById(R.id.name_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                initSearchView();
                searchView.setInputType(InputType.TYPE_CLASS_TEXT);
                searchView.setQueryHint("Enter customer's name");
                searchMode = 1;
                dialog.dismiss();
            }
        });

        //define what happens when user wants to search by transaction id
        dialog.findViewById(R.id.id_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                initSearchView();
                searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
                searchView.setQueryHint("Enter transaction ID");
                searchMode = 2;
                dialog.dismiss();
            }
        });
    }


    /**
     * method called to initialize the search view
     */
    private void initSearchView()
    {
        date.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        date2.setVisibility(View.VISIBLE);
        params = null;
    }

    @Override
    public void onDatePicked(long datePicked)
    {
        //calculate difference between date selected and today
        position = UtilityClass.getDateDifferenceFromToday(datePicked);
        //display what date is current being shown
        if(position == 0)
        {
            date.setText("Today");
            date2.setText("Today");
        }
        else
        {
            date.setText(UtilityClass.getDate(datePicked));
            date2.setText(UtilityClass.getDate(datePicked));
        }
        //calendarDialog.dismiss();
        //dialog.dismiss();
        //calendarDialog = null;
        //dialog = null;
        setInitialSearchConditions();
    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return new WeakReference<Activity>(getActivity());
    }

    /**
     * this method is called during initialization of the sales window to set search parameters
     * like the whereArgs variable etc. This sets search parameters for fetching all sales data
     * when this fragment is being viewed for the first time. By default, this obtains all the sales
     * for the current date.
     */
    private void setInitialSearchConditions()
    {
        startDate = UtilityClass.getDateTime(position,0,0,0);
        endDate = UtilityClass.getDateTime(position,23,59,59);
        WHEREARGS = "sale_transaction.id=sale_transaction_id AND sale_item.last_edited >= '"+
                    startDate+"' AND sale_item.last_edited <= '"+endDate+"' AND sale_transaction.archived='0'";
        whereArgs = WHEREARGS;
        DatabaseThread.getDatabaseThread().postDatabaseTask(search);
    }

    //this is the runnable for performing searches as the user types in the search field.
    private static Runnable search = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                JSONObject object = new JSONObject();
                String columns[] = {"name","sale_transaction_id", "amount_paid","suspended","sale_transaction.last_edited"};
                object.put("columnArgs",columns);
                object.put("tableName", "sale_item");
                object.put("join",UtilityClass.getJoinQuery("sale_transaction",whereArgs));
                object.put("extra","GROUP BY sale_transaction_id");
                //object.put("whereArgs", whereArgs);
                //object.put("extra","INNER JOIN sale_transaction ON "+whereArgs+" GROUP BY sale_transaction_id");
                JSONArray response = DatabaseManager.fetchData(object);
                //once response has been obtained, call the displayResponse method to display the
                //results
                displayResponse(response);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };

    /**
     * this method is called to display the result of the search. It is synchronized for thread
     * safety (to ensure the correct data is/are displayed).
     * @param response the response from the search passed in as a json array.
     */
    private static synchronized void displayResponse(final JSONArray response)
    {
        Logger.log(TAG,"whereArgs "+whereArgs+" ::response is:: "+response);
        Log.d(TAG,"whereArgs "+whereArgs+" ::response is:: "+response);

        Handler handler = new Handler(Looper.getMainLooper());
        final LayoutInflater inflater = LayoutInflater.from(UtilityClass.getContext());
        Logger.log(TAG,"About to display response");
        Log.d(TAG,"About to display response");
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                //remove all views
                SalesSearchListAdapter adapter =(SalesSearchListAdapter)((ListView) reference.get().primaryView.findViewById(R.id.search_display)).getAdapter();
                adapter.clearArrayList();
                ((ListView) reference.get().primaryView.findViewById(R.id.search_display)).setVisibility(View.VISIBLE);
                ((RelativeLayout) reference.get().primaryView.findViewById(R.id.no_content)).setVisibility(View.GONE);
                Logger.log(TAG,"::Code got here::");
                Log.d(TAG,"::Code got here::");

                if(response.length() > 0)
                {

                    for (int count = 0; count < response.length();count++)
                    {
                        try
                        {
                            adapter.addItem(response.getJSONObject(count));
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    if(((SearchView)reference.get().primaryView.findViewById(R.id.search_view)).getQuery().length() ==0)
                    {
                        ((ListView) reference.get().primaryView.findViewById(R.id.search_display)).setVisibility(View.GONE);
                        ((RelativeLayout) reference.get().primaryView.findViewById(R.id.no_content)).setVisibility(View.VISIBLE);
                    }
                        //.addView(noContent);
                }
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        primaryView = null;
        noContent = null;
        reference = null;
    }

    public void refresh()
    {
        setInitialSearchConditions();;
    }
}
