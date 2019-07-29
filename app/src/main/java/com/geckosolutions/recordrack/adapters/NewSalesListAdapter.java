package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.SharedLayout;
import com.geckosolutions.recordrack.fragments.NewSalesFragment;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by anthony1 on 1/28/16.
 * this class holds the items to be sold
 */
public class NewSalesListAdapter extends BaseAdapter
{
    private LayoutInflater inflater;
    private ArrayList<JSONObject> arrayList;
    private Hashtable<Long,Double> addedItems;//<item_id,quantity>
    private WeakReference<NewSalesFragment> reference;
    private Dialog dialog;
    protected double sum =0,val;
    private ArrayList<Double> total;
    private final String TAG="NewSalesListAdapter";

    public NewSalesListAdapter ()
    {
        inflater = LayoutInflater.from(UtilityClass.getContext());
        arrayList = new ArrayList<>();
        addedItems = new Hashtable<>();
        total = new ArrayList<>();
    }

    public NewSalesListAdapter(WeakReference<NewSalesFragment> reference)
    {
        this.reference = reference;
        inflater = LayoutInflater.from(UtilityClass.getContext());
        arrayList = new ArrayList<>();
        addedItems = new Hashtable<>();
        total = new ArrayList<>();
    }

    public ArrayList getArrayList()
    {
        return arrayList;
    }

    public Hashtable<Long,Double> getAddedItems()
    {
        return addedItems;
    }

