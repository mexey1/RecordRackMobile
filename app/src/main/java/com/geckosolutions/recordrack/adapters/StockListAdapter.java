package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomDialogFragment;
import com.geckosolutions.recordrack.interfaces.CustomDialogInterface;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.StockFragmentLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by anthony1 on 5/8/16.
 * This
 */
public class StockListAdapter extends BaseAdapter implements CustomDialogInterface
{
    private ArrayList<JSONObject> stocks;//this variable holds all the stocks to be displayed
    private LayoutInflater inflater;
    private WeakReference<Activity> reference;
    private WeakReference<FragmentManager> fragmentManagerWeakReference;
    private int datePosition = 0,position;
    private JSONObject stock;
    private StockListAdapter adapter;
    private CustomDialogFragment dialogFragment;
    private StockViewPagerAdapter viewPagerAdapter;
    private ViewPager pager;
    private Dialog dialog;
    private ViewGroup viewPagerPosition;
    private final String TAG="StockListAdapter";

    public StockListAdapter(WeakReference<Activity> reference, WeakReference<FragmentManager> reference1)
    {
        stocks = new ArrayList<>();
        inflater = LayoutInflater.from(UtilityClass.getContext());
        this.fragmentManagerWeakReference = reference1;
        this.reference = reference;
        adapter = this;
    }


    @Override
    public int getCount()
    {
        return stocks.size();
    }

    /**
     * this method takes a new stock and adds it to the stocks JSONArray
     * @param item the JSONObject to be added
     */
    public void addItem(JSONObject item)
    {
        stocks.add(item);
        notifyDataSetChanged();
    }

    private void deleteStock(int position)
    {
        stocks.remove(position);
        notifyDataSetChanged();
    }

    /**
     * remove all items in the data model backing this list view
     */
    public void clearStockArray()
    {
        stocks.clear();
        notifyDataSetChanged();
    }
    @Override
    public Object getItem(int position)
    {
        return stocks.get(position);
    }

