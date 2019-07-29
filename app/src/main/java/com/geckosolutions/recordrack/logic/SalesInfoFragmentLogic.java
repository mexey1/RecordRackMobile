package com.geckosolutions.recordrack.logic;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * Created by anthony1 on 7/15/16.
 *
 */
public class SalesInfoFragmentLogic
{
    private static String [] categoryColumns = {"id","category"}, itemColumns = {"id","item"};
    private static String [] quantityColumns = {"quantity","unit_id"}, unitColumns = {"unit","retail_price"};
    private static String [] openingStockColumns = {"quantity"};
    private static final String TAG="SalesInfoFragmentLogic";
    //private static int position = 0;
    //private static double sum = 0;

    /**
     * this method is called to retrieve data related to stocks. It obtains the categories and
     * all items related to each category.
     * @param datePosition how far back or ahead the date whose data is to be retrieved is from today
     */
    public static JSONArray getSalesInfoData(int datePosition)
    {
        JSONArray result = new JSONArray();
        //int position = datePosition;
        try
        {
            JSONObject data = new JSONObject();
            data.put("columnArgs",categoryColumns);
            data.put("whereArgs", "");
            data.put("tableName", "category");
            JSONArray categories = DatabaseManager.fetchData(data);//this fetches all categories
            Logger.log(TAG,"categories "+categories);
            Log.d(TAG, "categories " + categories);
            result = new JSONArray();//this holds the results to display
            int itemCount = 0;
            for (int count = 0; count<categories.length(); count++)
            {
                itemCount = 0;
                /**
                 * for each category, fetch all its items
                 */
                data = null;
                data = new JSONObject();
                data.put("columnArgs",itemColumns);
                data.put("whereArgs"," category_id = '"+categories.getJSONObject(count).getString("id")+"'");
                data.put("tableName", "item");
                JSONArray items = DatabaseManager.fetchData(data);//this fetches all items associated with the category
                Logger.log(TAG,"items "+items);
                Log.d(TAG,"items "+items);
                String itemName = null;
                String itemID = null;
                for(int loop = 0; loop<items.length(); loop++)
                {
                    itemName = items.getJSONObject(loop).getString("item");
                    itemID = items.getJSONObject(loop).getString("id");
                    JSONObject object = getItemSalesValue(datePosition,itemName,itemID);
                    if(object != null)
                    {
                        if(itemCount == 0)
                            result.put(categories.getJSONObject(count));
                        result.put(object);
                        itemCount++;
                    }
                    object = null;
                }

                items = null;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * this method sums up and returns the total sales value for a given item on a given day
     * @param position the date position relative to today
     * @param itemID item id for the given item
     * @param itemName item name for the given item
     * @return
     */
    public static JSONObject getItemSalesValue(int position, String itemName,String itemID)
    {
        JSONObject object = new JSONObject();
        try
        {
            String whereArgs = null;
            String min=null, max=null;
            //retrieve sales value for the given item
            whereArgs = null;
            min = UtilityClass.getDateTime(position, 0, 0, 0);
            max = UtilityClass.getDateTime(position, 23, 59, 59);
            whereArgs = "sale_transaction_id=sale_transaction.id WHERE item_id = '"+itemID+"' AND sale_item.last_edited >='"+min+"' " +
                        "AND sale_item.last_edited<='"+max+"' AND sale_transaction.suspended='0' AND sale_transaction.archived='0'";
            //call the sale join query to add the join command and make the suspended column available
            whereArgs = UtilityClass.getJoinQuery("sale_item",whereArgs);
            double total = DatabaseManager.sumUpRowsWithJoin("sale_transaction", "sale_item.cost", whereArgs);
            if(total == 0)
                object = null;
            else
            {
                //sum+=total;
                object.put("item", itemName);
                object.put("total", total);
                object.put("item_id", itemID);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * convenience method to check if sales have been performed today.
     * @param datePosition how far back or ahead the date whose data is to be retrieved is from today
     * @return
     */
    public static boolean areSalesAvailable(int datePosition)
    {
        boolean result = false;
        try
        {
            String startDate = null, endDate = null, whereArgs = null;
            startDate = UtilityClass.getDateTime(datePosition,0,0,0);
            endDate = UtilityClass.getDateTime(datePosition,23,59,59);
            whereArgs = "last_edited >= '"+startDate+"' AND last_edited <= " +
                    "'"+endDate+"' AND suspended='0' AND archived = '0'";
            JSONObject object = new JSONObject();
            String columns[] = {"name"};
            object.put("columnArgs",columns);
            object.put("tableName", "sale_transaction");
            object.put("whereArgs", whereArgs);
            JSONArray response = DatabaseManager.fetchData(object);

            result = response.length()>0;
            object = null;
            response = null;
            columns = null;
            endDate = null;
            startDate = null;
            whereArgs = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
            return result;
    }

    /**
     * method to get the sum of money obtained from the sale of the item specified by item ID
     * @param itemID ID of the item to sum up
     * @param start the start date from today
     * @param end the end date from today
     * @return a double array containing the values. The eight value is the maximum.
     */
    public static double[] getSalesForWeek(String itemID, int start, int end)
    {
        double [] totals = new double[8];
        String min = null, max = null, whereArgs = null;
        double maxValue = 0;
        int pos = 0;
        for(int count = start; count<=end;count++)
        {
            min = UtilityClass.getDateTime(count,0,0,0);
            max = UtilityClass.getDateTime(count,23,59,59);
            whereArgs = "sale_transaction_id=sale_transaction.id AND sale_item.last_edited >='"+min+"' " +
                        "AND sale_item.last_edited<='"+max+"' " +"AND item_id='"+itemID+"' AND " +
                    "sale_transaction.suspended = '0' AND sale_transaction.archived = '0'";
            whereArgs = UtilityClass.getJoinQuery("sale_item",whereArgs);
            totals[pos] = DatabaseManager.sumUpRowsWithJoin("sale_transaction","cost",whereArgs);
            if(totals[pos] > maxValue)
                maxValue = totals[pos];
            System.out.println(UtilityClass.getDateTime(count) +" "+totals[pos]);
            ++pos;
            whereArgs = null;
            min = null;
            max = null;
        }
        totals[7] = maxValue;
        return totals;
    }

    /**
     * method to retrieve the total sum that was sold today
     * @return
     */
    public static double getSum(int position)
    {
        double sum = 0;
        String min = null, max = null, whereArgs = null;
        min = UtilityClass.getDateTime(position, 0, 0, 0);
        max = UtilityClass.getDateTime(position, 23, 59, 59);
        whereArgs = "archived='0' AND suspended='0' AND last_edited >='"+min+"' AND last_edited<='"+max+"'";
        sum = DatabaseManager.sumUpRowsWithWhere("sale_transaction","amount_paid",whereArgs);

        return sum;
    }
}
