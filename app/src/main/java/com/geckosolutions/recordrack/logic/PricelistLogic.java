package com.geckosolutions.recordrack.logic;

import android.app.Activity;
import android.provider.Settings;

import com.geckosolutions.recordrack.fragments.StockFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 6/4/17.
 */

public class PricelistLogic
{
    private WeakReference<Activity> activityWeakReference;
    public PricelistLogic(WeakReference<Activity> activityWeakReference)
    {
        this.activityWeakReference = activityWeakReference;
    }
    public JSONArray getPriceList()
    {
        return StockFragmentLogic.getStockList(false);
    }

    public JSONArray getPrice(int itemID)
    {
        JSONArray result = null;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName","unit");
            object.put("columnArgs",new String[]{"id","unit","retail_price"});
            object.put("whereArgs","item_id="+itemID+" and archived=0");
            result = DatabaseManager.fetchData(object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public void updatePrice(Tuples<Long,Double> tuples[])
    {
        try
        {
            for(int i=0; i<tuples.length;i++)
            {
                JSONObject object = new JSONObject();
                object.put("tableName","unit");
                object.put("retail_price",tuples[i].getSecond());
                DatabaseManager.updateData(object,"id = "+tuples[i].getFirst());
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public WeakReference<Activity> getActivityWeakReference()
    {
        return activityWeakReference;
    }
}
