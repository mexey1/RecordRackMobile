package com.geckosolutions.recordrack.logic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by anthony1 on 9/1/17.
 */

public class SharedLayoutLogic
{
    private ArrayList<String> unit;
    private ArrayList<Long> unitID;
    private ArrayList<Double> unitPrice;
    private String category,itemName;
    private long itemID;
    private double currentQuantity;

    public SharedLayoutLogic()
    {
        unit = new ArrayList<>();
        unitID = new ArrayList<>();
        unitPrice = new ArrayList<>();
    }

    public JSONArray getSuggestions(String text)
    {
        JSONArray result = null;

        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName","item");
            object.put("columnArgs",new String[]{"category","item","item.id"});
            object.put("join",UtilityClass.getJoinQuery("category","category_id=category.id"));
            object.put("whereArgs","item like '"+text+"%' and item.archived='0'");
            result = DatabaseManager.fetchData(object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public void retrievePriceAndQuantityDetails(long itemID)
    {
        try
        {
            this.itemID = itemID;
            JSONObject object = new JSONObject();
            object.put("tableName","unit");
            object.put("columnArgs",new String[]{"category","item","unit","unit.id","retail_price"});
            object.put("whereArgs","unit.item_id="+itemID);
            String join1 = UtilityClass.getJoinQuery("item","item_id=item.id");
            String join2 = UtilityClass.getJoinQuery("category","category_id=category.id");
            String j = join1+" "+join2;
            object.put("join",j);
            JSONArray result = DatabaseManager.fetchData(object);
            currentQuantity = DatabaseManager.retrieveCurrentQuantity(itemID);

            //clear the array lists
            unit.clear();
            unitID.clear();
            unitPrice.clear();

            object = result.getJSONObject(0);
            category = object.getString("category");
            itemName = object.getString("item");

            //strip the jsonarray
            for(int i=0; i<result.length();i++)
            {
                object = result.getJSONObject(i);

                unit.add(object.getString("unit"));
                unitID.add(object.getLong("id"));
                unitPrice.add(object.getDouble("retail_price"));

            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public double getUnitPrice(int pos)
    {
        return unitPrice.get(pos);
    }

    public String[] getUnits()
    {
        String [] units = new String[unit.size()];
        return unit.toArray(units);
    }

    public String getUnit(int pos)
    {
        return unit.get(pos);
    }

    public long getUnitID(int pos)
    {
        return unitID.get(pos);
    }

    public String getCategory()
    {
        return category;
    }

    public String getItemName()
    {
        return itemName;
    }

    public long getItemID()
    {
        return itemID;
    }

    public double getCurrentQuantity()
    {
        return currentQuantity;
    }
}
