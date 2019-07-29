package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.fragments.IncomeFragment;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.IncomeFragmentLogic;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by anthony1 on 9/27/16.
 */
public class IncomeListAdapter extends BaseAdapter
{
    private ArrayList<JSONObject> arrayList;
    private LayoutInflater inflater;
    private WeakReference<Activity> activityWeakReference;
    private Dialog dialog;
    private IncomeFragmentLogic logic;
    private int mode;
    private String table;
    private boolean shouldDeleteFromArraylist;
    private final String TAG ="IncomeListAdapter";

    public IncomeListAdapter(WeakReference<Activity> reference, IncomeFragmentLogic logic, int mode, String table)
    {
        arrayList = new ArrayList<>();
        activityWeakReference = reference;
        this.logic = logic;
        this.mode = mode;
        this.table = table;
        inflater = LayoutInflater.from(UtilityClass.getContext());

    }
    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    /**
     * method to add a new item to the listview.
     */
    public void addItem(JSONObject object)
    {
        arrayList.add(object);
        IncomeFragment.displayListView();
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.income_item, parent, false);
            convertView.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
        }
        populateView(convertView,position);
        return convertView;
    }

    /**
     * call this method to clear what's being displayed
     */
    public void clearArrayList()
    {
        arrayList.clear();
        notifyDataSetChanged();
    }

    private void populateView(View view, int position)
    {
        TextView name = (TextView)view.findViewById(R.id.name);
        TextView amount = (TextView)view.findViewById(R.id.amount);
        initViewEditDelete(view,position);

        try
        {
            JSONObject object = arrayList.get(position);
            name.setText(object.getString("name"));
            amount.setText(object.getString("amount"));

            object = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        amount = null;
        name = null;;
    }

    private void initViewEditDelete(View view, int position)
    {
        //define what happens when the view button is pressed
        view.findViewById(R.id.view).setTag(position);
        view.findViewById(R.id.view).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    //define layout to inflate
                    JSONObject object = arrayList.get((int)v.getTag());
                    if(mode == 0)
                        dialog = UtilityClass.showCustomDialog(R.layout.income_view_layout,activityWeakReference);
                    else
                        dialog = UtilityClass.showCustomDialog(R.layout.expense_view_layout,activityWeakReference);
                    ((TextView)dialog.findViewById(R.id.name)).setText(object.getString("name"));
                    ((TextView)dialog.findViewById(R.id.purpose)).setText(object.getString("purpose"));
                    ((TextView)dialog.findViewById(R.id.amount)).setText(object.getString("amount"));
                    dialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                        }
                    });
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

        view.findViewById(R.id.edit).setTag(position);
        view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    if(mode == 0)
                        dialog = UtilityClass.showCustomDialog(R.layout.income_edit_layout,activityWeakReference);
                    else if(mode == 1)
                        dialog = UtilityClass.showCustomDialog(R.layout.expense_edit_layout,activityWeakReference);
                    final int pos = (int)v.getTag();
                    JSONObject object = arrayList.get(pos);
                    ((EditText)dialog.findViewById(R.id.name)).setText(object.getString("name"));
                    ((EditText)dialog.findViewById(R.id.purpose)).setText(object.getString("purpose"));
                    ((EditText)dialog.findViewById(R.id.amount)).setText(object.getString("amount"));
                    ((CustomEditText)dialog.findViewById(R.id.amount)).enableTextChangedListener();
                    dialog.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            String name = ((EditText)dialog.findViewById(R.id.name)).getText().toString().trim();
                            String purpose = ((EditText)dialog.findViewById(R.id.purpose)).getText().toString().trim();
                            String amount = ((EditText)dialog.findViewById(R.id.amount)).getText().toString().trim();
                            saveIncomeEntry(pos,name,purpose,UtilityClass.removeNairaSignFromString(amount));
                            dialog.dismiss();
                        }
                    });
                    //define what happens when the edit button is pressed
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

        view.findViewById(R.id.delete).setTag(position);
        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                dialog = UtilityClass.showCustomDialog(R.layout.confirm_delete_layout, activityWeakReference);
                dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View p)
                    {
                        shouldDeleteFromArraylist = true;
                        deleteIncomeEntry((int)v.getTag());
                        //notifyDataSetInvalidated();
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void deleteIncomeEntry(final int position)
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject object = arrayList.get(position);
                    long id = object.getLong("id");
                    object = new JSONObject();
                    object.put("tableName",table);
                    object.put("archived",1);
                    Logger.log(TAG,"id is "+id);
                    Log.d(TAG,"id is "+id);
                    DatabaseManager.updateData(object,"id = "+id);
                    if(shouldDeleteFromArraylist)
                        arrayList.remove(position);

                    //once we have deleted the entry, we'd like to update the UI
                    new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run()
                        {
                            notifyDataSetChanged();
                        }
                    });

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveIncomeEntry(final int pos, final String name, final String purpose, final double amount)
    {
        shouldDeleteFromArraylist = false;
        deleteIncomeEntry(pos);
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject object = new JSONObject();
                    object.put("name",name);
                    object.put("purpose",purpose);
                    object.put("amount",UtilityClass.formatMoney(amount));
                    object.put("id",logic.insertIncomeEntry(table,name,purpose,amount));
                    arrayList.remove(pos);
                    arrayList.add(pos,object);

                    //once we have deleted the entry, we'd like to update the UI
                    new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run()
                        {
                            notifyDataSetChanged();
                        }
                    });

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
