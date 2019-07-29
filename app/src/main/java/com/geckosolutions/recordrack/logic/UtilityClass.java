package com.geckosolutions.recordrack.logic;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Looper;

import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.AccountSetup;
import com.geckosolutions.recordrack.activities.MainActivity;
import com.geckosolutions.recordrack.activities.StoreDetails;
import com.geckosolutions.recordrack.adapters.BluetoothDevicesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * This class holds a reference to the application context as well as an instance to the
 * SegoeUi and AppleGothic typefaces.
 * Created by Anthony on 8/19/15.
 */
public class UtilityClass
{
    private static Context context;
    private static Typeface appleGothic, latoRegular,robotoThin,fontUI, fontAwesome;
    private static String SERVER="http://52.89.10.202:8080/Record_Rack_Web/v1";
    private static Handler handler;
    private static volatile boolean CONTINUE = false;
    private static int dialog_height = LinearLayout.LayoutParams.WRAP_CONTENT;
    private static WeakReference<MainActivity> mainActivityWeakReference;
    private static WeakReference<StoreDetails> storeDetailsWeakReference;
    private static WeakReference<AccountSetup> accountSetupWeakReference;
    private static BluetoothDevicesListAdapter bluetoothDevicesListAdapter;

    public static void setContext(Context cont)
    {
        context = cont;
    }

    /**
     * method to set a reference to the main activity
     * @param reference a weak reference to the activity
     */
    public static void setMainActivityWeakReference(WeakReference<MainActivity> reference)
    {
        mainActivityWeakReference = reference;
    }

    /**
     * method to set a reference to the store detail's activity
     * @param reference a weak reference to the activity
     */
    public static void setStoreDetailsWeakReference(WeakReference<StoreDetails> reference)
    {
        storeDetailsWeakReference = reference;
    }

    /**
     * method to set a reference to the main activity
     * @param reference a weak reference to the activity
     */
    public static void setAccountSetupWeakReference(WeakReference<AccountSetup> reference)
    {
        accountSetupWeakReference = reference;
    }

    /**
     * method called to dismiss the main activity
     */
    public static void dismissMainActivity()
    {
        if(mainActivityWeakReference!=null && mainActivityWeakReference.get() != null)
            mainActivityWeakReference.get().finish();
    }

    /**
     * method called to dismiss the store detail activity
     */
    public static void dissmissStoreDetails()
    {
        if(storeDetailsWeakReference!=null && storeDetailsWeakReference.get() != null)
            storeDetailsWeakReference.get().finish();
    }

    /**
     * method called to dismiss the account setup activity
     */
    public static void dismissAccountSetupActivity()
    {
        if(accountSetupWeakReference!=null && accountSetupWeakReference.get() != null)
            accountSetupWeakReference.get().finish();
    }

    public static Context getContext()
    {
        return context;
    }

    public static Typeface getAppleGothicTypeface()
    {
        if(appleGothic == null)
        {
            appleGothic = Typeface.createFromAsset(context.getAssets(), "fonts/AppleGothic.ttf");
        }
        return appleGothic;
    }

    public static Typeface getLatoRegularTypeface()
    {
        if(latoRegular == null)
        {
            latoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/LatoRegular.ttf");
        }
        return latoRegular;
    }

    public static Typeface getRobotoThinTypeface()
    {
        if(robotoThin == null)
        {
            try
            {
                robotoThin = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
            }
            catch (RuntimeException e)
            {
                robotoThin = Typeface.create("sans-serif-thin",Typeface.NORMAL);
                e.printStackTrace();
            }
        }
        return robotoThin;
    }

    public static Typeface getFlatUITypeface()
    {
        if(fontUI == null)
        {
            fontUI = Typeface.createFromAsset(context.getAssets(),"fonts/Flat-Icons.ttf");
        }

        return  fontUI;
    }

    public static Typeface getFontAwesomeTypeface()
    {
        if(fontAwesome == null)
        {
            fontAwesome = Typeface.createFromAsset(context.getAssets(), "fonts/FontAwesome.ttf");
        }
        return fontAwesome;
    }

    public static String getServer()
    {
        return SERVER;
    }

