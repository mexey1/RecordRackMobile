package com.geckosolutions.recordrack.logic;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by anthony1 on 7/1/17.
 */

public class DataUploadClass
{
    private static String serverAddress = "http://13.57.79.121:8080/";
    private static int PORT = 8080;
    private static int CONNECTION_TIMEOUT = 3000;//300 MILLISECONDS
    private static byte data [] = new byte[8*1024];
    private static final String TAG = "DataUploadClass";

    public static void uploadToServer(JSONObject object)
    {
        try
        {
            Logger.log(TAG,"data was sent here");
            Log.d(TAG,"data was sent here");
            StringBuilder builder = null;
            object.put("dbName",UtilityClass.getRackID());
            //builder.append("dbName="+object.toString());
            HttpURLConnection connection = getConnection("RecordRackWeb/submit");
            Tuples<String,String> serverResponse = sendData(connection,object);
            //if for some reason, saving data on server failed, we'd like to retry later
            //or an error occurred during sending data to server
            Logger.log(TAG,"response from server: "+(serverResponse==null?"no response from server":serverResponse.getFirst()));
            Log.d(TAG,"response from server: "+(serverResponse==null?"no response from server":serverResponse.getFirst()));
            //if we try to submit data to server & error occurs, log the data in db
            if(serverResponse == null || !serverResponse.getFirst().equals("200"))
            {
                final JSONObject object1 = new JSONObject();
                object1.put("tableName","queries");
                object1.put("queryString",object);
                object1.put("last_attempted_ts",UtilityClass.getDateTime());
                object1.put("sent_to_server","0");
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            DatabaseManager.insertData(object1);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                            Logger.writeException(e);
                            //e.printStackTrace();
                        }
                    }
                });
            }
            //if we succeed, check if we have pending data in db & send to server
            else
            {
                if(UtilityClass.shouldRetryDataUpload())
                    retryDataUpload();
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
            //e.printStackTrace();
        }
    }

    public static Tuples emailAddressValidationCodeRequest(String message)
    {
        Tuples<String,String> serverResponse = null;
        try
        {
            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection = getConnection("RecordRackWeb/email_verification");

            JSONObject object = new JSONObject();
            object.put("action","request");
            object.put("email",message);
            serverResponse = sendData(connection,object);
        }

        catch (JSONException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
            //e.printStackTrace();
        }
        return serverResponse;
    }

    public static Tuples submitValidationCode(String email, String code)
    {
        Tuples<String,String > serverResponse = null;
        try
        {
            StringBuilder builder;
            HttpURLConnection connection = getConnection("RecordRackWeb/email_verification");
            Logger.log(TAG,connection.toString());
            JSONObject object = new JSONObject();
            object.put("action","submit");
            object.put("email",email);
            object.put("code",code);

            if(connection == null)
            {
                serverResponse = new Tuples<>();
                serverResponse.setValues("201","No internet connection");
            }
            else
                serverResponse = sendData(connection,object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
            //e.printStackTrace();
        }
        return serverResponse;
    }

    public static Tuples registerStoreWithServer(String email)
    {
        Tuples<String,String > serverResponse = null;
        try
        {
            StringBuilder builder;
            HttpURLConnection connection = getConnection("RecordRackWeb/store_registration");
            JSONObject object = new JSONObject();
            object.put("action","submit");
            object.put("email",email);
            serverResponse = sendData(connection,object);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
            //e.printStackTrace();
        }

        return serverResponse;
    }

    private static HttpURLConnection getConnection(String path)
    {
        Logger.log(TAG,"Url is: "+path);
        Log.d(TAG,"Url is: "+path);
        HttpURLConnection connection = null;
        try
        {
            //only attempt to connect if internet connection is available
            if(isInternetConnectivityAvailable())
            {
                String urlPath = serverAddress+path;
                Logger.log(TAG,"Internet connection is available");
                Log.d(TAG,"Internet connection is available");
                URL url = new URL(urlPath);
                connection = (HttpURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setRequestMethod("POST");
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
        }
        catch (SocketTimeoutException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
            UtilityClass.showToast("Connection timed out. Please try again");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
        }
        return connection;
    }

    private static Tuples sendData(HttpURLConnection connection,JSONObject object)
    {
        Tuples<String,String> serverResponse = null;
        //int i = 0;
        if(isInternetConnectivityAvailable() && connection!=null && object!=null)
        {
            try
            {
                StringBuilder builder = null;
                Logger.log(TAG,"Connection edattedddde "+connection.toString()+" "+Thread.currentThread().getName());
                Log.d(TAG,"Connection edattedddde "+connection.toString()+" "+Thread.currentThread().getName());

                //i = i/0;
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                Logger.log(TAG,"About sending data to server");
                Log.d(TAG,"About sending data to server");
                outputStream.writeBytes(object.toString());
                Logger.log(TAG,"Data sent to server");
                Log.d(TAG,"Data sent to server");

                //read response from server
                DataInputStream inputStream = new DataInputStream(connection.getInputStream());
                Logger.log(TAG,"Response read from server");
                Log.d(TAG,"Response read from server");


                int read = 0;
                builder = new StringBuilder();
                while((read = inputStream.read(data,0,data.length)) != -1)
                    builder.append(new String(data,0,read));

                Logger.log(TAG,"Server response:"+builder.toString());
                Log.d(TAG,"Server response:"+builder.toString());
                object = new JSONObject(builder.toString().trim());

                serverResponse = new Tuples<>();
                serverResponse.setValues(object.getString("resp_cd"),object.getString("resp_msg"));

                inputStream.close();
                outputStream.close();
            }
            catch (SocketTimeoutException e)
            {
                e.printStackTrace();
                Logger.writeException(e);
                UtilityClass.showToast("Connection timed out. Please try again");
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Logger.log(TAG,"Exception occurred, please see log");
                Logger.writeException(e);
                //e.printStackTrace();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Logger.log(TAG,"Exception occurred, please see log");
                Logger.writeException(e);
                //Logger.writeException(e);
                Log.d(TAG,"Exception was written");
                //e.printStackTrace();
            }
        }
        else
        {
            Log.d(TAG,"Internet Connectivity:"+isInternetConnectivityAvailable()+" connection: "+connection+" json"+object);
            Logger.log(TAG, "Internet Connectivity:" + isInternetConnectivityAvailable() + " connection: " + connection + " json" + object);
        }

        Logger.log(TAG,"Returning server response "+serverResponse);
        Log.d(TAG,"Returning server response "+serverResponse);
        return serverResponse;
    }

    public static void retryDataUpload()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                //first, fetch all data in queries table with sent_to_server = 0
                try
                {
                    Logger.log(TAG,"Retrying data upload");
                    Log.d(TAG,"Retrying data upload");
                    JSONObject object = new JSONObject();
                    object.put("tableName","queries");
                    object.put("columnArgs",new String[]{"id","queryString"});
                    object.put("whereArgs","sent_to_server=0");
                    JSONArray array = DatabaseManager.fetchData(object);

                    int i = 0;
                    while (i<array.length())
                    {
                        Logger.log(TAG,"Message :"+(i+1));
                        Log.d(TAG,"Message :"+(i+1));
                        HttpURLConnection connection = getConnection("RecordRackWeb/submit");
                        object = array.getJSONObject(i);
                        long id = object.getLong("id");
                        object = new JSONObject(object.getString("queryString"));
                        Tuples<String,String> serverResponse = sendData(connection,object);
                        Logger.log(TAG,"Response code :"+ serverResponse.getFirst());
                        Log.d(TAG,"Response code :"+ serverResponse.getFirst());
                        Logger.log(TAG,"Response message :"+ serverResponse.getSecond());
                        Log.d(TAG,"Response message :"+ serverResponse.getSecond());
                        //successfully sent to server and data was stored properly
                        if(serverResponse!=null && serverResponse.getFirst().equals("200"))
                        {
                            object = new JSONObject();
                            object.put("tableName","queries");
                            object.put("sent_to_server","1");
                            DatabaseManager.updateData(object,"id="+id);
                        }
                        i++;
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Logger.log(TAG,"Exception occurred, please see log");
                    Log.d(TAG,"Exception occurred, please see log");
                    Logger.writeException(e);
                    //e.printStackTrace();
                }
            }
        });
    }

    public static void uploadExceptionToServer(String rackID,String file, String data)
    {
        try
        {
            Logger.log(TAG,"In here testing for thread");
            Log.d(TAG,"In here testing for thread");
            JSONObject object = new JSONObject();
            object.put("file",file);
            object.put("data",data);
            object.put("rackID",rackID);
            Logger.log(TAG,"Data to be uploaded: "+object);
            Log.d(TAG,"Data to be uploaded: "+object);
            HttpURLConnection connection = getConnection("RecordRackWeb/exception");

            Logger.log(TAG,"Connection created");
            Log.d(TAG,"Connection created");
            sendData(connection,object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
        }
    }

    /**
     * this is called from teh settings class to tell the server we are about to begin uploading
     * data to it
     * @return boolean telling the caller if it's ok to proceed with the force upload
     */
    public static boolean initiateDataUpload()
    {
        Logger.log(TAG,"initiate data uploadd called");
        Log.d(TAG,"initiate data uploadd called");
        Tuples tuples = null;
        boolean result = false;
        try
        {
            JSONObject object = new JSONObject();
            object.put("sync",true);
            object.put("dbName",UtilityClass.getRackID());
            HttpURLConnection connection = getConnection("RecordRackWeb/sync_with_client");
            tuples = sendData(connection,object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Logger.writeException(e);
        }

        if(tuples ==null)
            result = false;
        else
            result=tuples.getFirst().equals("200");
        return result;
    }

    /**
     * this method is called from the settings class. It basically tries to upload the contents
     * from each table to the server
     * @param object JSON containing the database name, table name and data to be inserted
     */
    public static void forceUpload(JSONObject object)
    {
        HttpURLConnection connection = getConnection("RecordRackWeb/sync_with_client");
        sendData(connection,object);
    }

    /**
     * this method is called from settings class. It downloads data from a specific database and
     * table from the server.
     * @param object JSON containing tableName and database name
     * @return returns the response from the server in the form of a string
     */
    public static String forceDownload(JSONObject object)
    {
        HttpURLConnection connection = getConnection("RecordRackWeb/download_to_client");
        Tuples<String,String> result = sendData(connection,object);
        return result.getSecond();
    }

    /**
     * method to detect if there's network connectivity
     * @return returns true if there's or false if there isn't
     */
    public static boolean isInternetConnectivityAvailable()
    {
        boolean result = false;
        ConnectivityManager manager = (ConnectivityManager) UtilityClass.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
            result= true;
        return result;
    }
}
