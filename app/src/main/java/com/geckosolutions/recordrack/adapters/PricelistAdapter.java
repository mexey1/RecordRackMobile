package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.PricelistLogic;
import com.geckosolutions.recordrack.logic.Tuples;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by anthony1 on 6/4/17.
 */

public class PricelistAdapter extends BaseAdapter
{
    private ArrayList<JSONObject> prices;
    private LayoutInflater inflater;
    private PricelistLogic pricelistLogic;
    private boolean isEdit;//boolean flag to indicate user wants to edit prices
    private int position;

    public PricelistAdapter(PricelistLogic pricelistLogic)
    {
        prices = new ArrayList<>();
        this.pricelistLogic = pricelistLogic;
        inflater = LayoutInflater.from(UtilityClass.getContext());
    }

    /**
     * this method takes a new stock and adds it to the stocks JSONArray
     * @param item the JSONObject to be added
     */
    public void addItem(JSONObject item)
    {
        prices.add(item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return prices.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
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
                        initializeViewAndEditButtons((LinearLayout) convertView, position);
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
                        ((TextView) convertView).setText(prices.get(position).getString("category"));
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
        TextView item = (TextView) inflater.inflate(R.layout.category,parent,false);
        item.setText(prices.get(position).getString("category"));
        return item;
    }

    private View createNewItem(int position, ViewGroup parent)
    {
        LinearLayout item = (LinearLayout)inflater.inflate(R.layout.price_list_item,parent,false);
        item.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
        //item.setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
        populateViews(item, position);
        initializeViewAndEditButtons(item, position);
        return item;
    }

    private void populateViews(LinearLayout newItem, int position)
    {
        try
        {
            JSONObject object = prices.get(position);
            ((TextView)newItem.findViewById(R.id.item)).setText(object.getString("item"));
            /*((TextView)newItem.findViewById(R.id.quantity)).setText(object.getString("quantity") +
                                                                    " " + object.getString("unit"));*/
            //((TextView)newItem.findViewById(R.id.quantity)).setText(object.getString("quantity"));
            //((TextView)newItem.findViewById(R.id.cost)).setText(UtilityClass.formatMoney(Double.parseDouble(object.getString("cost"))));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void initializeViewAndEditButtons(LinearLayout newItem, int pos)
    {
        newItem.findViewById(R.id.view).setTag(pos);
        newItem.findViewById(R.id.view).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        isEdit = false;
                        position = (Integer) v.getTag();
                        Tuples<String,JSONArray> tuples = getPrices();
                        displayPrices(tuples.getFirst(),tuples.getSecond());

                    }
                });
            }
        });

        newItem.findViewById(R.id.edit).setTag(pos);
        newItem.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        isEdit = true;
                        position = (Integer) v.getTag();
                        Tuples<String,JSONArray> tuples = getPrices();
                        displayPrices(tuples.getFirst(),tuples.getSecond());

                    }
                });
            }
        });
    }

    /**
     * this method returns a Tuple. the first element is the name of the item being displayed
     * the second is the JSONArray
     * @return
     */
    private Tuples<String, JSONArray> getPrices()
    {
        Tuples<String,JSONArray> tuples = null;
        try
        {
            tuples = new Tuples<>();
            JSONObject object = prices.get(position);
            String item = object.getString("item");
            int itemID = object.getInt("id");
            JSONArray prices = pricelistLogic.getPrice(itemID);
            tuples.setValues(item,prices);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return tuples;
    }

    private void displayPrices(final String item,final JSONArray prices)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                final Dialog dialog = UtilityClass.showCustomDialog(R.layout.pricelist_info_layout,pricelistLogic.getActivityWeakReference());
                ((TextView)dialog.findViewById(R.id.item_name)).setText(item);
                final ViewGroup container = (ViewGroup) dialog.findViewById(R.id.container);
                try
                {
                    for(int i=0; i<prices.length();i++)
                    {
                        JSONObject object = prices.getJSONObject(i);
                        ViewGroup item = null;
                        if(isEdit)
                        {
                            item = (ViewGroup) inflater.inflate(R.layout.pricelist_edit_item, container, false);
                            ((CustomEditText)item.findViewById(R.id.price)).enableTextChangedListener();
                            dialog.findViewById(R.id.submit).setVisibility(View.VISIBLE);
                            dialog.findViewById(R.id.done).setVisibility(View.GONE);
                        }
                        else
                            item = (ViewGroup) inflater.inflate(R.layout.pricelist_info_item,container,false);
                        ((TextView)item.findViewById(R.id.unit)).setText(object.getString("unit"));
                        ((TextView)item.findViewById(R.id.price)).setText(UtilityClass.formatMoney(object.getDouble("retail_price")));
                        container.addView(item);
                        item = null;
                        object = null;
                    }
                    if(isEdit)
                    {
                        dialog.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                //run database task off ui thread
                                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            Tuples<Long,Double> tupleArray[] = new Tuples[prices.length()];
                                            for(int i=0; i<prices.length();i++)
                                            {
                                                long id = prices.getJSONObject(i).getLong("id");
                                                String text = ((EditText)((ViewGroup)container.getChildAt(i)).findViewById(R.id.price)).getText().toString();
                                                if(text.trim().length() == 0)
                                                {
                                                    UtilityClass.showToast("Please enter a price value to continue");
                                                    return;
                                                }
                                                double price = UtilityClass.removeNairaSignFromString(text);
                                                Tuples<Long,Double> tuples = new Tuples<Long, Double>();
                                                tuples.setValues(id,price);
                                                tupleArray[i] = tuples;
                                                dialog.dismiss();
                                            }
                                            pricelistLogic.updatePrice(tupleArray);

                                        }
                                        catch (JSONException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                    }
                    else
                    {
                        dialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                dialog.dismiss();
                            }
                        });
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * check if the current item is a category or a valid item
     * @param position
     * @return
     * @throws JSONException
     */
    private boolean isItem(int position) throws JSONException
    {
        return prices.get(position).has("item");
    }
}
