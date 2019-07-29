package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomTextView;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.SalesInfoFragmentLogic;
import com.geckosolutions.recordrack.logic.StockFragmentLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by anthony1 on 7/15/16.
 */
public class SalesInfoListAdapter extends BaseAdapter
{
    private JSONArray items;
    private LayoutInflater inflater;
    private static int start, end, pos;
    private static String itemID = null, itemName = null;
    private  Dialog dialog;
    private static WeakReference<Activity> reference;
    private static WeakReference<SalesInfoListAdapter> salesInfoListAdapterWeakReference;
    private String daysOfWeeks[] = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
    int []colors = new int[]{R.color.pomegranate,R.color.midnight_blue,R.color.orange1,R.color.blue1,
            R.color.emerald,R.color.applegreen,R.color.battleship_grey,R.color.wisteria,
            R.color.peterriver,R.color.wet_asphalt,R.color.green_sea,R.color.grey};
    private double total;
    public SalesInfoListAdapter(WeakReference<Activity>reference)
    {
        //items = new ArrayList();
        this.reference = reference;
        salesInfoListAdapterWeakReference = new WeakReference<SalesInfoListAdapter>(this);
        inflater = LayoutInflater.from(UtilityClass.getContext());
    }

    public void addItem(JSONArray object)
    {
        items = null;
        items = object;
        notifyDataSetChanged();
    }

    /**
     * private method to delete items in the data model
     */
    public void deleteAllItems()
    {
        items = null;
    }
    @Override
    public int getCount()
    {
        if(items == null)
            return 0;
        else
             return items.length();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        try
        {
            if(convertView == null)
            {
                if(isItem(position))
                    convertView = createNewItem(position,parent);
                else
                    convertView = createNewCategory(position,parent);
            }
            else
            {
                if(!(convertView instanceof TextView))
                {
                    if(isItem(position))
                    {
                        populateViews((LinearLayout) convertView, position);
                        initializeViewButton((LinearLayout) convertView, position);
                        //return convertView;
                    }
                    else
                        convertView = createNewCategory(position, parent);
                }
                else
                {
                    if(isItem(position))
                        convertView = createNewItem(position, parent);
                    else
                        ((TextView) convertView).setText(items.getJSONObject(position).getString("category"));
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return  convertView;
    }

    //creates a new category object
    private View createNewCategory(int position, ViewGroup parent) throws JSONException
    {
        TextView item = (TextView)inflater.inflate(R.layout.textview,parent,false);
        item.setText(items.getJSONObject(position).getString("category"));
        return item;
    }

    //creates a new item
    private View createNewItem(int position, ViewGroup parent)
    {
        LinearLayout item = (LinearLayout)inflater.inflate(R.layout.sales_item2,parent,false);
        item.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
        item.findViewById(R.id.view).setTag(position);
        //item.setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
        populateViews(item, position);
        initializeViewButton(item, position);
        return item;
    }

    private void populateViews(LinearLayout newItem, int position)
    {
        try
        {
            JSONObject object = items.getJSONObject(position);
            ((TextView)newItem.findViewById(R.id.item)).setText(object.getString("item"));
            ((TextView)newItem.findViewById(R.id.value)).setText(UtilityClass.formatMoney(Double.parseDouble(object.getString("total"))));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * check if the current item is a category or a valid item
     * @param position
     * @return
     * @throws JSONException
     */
    private boolean isItem(int position) throws JSONException
    {
        return items.getJSONObject(position).has("total");
    }

    /**
     * define what happens when the view button is clicked
     * @param item the linearlayout containing the view button.
     * @param position the position of the linearlayout in the listview.
     */
    private void initializeViewButton(LinearLayout item, final int position)
    {
        item.findViewById(R.id.view).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    pos = (Integer)v.getTag();
                    itemID = items.getJSONObject(pos).getString("item_id");
                    //initialize the first start and end date
                    Calendar calendar = Calendar.getInstance();
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    start = 1 - dayOfWeek;
                    end = 7 - dayOfWeek;
                    DatabaseThread.getDatabaseThread().postDatabaseTask(query);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * runnable that queries the database to fetch sales values for a given date range
     */
    private static Runnable query = new Runnable()
    {
        @Override
        public void run()
        {
            //retrieve data
            final double totals[] = SalesInfoFragmentLogic.getSalesForWeek(itemID, start, end);

            //populate on UI thread
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        populateDialog(totals);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /**
     * method to populate the dialog that shows up when new data is to be displayed.
     * @param values the values to be represented as a bar chart.
     */
    private static void populateDialog(double values[]) throws JSONException
    {
        Dialog dialog = salesInfoListAdapterWeakReference.get().dialog;
        if(dialog== null)
        {
            salesInfoListAdapterWeakReference.get().dialog = UtilityClass.showCustomDialog(R.layout.sales_barchart_layout, reference);
            dialog = salesInfoListAdapterWeakReference.get().dialog;
            //define what happens when the back ward button is clicked
            dialog.findViewById(R.id.backward).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    salesInfoListAdapterWeakReference.get().onBackward();
                }
            });

            //define what happens when the forward button is clicked
            dialog.findViewById(R.id.forward).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    salesInfoListAdapterWeakReference.get().onForward();
                }
            });

            //listen for when the dialog is dismissed
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    start = 0;
                    end = -1;
                    itemID = null;
                    pos = -1;
                    salesInfoListAdapterWeakReference.get().dialog = null;
                }
            });
        }

