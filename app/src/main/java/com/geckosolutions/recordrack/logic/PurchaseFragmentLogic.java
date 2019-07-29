package com.geckosolutions.recordrack.logic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anthony1 on 7/9/17.
 */

public class PurchaseFragmentLogic
{
    public static double getTotalExpense(int datePosition)
    {
        String min = null, max = null, whereArgs = null;
        min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
        max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
        whereArgs = "archived='0' AND ded_frm_rev=1 AND last_edited >='"+min+"' AND last_edited<='"+max+"'";
        return DatabaseManager.sumUpRowsWithWhere("purchase_transaction","amount_paid",whereArgs);
    }

    public static JSONArray getPurchasesForDate(int datePosition)
    {
        JSONArray array = null;
        try
        {
            String whereArgs,min,max;
            min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
            max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
            whereArgs = "archived = 0 AND last_edited BETWEEN '"+min+"' AND '"+max+"'";
            JSONObject object = new JSONObject();

            object.put("tableName","purchase_transaction");
            object.put("columnArgs",new String[]{"id as purchase_transaction_id","name","amount_paid","last_edited","suspended"});
            object.put("whereArgs",whereArgs);

            array = DatabaseManager.fetchData(object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return array;
    }
}