    public static void showToast(final String text)
    {
        if(handler == null)
            handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable()
        {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static int convertToPixels(int dp)
    {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        UtilityClass.setContext(context.getApplicationContext());
        return (int)((float)dm.density*dp);
    }

    public static String getRackID()
    {
        String rack_id = null;
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        rack_id = preferences.getString("rack_id","null");
        preferences = null;
        return  rack_id;
    }

    public static void saveRackID(String rack_id)
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        preferences.edit().putString("rack_id", rack_id).commit();
        preferences = null;
    }

    public static void setCurrentUserID(int user_id)
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        preferences.edit().putInt("user_id", user_id).commit();
        preferences = null;

    }

    public static void setCurrentUserName(String user)
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        preferences.edit().putString("user", user).commit();
        preferences = null;

    }


    public static String getCurrentUserName()
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        return preferences.getString("user","null");
    }

    public static int getCurrentUserID()
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        return preferences.getInt("user_id",1);
    }

    public static boolean hasUserCompletedStoreDetails()
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        return preferences.getBoolean("store_details",false);
    }

    public static void setStoreDetailsComplete()
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        preferences.edit().putBoolean("store_details", true).commit();
        preferences = null;
    }

    public static boolean hasUserCompletedAccountSetup()
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        return preferences.getBoolean("account_setup", false);
    }

    public static void setAccountSetupComplete()
    {
        SharedPreferences preferences = context.getSharedPreferences("recordrack",Context.MODE_PRIVATE);
        preferences.edit().putBoolean("account_setup", true).commit();
        preferences = null;
    }

    /**
     * this method creates a new dialog.
     * @param layoutForDialog the layout for for the dialog
     * @param activity a weak reference to the activity which owns the dialog
     * @return
     */
    public static Dialog showCustomDialog(int layoutForDialog, WeakReference<Activity> activity)
    {
        if(activity.get() != null)
        {
            Dialog dialog = new Dialog(activity.get());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(layoutForDialog);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.show();
            dialog.getWindow().setAttributes(lp);
            return  dialog;
        }
        else
            return null;
    }

    public static void showCustomDialog(Dialog dialog)
    {
        //Dialog dialog = new Dialog(activity.get());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setContentView(layoutForDialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = UtilityClass.convertToPixels(500);

                //WindowManager.LayoutParams.WRAP_CONTENT;
        //dialog.show();
        dialog.getWindow().setAttributes(lp);
        //return  dialog;
    }

    /**
     * public method to display messages to the user.
     * @param ref a weakreference to the AppCompatActivity
     * @param title the title of the dialog
     * @param text the text to be displayed
     */
    public static void showMessageDialog(WeakReference<Activity> ref, String title, Spanned text)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ref.get());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setIcon(ref.get().getResources().getDrawable(R.drawable.logo));
        builder.setMessage(text);
        builder.show();
    }

    /**
     * this is an overloaded method which requires a third argument, height
     * @param layoutForDialog the dialog to inflate
     * @param activity a weak reference to the activity that owns the dialog
     * @param height the height to set the dialog in dp
     * @return
     */
    public static Dialog showCustomDialog(int layoutForDialog,WeakReference<Activity> activity, double height)
    {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        dialog_height = (int)(dm.heightPixels*height);
        return showCustomDialog(layoutForDialog,activity);
    }


    /**
     * this method creates and returns a progress dialog
     * @param activity a weak reference to the activity that owns the dialog.
     * @return the progress dialog
     */
    public static ProgressDialog getProgressDialog(WeakReference<Activity> activity)
    {
        ProgressDialog dialog = new ProgressDialog(activity.get());
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setIcon(R.drawable.logo);
        return  dialog;
    }

    /**
     * this method creates the layout parameter for the horizontal scroll views used in the app
     * @return params object
     */
    public static LinearLayout.LayoutParams getParamsForHorizontalScrollView()
    {
        WindowManager wm = (WindowManager)UtilityClass.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
        return params;
    }

    /**
     * method to create a layout params with a specific width
     * @param width the width
     * @return
     */
    public static LinearLayout.LayoutParams getParamsForHorizontalScrollView(int width)
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        return params;
    }

    /**
     * this method returns the date and time for insertion into the SQLIte database
     * @return the date and time
     */
    public static String getDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * get the date in the form of Sun, Jul 29.
     * @param position this represents how far back or further from today. Positive numbers for
     *                 future dates, negative numbers for previous dates.
     * @return the date
     */
    public static String getDateTime(int position)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, position);
        return dateFormat.format(cal.getTime());
    }

    /**
     * call this method to calculate the number of days between a given date and today.
     * @param date the date in milliseconds
     * @return the number of days between the specified date and today i.e date - Today
     */
    public static int getDateDifferenceFromToday(long date)
    {
        //Timestamp ts =
        //java.sql.Timestamp timestamp = Timestamp.valueOf(UtilityClass.getDateTime(0,0,0,0));//get today's time in milliseconds
        long today = Calendar.getInstance().getTimeInMillis();
        return getDifferenceBetweenDates(date,today);
    }

    /**
     * call this method to calculate the number of days between a given date and today. It is calculated
     * by date1 - date2
     * @param date1 the first date in milliseconds
     * @param date2 the second date in milliseconds
     * @return the number of days between the specified dates
     */
    public static int getDifferenceBetweenDates(long date1, long date2)
    {
        return (int)((date1 - date2)/(24*60*60*1000));
    }

    /**
     * this method takes a bunch of arguments and returns the formatted date and time. It makes sure
     * the values are 2 characters. E.g if  month value is 1 it changes it to 01 etc.
     * @param year a given year
     * @param month a given month
     * @param day a given day
     * @param hour a given hour
     * @param minute a given minute
     * @param second a given second
     * @return formatted date and time
     */
    public static String getDateTime(int year, int month, int day, int hour, int minute, int second)
    {
        //append 0 to values to make them double digits or characters
        String yr = year < 10?"0"+year:Integer.toString(year);
        String mnt = month < 10?"0"+month:Integer.toString(month);
        String dy = day < 10?"0"+day:Integer.toString(day);
        String hr = hour < 10?"0"+hour:Integer.toString(hour);
        String min = minute < 10?"0"+minute:Integer.toString(minute);
        String sec = second < 10?"0"+second:Integer.toString(second);

        String date = yr+"-"+mnt+"-"+dy+" "+hr+":"+min+":"+sec;
        return date;
    }

    /**
     * this method returns previous dates depending on the value of position. For example, if
     * position = 0, it returns today's date, if position = -1 it returns yesterday's date, if
     * position is +1 it returns tomorrow's date.
     * @param position how far back the date should be from today's date.
     * @param hour a given hour value
     * @param minute a given minute value
     * @param second a given second value
     * @return date
     */
    public static String getDateTime(int position,int hour, int minute, int second)
    {
        String date = null;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, position);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        date = getDateTime(year, month, day, hour, minute, second);
        return date;
    }

    /**
     * this method takes a long and converts it to sql based time stamp yyyy-mm-dd hh:mm:ss
     * @param date date as long
     * @return string representation of date
     */
    public static String getDateTimeForSql(long date)
    {
        Timestamp ts = new Timestamp(date);
        return ts.toString();
    }

    /**
     * given a date as long, this method formats it and returns it as Sun, Jul 31
     * @param date the date as a long
     * @return the formatted date.
     */
    public static String getDate(long date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE,  MMM d", Locale.getDefault());
        return  dateFormat.format(new Date(date));
    }

    /**
     * this method is used to return the date and time for printing purposes
     * @param sqlTime The date and time as retrieved from the sql database
     * @return an array containing date and time. The first element is the date, the second is the time
     */
    public static String [] getDateAndTime(String sqlTime)
    {
        String result[] = new String[2];
        Timestamp timestamp = Timestamp.valueOf(sqlTime.length()==0?UtilityClass.getDateTime():sqlTime);
        result[0] = UtilityClass.getDate(timestamp.getTime());
        int hour = timestamp.getHours()%12;
        int min = timestamp.getMinutes();
        hour = hour==0?12:hour;
        result[1] = (hour<10?"0"+hour:hour)+":"+(min<10?"0"+min:min)+" "+(timestamp.getHours()>12?"PM":"AM");
        return result;
    }

    /**
     * method to return date and time in the form of Sun, Apr 18 etc
     * @return
     */
    public static String[] getDateAndTime()
    {
        return getDateAndTime("");
    }

    /**
     * method to pull the time portion from a date time object
     * @param dateTime string containing both date and time
     * @return the time portion of the date time string
     */
    public static String getTimeFromDateTime(String dateTime)
    {
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        long time = timestamp.getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        String dateString = UtilityClass.getDate(time)+" ";
        String hour = (calendar.get(Calendar.HOUR)<10)?"0"+calendar.get(Calendar.HOUR):""+calendar.get(Calendar.HOUR);
        String minute = calendar.get(Calendar.MINUTE)<10?"0"+calendar.get(Calendar.MINUTE):""+calendar.get(Calendar.MINUTE);
        String period = (calendar.get(Calendar.AM_PM)==0?"AM":"PM");

        return hour+":"+minute+" "+period;
    }

    /**
     * this method takes the datetime as string and gives the corresponding time as long
     * @param d time as string e.g 2017-08-29 09:40:29
     * @return datetime as long
     */
    public static long getDateAsLong(String d)
    {
        Timestamp timestamp = Timestamp.valueOf(d);
        return timestamp.getTime();
    }

    /**
     * convenient method to get current date/time in milliseconds
     * @return current date/time in millis
     */
    public static long getCurrentDateTimeMillis()
    {
        return Calendar.getInstance().getTimeInMillis();
    }

    /**
     * this method removes the naira sign from a string and all other formatting
     * @param amount the string containing naira sign
     * @return the money value as a double
     */
    public static double removeNairaSignFromString(String amount)
    {
        String naira = context.getResources().getString(R.string.naira);
        Number value = null;
        double dValue = -1;
        try
        {
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            amount = amount.replace(naira,"");       //amount.substring(1);
            amount = amount.replaceAll(",","");
            value = numberFormat.parse(amount);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        if(value != null)
           dValue = value.doubleValue();

        return dValue;
    }

    /**
     * this method formats the money and returns it as a string. It adds commas and appends the
     * naira sign.
     * @param money the money value to format
     * @return formatted money
     */
    public static String formatMoney(double money)
    {
        String value = context.getResources().getString(R.string.naira);
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setCurrency(Currency.getInstance("NGN"));
        //String.format("%,.2f",money);
        return value+numberFormat.format(money);
    }

    /**
     * this method formats the money and returns it as a string. It adds commas and appends the
     * naira sign.
     * @param money the money value to format
     * @return formatted money
     */
    public static String formatMoneyWithoutNairaSign(double money)
    {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setCurrency(Currency.getInstance("NGN"));
        //String.format("%,.2f",money);
        return numberFormat.format(money);
    }

    /**
     * this method returns the naira sisgn
     * @return the naira sign as a string
     */
    public static String getNairaSign()
    {
        return context.getResources().getString(R.string.naira);
    }

    /**
     * convenience method for obtaining the screen width
     * @return screen width
     */
    public static int getScreenWidth()
    {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels; //- (int)(20*dm.density);
        return width;
    }

    /**
     * convenience method for obtaining the screen height
     * @return screen height
     */
    public static int getScreenHeight()
    {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels; //- (int)(20*dm.density);
        return height;
    }

    /**
     * method to retrieve the current set currency. By default, this returns NGN
     * @return the currency.
     */
    public static String getCurrency()
    {
        return "NGN";
    }

    /**
     * create a string with the INNER JOIN statement
     * @param joinClause the constraint on the join statement without the ON statement
     * @param table the table to join
     * @return the join query
     */
    public static String getJoinQuery(String table, String joinClause)
    {
        String join = null;
        join = " INNER JOIN "+table+ " ON "+joinClause;
        /*if(group.length() == 0)
            join = " INNER JOIN "+table+ " ON "+joinClause; //+" LIMIT 20";
        else
            join = " INNER JOIN "+table+ " ON "+joinClause; //+" GROUP BY "+group;//+" LIMIT 20";*/
        return join;
    }

    /**
     * This method is called to create a Popup window, anchor it to a view and return a
     * reference to the window to the caller.
     * @param view the view to anchor the popup window to.
     * @param viewGroup a view group in the hierarchy of the layout
     */
    public static PopupWindow showPopupWindow(View view, ViewGroup viewGroup)
    {
        LayoutInflater inflater = LayoutInflater.from(UtilityClass.getContext());
        LinearLayout parent = (LinearLayout) inflater.inflate(R.layout.popup_window_layout,viewGroup,false);
        LinearLayout popupLayout = (LinearLayout) parent.findViewById(R.id.popup_layout);
        PopupWindow popupWindow = new PopupWindow();
        popupWindow.setContentView(parent);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        int width = viewGroup.getWidth() - UtilityClass.convertToPixels(10);
        popupWindow.setWidth(width);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.wet_asphalt_border));
        popupWindow.showAsDropDown(view,(viewGroup.getWidth() - width)/2,10);

        inflater = null;
        viewGroup = null;
        view = null;
        parent = null;

        return popupWindow;
    }


    /**
     *this method is responsible for fetching and returning the transaction information related
     * to a given transaction ID
     * @param saleTransactionID the transaction ID whose sale information is to be fetched
     */
    public static JSONArray fetchDataForSaleTransaction(String saleTransactionID)
    {
        JSONArray array = new JSONArray();
        try
        {
            JSONObject object = new JSONObject();
            String columns [] = {"sale_transaction.name as name","item_id","quantity","unit_id","unit_price","cost","sale_transaction.last_edited as last_edited","amount_paid","total","user.name as user"};
            object.put("tableName","sale_item");
            String join = UtilityClass.getJoinQuery("sale_transaction","sale_transaction.id = " + "sale_transaction_id");
            String join1 = UtilityClass.getJoinQuery("user","user.id = " + "sale_transaction.user_id");
            object.put("join",join+" "+join1);
            object.put("whereArgs","sale_transaction_id" + "='"+saleTransactionID+"'");
            object.put("columnArgs",columns);
            //retrieve all the sale item relating to a particular sale transaction id
            array = DatabaseManager.fetchData(object);

            JSONObject item = null, query = null;
            for(int i = 0; i<array.length(); i++)
            {
                //loop through the result and retrieve the category and category id
                //for each item
                item = array.getJSONObject(i);
                query = new JSONObject();
                query.put("tableName","category");
                query.put("columns",new String[]{"category","category_id","item"});
                query.put("join",UtilityClass.getJoinQuery("item","category.id = " +
                        "category_id AND item.id='"+item.getString("item_id")+"'"));
                query.put("extra","GROUP BY category.id");
                JSONArray array1 = DatabaseManager.fetchData(query);
                item.put("category",array1.getJSONObject(0).getString("category"));
                item.put("category_id",array1.getJSONObject(0).getString("category_id"));
                item.put("item",array1.getJSONObject(0).getString("item"));

                query = null;
                array1 = null;
                //retrieve unit from the unit table
                query = new JSONObject();
                query.put("tableName","unit");
                query.put("columns",new String[]{"unit"});
                query.put("whereArgs","id = '"+item.getString("unit_id")+"' "
                        + "AND item_id='"+item.getString("item_id")+"'");
                array1 = DatabaseManager.fetchData(query);
                item.put("unit",array1.getJSONObject(0).getString("unit"));
                //item.put("unit_id",array1.getJSONObject(0).getString("id"));

                //retrieve the current quantity for the given item
                item.put("current_quantity",DatabaseManager.retrieveCurrentQuantity(
                        Long.parseLong(item.getString("item_id"))));

                item = null;
                array1 = null;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return array;
    }

    /**
     * this method is called by the bluetooth receiver class to get a reference to the bluetooth
     * devices adapter object
     * @return
     */
    public static BluetoothDevicesListAdapter getBluetoothDevicesListAdapter()
    {
        return bluetoothDevicesListAdapter;
    }

    public static void setBluetoothDevicesListAdapter(BluetoothDevicesListAdapter adapter)
    {
        bluetoothDevicesListAdapter = adapter;
    }

    private static void setLastUploadRetryTime()
    {
        SharedPreferences preferences = UtilityClass.getContext().getSharedPreferences("reccordrack",Context.MODE_PRIVATE);
        Date date = new Date();
        preferences.edit().putLong("retry_time",date.getTime()).commit();
    }

    private static long getLastUploadRetryTime()
    {
        SharedPreferences preferences = UtilityClass.getContext().getSharedPreferences("reccordrack",Context.MODE_PRIVATE);
        long time = preferences.getLong("retry_time",0);
        //if(time.equals("null"))
        return time;
    }

    /**
     * this method checks to see if the last upload time was more than 10 mins ago or if retry has
     * never been attempted, then we should try it
     * @return true if retry upload should be invoked, false otherwise
     */
    public static boolean shouldRetryDataUpload()
    {
        boolean res=false;
        long time = getLastUploadRetryTime();
        Date now = new Date();
        long prev = now.getTime()-time;
        prev = prev/1000;
        if(prev>600 || time==0)//if more than 10 mins ago or we've never uploaded before
        {
            res = true;
            setLastUploadRetryTime();
        }
        else
            res = false;
        return res;
    }

    public static String getLogPath()
    {
        File logsDir = UtilityClass.getContext().getDir("logs", Context.MODE_PRIVATE);
        //File logsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        //showToast(logsDir.getAbsolutePath());
        return logsDir.getAbsolutePath();
    }
}