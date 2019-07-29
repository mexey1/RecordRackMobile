package com.geckosolutions.recordrack.logic;

import android.util.Log;

import com.geckosolutions.recordrack.interfaces.ProgressInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 9/15/17.
 */

public class Settings
{
    private static String name;
    private static int pos =1;
    private static byte  shouldPrint=-1;
    private static String tables [] = {"business_details","user","category","client","credit_payment","credit_transaction","creditor",
                                            "current_quantity","customer","db_info", "debt_payment", "debt_transaction", "debtor", "expense_purpose", "expense",
                                            "income", "income_purpose", "initial_quantity", "item", "last_used_date_time", "notes", "notifications", "pending_user",
                                            "purchase_item", "purchase_transaction", "sale_item", "sale_transaction", "special_quantity", "unit", "unit_relation",
                                            "vendor", "settings"};
    private static boolean isForceDownload;
    private static ProgressInterface progressInterface;
    private static String TAG="Settings";

    public static String getPrinterName()
    {
        if(name == null)
        {
            //name = null;
            try
            {
                JSONObject object = new JSONObject();
                object.put("tableName","settings");
                object.put("columnArgs",new String[]{"val"});
                object.put("whereArgs","comp_nm='bluetooth_device_name'");
                JSONArray array = DatabaseManager.fetchData(object);
                if(array.length()>0)
                    name = array.getJSONObject(0).getString("val");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return name;
    }

    public static boolean isPrintingEnabled()
    {
        if(shouldPrint ==-1)
        {
            try
            {
                JSONObject object = new JSONObject();
                object.put("tableName", "settings");
                object.put("columnArgs", new String[]{"val"});
                object.put("whereArgs", "comp_nm='printing_enabled'");
                JSONArray array = DatabaseManager.fetchData(object);
                if(array.length() > 0)
                    shouldPrint = (byte) array.getJSONObject(0).getInt("val");
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return shouldPrint ==1;
    }

    public static void setPrintingEnabled()
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName", "settings");
            object.put("whereArgs","comp_nm='printing_enabled'");
            JSONArray array = DatabaseManager.fetchData(object);
            if(array.length() == 0)//go ahead and insert
            {
                object.remove("whereArgs");
                object.put("comp_nm", "printing_enabled");
                object.put("val", "1");
                DatabaseManager.insertData(object);
            }
            else
            {
                object.remove("whereArgs");
                object.put("val", "1");
                DatabaseManager.updateData(object,"comp_nm='printing_enabled'");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public static void disablePrinting()
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName", "settings");
            object.put("val", "0");
            DatabaseManager.updateData(object,"comp_nm='printing_enabled'");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public static void setPrinterName(String deviceName)
    {
        name = deviceName;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName", "settings");
            object.put("comp_nm", "bluetooth_device_name");
            object.put("val", ""+deviceName+"");
            DatabaseManager.insertData(object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * method to determine if the a description dialog should show. This dialog shows up when user
     * selects an item from the drawer.
     * @param activity the item selected from the drawer. Could be one of dashboard, purchases, sales etc
     * @return true, show dialog or false, do not show.
     */
    public static boolean shouldShowDescriptionDialog(String activity)
    {
        boolean result = false;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName","settings");
            object.put("columns",new String[]{"val"});

            if(activity.equalsIgnoreCase("dashboard"))
                object.put("whereArgs","comp_nm='showdashboarddialog'");
            else if(activity.equalsIgnoreCase("sales"))
                object.put("whereArgs","comp_nm='showsalesdialog'");
            else if(activity.equalsIgnoreCase("purchases"))
                object.put("whereArgs","comp_nm='showpurchasesdialog'");
            else if(activity.equalsIgnoreCase("stock"))
                object.put("whereArgs","comp_nm='showstockdialog'");
            else if(activity.equalsIgnoreCase("pricelist"))
                object.put("whereArgs","comp_nm='showpricelistdialog'");
            else if(activity.equalsIgnoreCase("debtors"))
                object.put("whereArgs","comp_nm='showdebtorsdialog'");
            else if(activity.equalsIgnoreCase("income"))
                object.put("whereArgs","comp_nm='showincomedialog'");
            else if(activity.equalsIgnoreCase("expenses"))
                object.put("whereArgs","comp_nm='showexpensesdialog'");
            else if(activity.equalsIgnoreCase("settings"))
                object.put("whereArgs","comp_nm='showsettingsdialog'");

            JSONArray array = DatabaseManager.fetchData(object);
            if(array.length() == 0)//hasn't been viewed, so we need to show the user
                result = true;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }



    /**
     * once the dialog is seen and user selects "got it", we should note this and not display it again.
     * @param activity  the item that has been seen. Dashboard, sales, purchases etc
     */
    public static void setShouldShowDescriptionDialog(String activity)
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName","settings");


            if(activity.equalsIgnoreCase("dashboard"))
                object.put("comp_nm","showdashboarddialog");
            else if(activity.equalsIgnoreCase("sales"))
                object.put("comp_nm","showsalesdialog");
            else if(activity.equalsIgnoreCase("purchases"))
                object.put("comp_nm","showpurchasesdialog");
            else if(activity.equalsIgnoreCase("stock"))
                object.put("comp_nm","showstockdialog");
            else if(activity.equalsIgnoreCase("pricelist"))
                object.put("comp_nm","showpricelistdialog");
            else if(activity.equalsIgnoreCase("debtors"))
                object.put("comp_nm","showdebtorsdialog");
            else if(activity.equalsIgnoreCase("income"))
                object.put("comp_nm","showincomedialog");
            else if(activity.equalsIgnoreCase("expenses"))
                object.put("comp_nm","showexpensesdialog");
            else if(activity.equalsIgnoreCase("settings"))
                object.put("comp_nm","showsettingsdialog");

            object.put("val","false");//since it has been seen, we should set it to false
            DatabaseManager.insertData(object);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * merthod called to begin uploading all data from the mobile device to the server
     * @param pi a reference to the progressinterface implemented by the caller
     */
    public static void beginUpload(ProgressInterface pi)
    {
        Logger.log(TAG,"begin upload called");
        progressInterface = null;
        progressInterface = pi;
        progressInterface.setMax(tables.length);
        pos = 1;
        //it's 3Am and I'm so tired. So I'd just go ahead and upload data from the database thread
        NetworkThread.getNetworkThread().postNetworkTask(new Runnable()
        {
            @Override
            public void run()
            {
                if(DataUploadClass.isInternetConnectivityAvailable())
                {
                    Logger.log(TAG,"Internet access is available");
                    //tell the server we are about to sync data. This would cause the server to delete
                    //all data currently residing there
                    boolean res = DataUploadClass.initiateDataUpload();
                    Logger.log(TAG,"initiate data upload response: "+res);
                    if(res)
                        fetchDataFromLocalDatabase();
                    else
                    {
                        Logger.log(TAG,"An error occurred while processing your request");
                        UtilityClass.showToast("An error occurred while processing your request");
                        progressInterface.dismissDialog();
                    }
                }
                else //if no internet access, we'd close the dialog and notify the user
                {
                    Logger.log(TAG,"No internet connection");
                    UtilityClass.showToast("No internet connection");
                    progressInterface.dismissDialog();
                }
            }
        });
    }

    /**
     * this method is called to fetch data from the local database. it is called from beginUpload
     * method.
     */
    private static void fetchDataFromLocalDatabase()
    {
        Logger.log(TAG,"fetchDataFromLocalDatabase method called");
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                JSONObject object = null;
                try
                {
                    //loop through each table, picking beginUpload() the data
                    for (int i=0; i<tables.length; i++)
                    {
                        object = new JSONObject();
                        object.put("tableName",tables[i]);
                        JSONArray array = DatabaseManager.fetchData(object);
                        object = new JSONObject();
                        object.put("dbName",UtilityClass.getRackID());
                        object.put("tableName",tables[i]);
                        object.put("array",array.toString());

                        Logger.log(TAG,"json prepared, about uploading to server");
                        Log.d(TAG,"json prepared, about uploading to server");
                        uploadToServer(object);
                    }
                    //delete everything from the queries table
                    DatabaseManager.deleteData("queries");

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * method to upload the data fetched from fetchDataFromLocalDatabase
     * @param object a json object containing the data to send to the server
     */
    private static void uploadToServer(final JSONObject object)
    {

        NetworkThread.getNetworkThread().postNetworkTask(new Runnable()
        {
            @Override
            public void run()
            {
                Logger.log(TAG,"DataUploadClass.forceUpload method called");
                DataUploadClass.forceUpload(object);
                progressInterface.setProgress(++pos);
                if(pos > tables.length)
                    progressInterface.dismissDialog();
            }
        });
    }

    public static boolean beginDownload(ProgressInterface pi)
    {
        progressInterface = null;
        progressInterface = pi;
        isForceDownload = true;
        pos = 1;
        if(DataUploadClass.isInternetConnectivityAvailable())
            clearQueriesTable();
        else
        {
            Logger.log(TAG,"No internet connection");
            UtilityClass.showToast("No internet connection");
            progressInterface.dismissDialog();
        }
        return false;
    }

    private static void clearQueriesTable()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
            @Override
            public void run()
            {
                DatabaseManager.deleteData("queries");
                //retrieve data from the server for a particular database and table
                NetworkThread.getNetworkThread().postNetworkTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            progressInterface.setMax(tables.length);
                            for (int i=0;i<tables.length;i++)
                            {
                                JSONObject object = new JSONObject();
                                object.put("dbName","PLVTV1CFX1");
                                object.put("tableName",tables[i]);
                                Logger.log(TAG,"About calling force download ");
                                Log.d(TAG,"About calling force download ");
                                String response = DataUploadClass.forceDownload(object);
                                Logger.log(TAG,"server response "+response);
                                Log.d(TAG,"Server response"+response);
                                insertIntoDatabase(response);
                            }
                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * this method is called to insert data downloaded from the server into the local database
     * @param result
     */
    private static void insertIntoDatabase(final String result)
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(result.length() > 0)
                    {
                        Logger.log(TAG,"Insert to database called");
                        JSONObject object = new JSONObject(result);
                        JSONArray array = new JSONArray(object.getString("array"));
                        String tableName = object.getString("tableName");
                        Logger.log(TAG,"Deleting data from table "+tableName);
                        Log.d(TAG,"Deleting data from table "+tableName);
                        //let's delete everything from the current database
                        DatabaseManager.deleteData(tableName);
                        Logger.log(TAG,"Recreating table "+tableName);
                        for(int i=0; i<array.length();i++)
                        {
                            object = array.getJSONObject(i);
                            object.put("tableName",tableName);
                            DatabaseManager.insertData(object);
                        }
                        Logger.log(TAG,"Recreation completed");
                        progressInterface.setProgress(++pos);
                        Log.d("POS",new Integer(pos).toString());
                        if (pos > tables.length)
                        {
                            progressInterface.dismissDialog();
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
