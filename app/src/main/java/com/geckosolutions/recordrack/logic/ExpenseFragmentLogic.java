package com.geckosolutions.recordrack.logic;

/**
 * Created by anthony1 on 9/30/16.
 */
public class ExpenseFragmentLogic
{
    public static double getTotalExpense(int datePosition)
    {
        String min = null, max = null, whereArgs = null;
        min = UtilityClass.getDateTime(datePosition, 0, 0, 0);
        max = UtilityClass.getDateTime(datePosition, 23, 59, 59);
        whereArgs = "archived='0' AND last_edited >='"+min+"' AND last_edited<='"+max+"'";
        return DatabaseManager.sumUpRowsWithWhere("expense","amount",whereArgs);
    }
}