        String itemName = salesInfoListAdapterWeakReference.get().items.getJSONObject(pos).getString("item");
        double max = values[7];
        int count = 0;
        ((CustomTextView)dialog.findViewById(R.id.date)).setText(UtilityClass.getDateTime(start)+" - "+UtilityClass.getDateTime(end));
        ((CustomTextView)dialog.findViewById(R.id.title)).setText(itemName);
        while (count < values.length-1)
        {
            LinearLayout chart = (LinearLayout)salesInfoListAdapterWeakReference.get().inflater.inflate(R.layout.bar_chart_element,
                                                                (LinearLayout)dialog.findViewById(R.id.container),false);
            ((TextView)chart.findViewById(R.id.label)).setText(salesInfoListAdapterWeakReference.get().daysOfWeeks[count]);//set the label

            //format the money value appending K, M, B
            String value = null;
            if(values[count] < 999)
                value = UtilityClass.getNairaSign()+values[count];
            else if(values[count] < 999999)
                value = UtilityClass.getNairaSign()+String.format("%.1f",values[count]/1000)+"K";
            else if(values[count] < 999999999)
                value = UtilityClass.getNairaSign()+String.format("%.1f", values[count]/1000000)+"M";
            else
                value = UtilityClass.getNairaSign()+String.format("%.1f", values[count]/1000000000)+"B";

            ((TextView) chart.findViewById(R.id.value)).setText(value);

            int width = UtilityClass.getScreenWidth()/7;
            int pseudoHeight = UtilityClass.convertToPixels(10);
            int height = UtilityClass.convertToPixels((int)(values[count]/max*100));
            height = height < pseudoHeight ? pseudoHeight+height:height;
            //format the bar. set the height, width and background color
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
            params.setMargins(10,10,10,10);
            chart.findViewById(R.id.bar).setLayoutParams(params);
            chart.findViewById(R.id.bar).setMinimumHeight(UtilityClass.convertToPixels(10));
            chart.findViewById(R.id.bar).setBackgroundColor(reference.get().getResources().
                                                            getColor(salesInfoListAdapterWeakReference.get().colors[count]));
            ((LinearLayout)dialog.findViewById(R.id.container)).addView(chart);
            chart = null;
            params = null;
            count++;
        }
        dialog = null;
    }

    /**
     * method that clearly defines what happens when the backward button is pressed
     */
    private void onBackward()
    {
        end = start -1;
        start-=7;
        ((LinearLayout)dialog.findViewById(R.id.container)).removeAllViews();
        DatabaseThread.getDatabaseThread().postDatabaseTask(query);
    }

    /**
     * method that clearly defines what happens when the backward button is pressed
     */
    private void onForward()
    {
        start = end + 1;
        end = start+6;
        ((LinearLayout)dialog.findViewById(R.id.container)).removeAllViews();
        DatabaseThread.getDatabaseThread().postDatabaseTask(query);
    }
}