    @Override
    public long getItemId(int position)
    {
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
                    convertView =  createNewItem(position,parent);
                else
                    convertView = createNewCategory(position,parent);
            }
            else
            {
                if(!(convertView instanceof TextView))
                {
                    if(isItem(position))
                    {
                        populateViews((LinearLayout)convertView,position);
                        initializeDeleteAndEditButtons((LinearLayout) convertView, position);
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
                        ((TextView) convertView).setText(stocks.get(position).getString("category"));
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return  convertView;
    }

    private View createNewCategory(int position, ViewGroup parent) throws JSONException
    {
        Logger.log(TAG,"Stock to be displayed "+stocks.get(position).toString());
        Log.d(TAG,"Stock to be displayed "+stocks.get(position).toString());
        TextView item = (TextView) inflater.inflate(R.layout.category,parent,false);
        item.setText(stocks.get(position).getString("category"));
        return item;
    }

    private View createNewItem(int position, ViewGroup parent)
    {
        LinearLayout item = (LinearLayout)inflater.inflate(R.layout.stock_item,parent,false);
        item.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
        //item.setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
        populateViews(item, position);
        initializeDeleteAndEditButtons(item, position);
        return item;
    }

    private void populateViews(LinearLayout newItem, int position)
    {
        try
        {
            JSONObject object = stocks.get(position);
            ((TextView)newItem.findViewById(R.id.item)).setText(object.getString("item"));
            /*((TextView)newItem.findViewById(R.id.quantity)).setText(object.getString("quantity") +
                                                                    " " + object.getString("unit"));*/
            ((TextView)newItem.findViewById(R.id.quantity)).setText(object.getString("quantity"));
            //((TextView)newItem.findViewById(R.id.cost)).setText(UtilityClass.formatMoney(Double.parseDouble(object.getString("cost"))));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * this method is designed to specify what happens when edit and delete buttons would do
     * @param newItem the item being added that needs to be modified
     * @param pos the position this view ta
     */
    private void initializeDeleteAndEditButtons(final LinearLayout newItem,  int pos)
    {
        //define what happens when the view button is pressed
        newItem.findViewById(R.id.view).setTag(pos);
        newItem.findViewById(R.id.view).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int pos = (Integer) v.getTag();
                datePosition = 0;
                position = pos;
                dialogFragment = new CustomDialogFragment();
                dialogFragment.onDialogShow(adapter);
                Bundle bundle = new Bundle();
                bundle.putInt("xml",R.layout.stock_view_layout);
                dialogFragment.setHeight(500);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(fragmentManagerWeakReference.get(),"ViewStock");

            }
        });

        //define what happens when the edit button is pressed
        newItem.findViewById(R.id.edit).setTag(pos);
        newItem.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    final int pos = (Integer) v.getTag();
                    dialog = UtilityClass.showCustomDialog(R.layout.stock_edit_layout,reference);
                    stock = stocks.get(pos);
                    ((EditText)dialog.findViewById(R.id.item_name)).setText(stock.getString("item"));

                    //define what happens when the submit button is pressed
                    ((Button)dialog.findViewById(R.id.submit)).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            DatabaseThread.getDatabaseThread().postDatabaseTask(updateStock);
                        }
                    });
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

        //define what happens when the delete button is pressed
        newItem.findViewById(R.id.delete).setTag(pos);
        newItem.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final int pos = (Integer) v.getTag();
                dialog = UtilityClass.showCustomDialog(R.layout.confirm_delete_layout,reference);
                ((TextView)dialog.findViewById(R.id.text)).setText("Deleting this item would no longer make it available for sale. Do you wish to proceed?");
                dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        position = pos;
                        DatabaseThread.getDatabaseThread().postDatabaseTask(deleteStock);
                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private Runnable deleteStock = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                stock = stocks.get(position);
                String itemID = stock.getString("item_id");
                JSONObject object = new JSONObject();
                object.put("tableName","item");
                object.put("archived","1");
                DatabaseManager.updateData(object,"id='"+itemID+"'");

                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        deleteStock(position);
                        UtilityClass.showToast("Item was deleted successfully");
                    }
                });
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };

    private Runnable updateStock = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                String itemName = ((EditText)dialog.findViewById(R.id.item_name)).getText().toString().trim();
                if(itemName.length() == 0)
                {
                    UtilityClass.showToast("Item name cannot be left empty");
                    return;
                }
                JSONObject object = new JSONObject();
                object.put("tableName","item");
                object.put("item",itemName);
                Logger.log(TAG,"This is stock: "+"this is stock "+stock.toString());
                Log.d(TAG,"This is stock: "+"this is stock "+stock.toString());
                DatabaseManager.updateData(object,"id = "+stock.getLong("item_id"));
                //close the dialog
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        dialog.dismiss();
                    }
                });
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };
    /**
     * check if the current item is a category or a valid item
     * @param position
     * @return
     * @throws JSONException
     */
    private boolean isItem(int position) throws JSONException
    {
        return stocks.get(position).has("quantity");
    }

    /**
     * convenience method to set data for the stock info dialog
     * @param parent the layout containing views whose data is being updated
     * @param object JSONObject containg the stock info data
     * @throws JSONException
     */
    private void setStockInfoItem(ViewGroup parent, JSONObject object)
    {
        try
        {
            TextView openingStock = (TextView)parent.findViewById(R.id.opening_stock);
            TextView quantitySold = (TextView)parent.findViewById(R.id.quantity_sold);
            TextView quantity = (TextView)parent.findViewById(R.id.current_quantity);
            TextView date = (TextView)parent.findViewById(R.id.date);
            TextView purchase = (TextView)parent.findViewById(R.id.purchase);

            date.setText(UtilityClass.getDateTime(datePosition));
            Logger.log(TAG,"Date position selected " + datePosition);
            Log.d(TAG,"Date position selected " + datePosition);
            if(object.has("initialQuantity") && object.has("quantitySold") && object.has("quantity"))
            {
                parent.findViewById(R.id.parent).setVisibility(View.VISIBLE);
                parent.findViewById(R.id.end_data).setVisibility(View.GONE);
                openingStock.setText(object.getString("initialQuantity"));
                quantitySold.setText(object.getString("quantitySold"));
                quantity.setText(object.getString("quantity"));
                purchase.setText(object.getString("purchase"));
            }
            else
            {
                parent.findViewById(R.id.parent).setVisibility(View.GONE);
                parent.findViewById(R.id.end_data).setVisibility(View.VISIBLE);
            }
            openingStock = null;
            quantitySold = null;
            quantity = null;
            date = null;
            purchase = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void initDialog()
    {
        try
        {
            getBaseUnitEquivalent();
            JSONObject object = stocks.get(position);
            viewPagerAdapter = new StockViewPagerAdapter();
            dialog = dialogFragment.getDialog();
            pager = (ViewPager) dialog.findViewById(R.id.view_pager);
            viewPagerPosition = (ViewGroup)dialog.findViewById(R.id.positions);
            pager.setAdapter(viewPagerAdapter);

            //inflate the layout that would reside on the first page
            final ViewGroup parent = (ViewGroup) LayoutInflater.from(UtilityClass.getContext()).inflate(R.layout.stock_info_layout,pager,false);
            //set title to the item selected
            ((TextView)dialog.findViewById(R.id.title)).setText(object.getString("item"));

            ScrollView stockInfoItem = (ScrollView)LayoutInflater.from(UtilityClass.getContext()).inflate(R.layout.stock_info_item,parent,false);
            LinearLayout container = (LinearLayout)parent.findViewById(R.id.container);
            container.addView(stockInfoItem);
            setStockInfoItem(parent, object);
            viewPagerAdapter.addView(parent);

            //go back one date
            parent.findViewById(R.id.backward).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //run off UI thread
                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                --datePosition;
                                JSONObject object = stocks.get(position);
                                String itemName = object.getString("item");
                                String itemID = object.getString("item_id");
                                object = null;

                                final JSONObject object1 = StockFragmentLogic.getItemData(itemName,itemID, datePosition);
                                //move to UI thread
                                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        setStockInfoItem(parent,object1);
                                    }
                                });
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            //go forward one day
            parent.findViewById(R.id.forward).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                        @Override
                        public void run()
                        {
                            try
                            {
                                ++datePosition;
                                JSONObject object = stocks.get(position);
                                String itemName = object.getString("item");
                                String itemID = object.getString("item_id");
                                object = null;

                                final JSONObject object1 = StockFragmentLogic.getItemData(itemName,itemID, datePosition);
                                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        setStockInfoItem(parent,object1);
                                    }
                                });

                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
            {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position)
                {
                    viewPagerPosition.findViewById(position).setBackgroundResource(R.drawable.grey_circle);
                    viewPagerPosition.findViewById((position+1)%2).setBackgroundResource(R.drawable.white_circle);
                }

                @Override
                public void onPageScrollStateChanged(int state)
                {

                }
            });


            //dialog = null;
            object = null;
            container = null;
            stockInfoItem = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void getBaseUnitEquivalent()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject object = new JSONObject();

                    object.put("tableName","unit");
                    object.put("columnArgs", new String[]{"unit","base_unit_equivalent"});
                    object.put("whereArgs","item_id = "+stocks.get(position).getString("item_id"));
                    final JSONArray array = DatabaseManager.fetchData(object);
                    Logger.log(TAG,"Fetched this "+array.toString());
                    Log.d(TAG,"Fetched this "+array.toString());
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(array.length() > 1)
                            {
                                ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.unit_relationship_view,pager,false);
                                ViewGroup container = (ViewGroup) layout.findViewById(R.id.container);
                                for(int i=0; i<array.length()-1;i++)
                                {
                                    try
                                    {
                                        ViewGroup child = (ViewGroup) inflater.inflate(R.layout.unit_relationship_view_item,pager,false);
                                        JSONObject object = array.getJSONObject(i);
                                        JSONObject object1 = array.getJSONObject(i+1);
                                        double quantity = object1.getDouble("base_unit_equivalent")/object.getInt("base_unit_equivalent");
                                        ((TextView)child.findViewById(R.id.quantity)).setText(Double.toString(quantity));
                                        ((TextView)child.findViewById(R.id.unit1)).setText(object.getString("unit"));
                                        ((TextView)child.findViewById(R.id.unit2)).setText(object1.getString("unit"));
                                        container.addView(child);
                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }

                                }
                                viewPagerAdapter.addView(layout);
                                addPositionIndicator(0);
                                addPositionIndicator(1);
                            }
                        }
                    });

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * this method is called to create and add the position indicator for the viewpager
     * @param pos the position this indicator represents
     */
    private void addPositionIndicator(int pos)
    {
        View view = inflater.inflate(R.layout.view,viewPagerPosition,false);
        if(pos == 0)
            view.setBackgroundResource(R.drawable.grey_circle);
        else
            view.setBackgroundResource(R.drawable.white_circle);
        view.setId(pos);
        viewPagerPosition.addView(view);
    }
}
