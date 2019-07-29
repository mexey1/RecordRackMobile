package com.geckosolutions.recordrack.logic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anthony1 on 9/30/16.
 */
public class IncomeFragmentLogic
{
    public static double getTotalIncome(int datePosition)
    {
        String min = null, max = null, whereArgs = null;
        min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
        max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
        whereArgs = "archived='0' AND last_edited >='"+min+"' AND last_edited<='"+max+"'";
        return DatabaseManager.sumUpRowsWithWhere("income","amount",whereArgs);
    }

    /**
     * this method is called to save a new income entry.
     * @param name name of client/customer who made the payment
     * @param purpose what is the payment for
     * @param amount the amount paid
     */
    public long insertIncomeEntry(String tableName,String name, String purpose, double amount)
    {
        long id = -1;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName", tableName);
            object.put("name", name);
            object.put("purpose", purpose);
            object.put("amount", amount);
            object.put("created", UtilityClass.getDateTime());
            object.put("currency", "NGN");
            object.put("last_edited", UtilityClass.getDateTime());
            object.put("user_id", UtilityClass.getCurrentUserID());
            id = DatabaseManager.insertData(object);
            object = null;
            //display the newly added income entry
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return id;
    }
}
