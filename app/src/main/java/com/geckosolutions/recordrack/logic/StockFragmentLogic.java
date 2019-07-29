package com.geckosolutions.recordrack.logic;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anthony1 on 7/13/16.
 */
public class StockFragmentLogic
{
    private static String [] categoryColumns = {"id","category"}, itemColumns = {"id","item"};
    private static String [] quantityColumns = {"quantity"}, unitColumns = {"unit","retail_price"};
    private static String [] openingStockColumns = {"quantity"};
    private final static String TAG = "StockFragmentLogic";

    private static int position;

    /**
     * this obtains the categories and all items related to each category.
     * @param getQuantityData if this is true, it returns quantity related data for each item
     * @return json array containing the stock list/and quantity data
     */
    public static JSONArray getStockList(boolean getQuantityData)
    {
        JSONArray result = new JSONArray();
        try
        {
            JSONObject data = new JSONObject();
            data.put("columnArgs",categoryColumns);
            data.put("whereArgs", "");
            data.put("tableName", "category");
            data.put("extra","ORDER BY CATEGORY ASC");
            JSONArray categories = DatabaseManager.fetchData(data);//this fetches all categories
            Log.d(TAG,"categories "+categories);
            Logger.log(TAG,"categories "+categories);
            result = new JSONArray();//this holds the results to display
            for (int count = 0; count<categories.length(); count++)
            {
                result.put(categories.getJSONObject(count));
                /**
                 * for each category, fetch all its items
                 */
                data = null;
                data = new JSONObject();
                data.put("columnArgs",itemColumns);
                data.put("whereArgs"," category_id = '"+categories.getJSONObject(count).getString("id")+"' AND archived=0");
                data.put("tableName", "item");
                data.put("extra","ORDER BY item ASC");
                JSONArray items = DatabaseManager.fetchData(data);//this fetches all items associated with the category
                Logger.log(TAG,"items "+items);
                Log.d(TAG,"items "+items);
                String itemName = null;
                String itemID = null;
                for(int loop = 0; loop<items.length(); loop++)
                {
                    JSONObject itemObject = items.getJSONObject(loop);
                    if(getQuantityData)
                    {
                        itemName = itemObject.getString("item");
                        itemID = itemObject.getString("id");
                        result.put(getItemData(itemName, itemID, 0));
                    }
                    else
                        result.put(itemObject);
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
     * this method fetches intitial quantity, quantity sold and current quantity data from the
     * database. The result is returned as a JSONObject.
     * @return JSONObject containing stock related information about an item.
     * @param itemName the name of the item being fetched
     * @param itemID itemID of the item
     * @param datePosition how far behind or ahead from today
     * @return
     */
    public static JSONObject getItemData(String itemName, String itemID, int datePosition)
    {
        JSONObject data = null;
        String min = null, max = null, whereArgs = null;
        double initialQuantity, quantitySold, purchaseQuantity, total;
        try
        {
            //get current quantity
            data = null;
            data = new JSONObject();
            data.put("columnArgs",quantityColumns);
            data.put("whereArgs","item_id = '"+itemID+"'");
            data.put("tableName","current_quantity");
            JSONArray quantity = DatabaseManager.fetchData(data);
            Logger.log(TAG,"quantity "+quantity);
            Log.d(TAG,"quantity "+quantity);

            //get item unit and price
            /*data = null;
            data = new JSONObject();
            data.put("columnArgs",unitColumns);
            data.put("whereArgs","id = '"+quantity.getJSONObject(0).getString("unit_id")+"'");
            data.put("tableName","unit");
            JSONArray unit = DatabaseManager.fetchData(data);*/

            //get opening stock
            data = null;
            data = new JSONObject();
            data.put("columnArgs",openingStockColumns);

            min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
            max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
            whereArgs = "item_id = '"+itemID+"' AND last_edited >='"+min+"' AND last_edited<='"+max+"'";
            data.put("whereArgs",whereArgs);
            data.put("tableName","initial_quantity");
            JSONArray initial = DatabaseManager.fetchData(data);
            initialQuantity = initial.getJSONObject(0).getDouble("quantity");
            Logger.log(TAG,"initial quantity "+initialQuantity);
            Log.d(TAG,"initial quantity "+initialQuantity);

            //retrieve total quantity sold for the given item
            whereArgs = null;
            min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
            max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
            whereArgs = " sale_transaction_id = sale_transaction.id AND item_id = '"+itemID+"' AND " +
                        "sale_transaction.suspended='0' AND sale_transaction.archived= '0' AND " + "sale_item.last_edited >='"+
                        min+"' AND sale_item.last_edited<='"+max+"'";
            whereArgs = UtilityClass.getJoinQuery("sale_transaction",whereArgs);
            quantitySold = DatabaseManager.sumUpRowsWithJoin("sale_item","_quantity",whereArgs);
            Logger.log(TAG,"quantity sold "+quantitySold);
            Log.d(TAG,"quantity sold "+quantitySold);
            //retrieve purchases made for that day
            min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
            max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
            whereArgs = "item_id = '"+itemID+"' AND last_edited >='"+min+"' AND last_edited<='"+max+"' AND archived=0";
            //JSONArray purchase = DatabaseManager.sumUpRowsWithWhere("purchase_item","_quantity",whereArgs);
                    //DatabaseManager.fetchData(data);
            //purchaseQuantity = purchase.length()==0?0:purchase.getJSONObject(0).getDouble("quantity");
            purchaseQuantity = DatabaseManager.sumUpRowsWithWhere("purchase_item","_quantity",whereArgs);
            Logger.log(TAG,"purchase "+purchaseQuantity);
            Log.d(TAG,"purchase "+purchaseQuantity);

            //prepare data, tagging them accordingly
            data = null;
            data = new JSONObject();
            data.put("item",itemName);
            data.put("item_id",itemID);
            data.put("initialQuantity",breakDown(initialQuantity,itemID));
            data.put("quantitySold",breakDown(quantitySold,itemID));
            total = initialQuantity-quantitySold+purchaseQuantity;
            Logger.log(TAG,"total: "+total);
            Log.d(TAG,"total: "+total);

            data.put("quantity",breakDown(total,itemID));//quantity.getJSONObject(0).getString("quantity")
            //data.put("unit",unit.getJSONObject(0).getString("unit"));
            //data.put("cost",unit.getJSONObject(0).getString("retail_price"));
            data.put("purchase",breakDown(purchaseQuantity,itemID));

            quantity = null;
            //unit = null;
            initial = null;

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * this method is called to breakdown a given quantity(in its base unit) into its separate
     * quantity components. E.g if we pass in 500, it breaks it down into 50 cartons, 10 packs etc.
     */
    public static String breakDown(double total, String itemID)
    {
        Tuples[] results = DatabaseManager.getBaseUnitEquivalents(Long.parseLong(itemID));
        StringBuilder stringBuilder = new StringBuilder();
        if (results.length > 1)
        {
            for (Tuples<String,Integer> t : results)
            {
                Logger.log(TAG,"Unit: "+t.getFirst()+" Second: "+t.getSecond());
                Log.d(TAG,"Unit: "+t.getFirst()+" Second: "+t.getSecond());
                int wholeNumber = (int)(total/t.getSecond());
                if(wholeNumber > 0)
                {
                    if(stringBuilder.length() > 0)
                        stringBuilder.append(", ");
                    stringBuilder.append(wholeNumber + " " + "" + t.getFirst());
                }
                if((total - wholeNumber) == 0)
                    break;
                total = total%t.getSecond();

            }
        }
        else
            stringBuilder.append(total+""+results[0].getFirst());
        return stringBuilder.toString();
    }
}
