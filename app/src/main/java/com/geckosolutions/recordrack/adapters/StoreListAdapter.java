package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.AccountSetup;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by anthony1 on 8/15/17.
 */

public class StoreListAdapter extends BaseAdapter
{

    private ArrayList<JSONObject> arrayList;
    private LayoutInflater inflater;
    private WeakReference<Activity> reference;

    public  StoreListAdapter(WeakReference<Activity> reference)
    {
        inflater = LayoutInflater.from(UtilityClass.getContext());
        this.reference = reference;
        arrayList = new ArrayList<>();
    }

    @Override
    public int getCount()
    {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.business_location_item,parent,false);
        }

        populateViews(convertView,position);
        setOnClickListener(convertView,position);
        return convertView;
    }

    public void addItem(JSONObject object)
    {
        arrayList.add(object);
        notifyDataSetChanged();
    }

    /**
     * method to set the business name and address of the store locations.
     * @param layoutToPopulate the viewgroup containing views to populate
     * @param pos the position in the heirarchy of the viewgroup being populated
     */
    private void populateViews(View layoutToPopulate, int pos)
    {
        try
        {
            ViewGroup viewGroup = (ViewGroup)layoutToPopulate;
            JSONObject object = arrayList.get(pos);
            String name = object.getString("name");
            String address = object.getString("address");
            ((TextView)viewGroup.findViewById(R.id.business_name)).setText(name);
            ((TextView)viewGroup.findViewById(R.id.business_address)).setText(address);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * define what happens when a store is chosen
     * @param view the view to add the listener to
     * @param pos the position of the view in the layout
     */
    private void setOnClickListener(View view, int pos)
    {
        view.setTag(pos);
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    //once the user selects the store, we'd like to store the business details
                    //as well as the rack id obtained from the server.
                    int pos = (Integer) v.getTag();
                    UtilityClass.saveRackID(arrayList.get(pos).getString("rack_id"));
                    final JSONObject object = new JSONObject(arrayList.get(pos).toString());
                    object.remove("rack_id");
                    //saving to db has to be off the ui thread
                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                JSONObject object1 = new JSONObject();
                                object1.put("tableName","business_details");
                                object1.put("whereArgs","");
                                DatabaseManager.deleteData(object1);
                                DatabaseManager.insertData(object);
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                    //move to account setup page
                    UtilityClass.setStoreDetailsComplete();
                    reference.get().startActivity(new Intent(reference.get(), AccountSetup.class));
                    reference.get().finish();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