    /**
     * this method is called to add a new item to the data model supporting the view
     * @param object the item to be added
     * @throws JSONException
     */
    public void addItem(final JSONObject object) throws JSONException
    {
        arrayList.add(object);
        total.add(object.getDouble("cost"));
        val = object.getDouble("quantity");
        if(addedItems.containsKey(object.getLong("item_id")))
        {
            Logger.log(TAG,"Value of val0: "+val);
            Log.d(TAG,"Value of val0: "+val);
            val = addedItems.get(object.getLong("item_id"));
            long unit_id = Long.parseLong(object.getString("unit_id"));
            long item_id = Long.parseLong(object.getString("item_id"));

            //bue means base unit equivalent
            int bue = DatabaseManager.getBaseUnitEquivalent(item_id,unit_id);
            val = val+(object.getDouble("quantity")* bue);
        }
        Logger.log(TAG,"Value of val is: "+val);
        Log.d(TAG,"Value of val is: "+val);

        //if not UI thread
        if(!Looper.getMainLooper().getThread().equals(Thread.currentThread()))
        {
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        addedItems.put(object.getLong("item_id"),val);
                        refreshUI();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        else
        {
            addedItems.put(object.getLong("item_id"),val);
            refreshUI();
        }
    }

    /**
     * if this item was previously added, return the quantity added
     * @param itemID the itemID of the iten in question
     * @return quantity already added
     */
    public double getSoldQuantities(long itemID)
    {
        return (addedItems.containsKey(itemID))?addedItems.get(itemID):0;
    }

    /**
     * method to clear the data model backing up the associated listview
     */
    public void clearArrayList()
    {
        //arrayList.clear();
        total.clear();
        addedItems.clear();
        //arrayList.clear();
        arrayList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) throws JSONException
    {
        JSONObject object = arrayList.remove(position);
        total.remove(position);
        total.trimToSize();
        arrayList.trimToSize();

        long unit_id = Long.parseLong(object.getString("unit_id"));
        long item_id = Long.parseLong(object.getString("item_id"));

        int bue = DatabaseManager.getBaseUnitEquivalent(item_id,unit_id);
        double val = addedItems.get(object.getLong("item_id"));
        val = val-(object.getDouble("quantity")* bue);
        if(val == 0)
            addedItems.remove(object.getLong("item_id"));
        else
            addedItems.put(object.getLong("item_id"),val);
        //computeTotalAmount();
        refreshUI();
    }

    private void refreshUI()
    {
        if(Looper.myLooper() == Looper.getMainLooper())
        {
            notifyDataSetChanged();
            //notifyDataSetInvalidated();
            computeTotalAmount();
        }
        else
            {
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    notifyDataSetChanged();
                    computeTotalAmount();
                }
            });
        }
    }

    private void computeTotalAmount()
    {
        sum = 0;
        for(double d : total)
            sum+=d;
        if(reference != null)
            reference.get().setTotal(sum);
    }
    @Override
    public int getCount()
    {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            LinearLayout newItem = (LinearLayout)inflater.inflate(R.layout.sales_item,parent,false);
            populateViews(newItem, position);
            if(reference != null)
            {
                newItem.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
                initializeDeleteAndEditButtons(newItem, position);
            }
            convertView = newItem;
        }
        else
        {
            populateViews(convertView,position);
            if(reference != null)
                initializeDeleteAndEditButtons(convertView,position);
        }

        return convertView;
    }

    /**
     * this method is called to populate the fields for the current listview item
     * @param view the view that holds the fields
     * @param position the position where the view would be inserted. The listview data is backed by
     *                 an ArrayList, the data to fill into the fields are gotten from the ArrayList.
     */
    private void populateViews(View view, int position)
    {
        try
        {
            TextView category = (TextView)view.findViewById(R.id.category);
            TextView item = (TextView)view.findViewById(R.id.item);
            TextView quantity = (TextView)view.findViewById(R.id.quantity);
            TextView cost = (TextView)view.findViewById(R.id.cost);

            JSONObject object = arrayList.get(position);

            category.setText(object.getString("category"));
            item.setText(object.getString("item"));
            quantity.setText(object.getString("quantity") +" "+ object.getString("unit"));
            cost.setText(UtilityClass.formatMoney(Double.parseDouble(object.getString("cost"))));//" "+);

            category = null;
            item = null;
            quantity = null;
            cost = null;
            object = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * For each item added to the list, there exists Delete and Edit buttons that are used to
     * perform edit and delete actions
     * @param view the view whose edit and delete buttons are being initialized
     * @param position the position where the view is located in the ListView
     */
    private void initializeDeleteAndEditButtons(final View view,int position)
    {
        final LinearLayout edit = (LinearLayout)view.findViewById(R.id.edit);
        LinearLayout delete = (LinearLayout)view.findViewById(R.id.delete);
        edit.setTag(new Integer(position));
        delete.setTag(new Integer(position));
        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                Log.d(TAG,"Should allow edit"+Boolean.toString(NewSalesFragment.shouldAllowEdit()));
                Logger.log(TAG,"Should allow edit"+Boolean.toString(NewSalesFragment.shouldAllowEdit()));
                //UtilityClass.showToast(Boolean.toString(NewSalesFragment.shouldAllowEdit()));
                if(!NewSalesFragment.shouldAllowEdit())
                {
                    final Dialog dialog = UtilityClass.showCustomDialog(R.layout.edit_not_allowed,
                            new WeakReference<Activity>((AppCompatActivity) reference.get().getActivity()));
                    dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                        }
                    });
                }
                else
                {
                    //we have to first retrieve the current quantity for the item being edited.
                    //in case user doesn't follow nornormal program flow
                    final Integer position = (Integer)v.getTag();

                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                JSONObject object = arrayList.get(position);
                                final double currentQuantity = DatabaseManager.retrieveCurrentQuantity(object.getLong("item_id"));
                                //once we have the current quantity, we'd have to proceed with normal flow
                                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        displayEditDialog(position, currentQuantity);
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
            }
        });

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Integer position = (Integer)v.getTag();
                dialog = UtilityClass.showCustomDialog(R.layout.confirm_delete_layout,
                        new WeakReference<Activity>((AppCompatActivity)reference.get().getActivity()));
                dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //move to database thread
                        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    removeItem(position);
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });

                        //notifyDataSetInvalidated();
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

    /**
     * method called when the edit button is clicked
     * @position position of item being edited
     */
    private void displayEditDialog(final int position, final double currentQuantity)
    {
        dialog = reference.get().displayNewItemDialog(arrayList.get(position),currentQuantity,1);
        populateItemEditDialog(position);
        dialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //onDoneButtonPressed calls a database method, hence has to be called from the
                //database thread
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if(reference.get().onDoneButtonPressed())
                                removeItem(position);
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

    /**
     * private method to populate the dialog that pops up when the edit button is
     * clicked.
     * @param position the position of the item in the data model
     */
    private void populateItemEditDialog(int position)
    {
        try
        {
            JSONObject object = arrayList.get(position);
            EditText search = (EditText)dialog.findViewById(R.id.search);
            EditText quantity = (EditText) dialog.findViewById(R.id.quantity);
            EditText unitPrice = (EditText)dialog.findViewById(R.id.unit_price);
            search.setText(object.getString("item"));
            quantity.setText(object.getString("quantity"));
            unitPrice.setText(object.getString("unit_price"));
            reference.get().setItemBeingEdited(object.getString("item"),object.getLong("item_id"), object.getLong("unit_id"));



        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
