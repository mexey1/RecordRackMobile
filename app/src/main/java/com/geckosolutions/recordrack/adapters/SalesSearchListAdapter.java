package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Looper;
import android.support.v4.view.ViewPager;
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
import com.geckosolutions.recordrack.activities.SaleViewActivity;
import com.geckosolutions.recordrack.fragments.BaseSearchFragment;
import com.geckosolutions.recordrack.fragments.NewSalesFragment;
import com.geckosolutions.recordrack.fragments.SalesViewPager;
import com.geckosolutions.recordrack.logic.BluetoothPrint;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.SalesViewLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Timestamp;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by anthony1 on 7/3/16.
 * this class holds the result(s) of searches performed via the sales search fragment
 */
public class SalesSearchListAdapter extends BaseAdapter
{
    private LayoutInflater inflater;
    private ArrayList<JSONObject> arrayList;
    private WeakReference<Activity> reference;
    private Dialog dialog;
    private ArrayList<Double> total;
    private String operation;
    private boolean shouldAllowEdit;
    private String mode,columnName;
    private JSONArray result;
    private final String TAG="SalesSearchListAdapter";
    //private boolean edit;

    /**
     *
     * @param reference
     * @param MODE 0 for sales, 1 for purchase
     */
    public SalesSearchListAdapter(WeakReference<Activity> reference, int MODE)
    {
        this.reference = reference;
        inflater = LayoutInflater.from(UtilityClass.getContext());
        arrayList = new ArrayList<>();
        total = new ArrayList<>();

        switch (MODE)
        {
            case 0:
                mode = "sale_transaction";
                columnName = "sale_transaction_id";
                break;
            case 1: mode = "purchase_transaction";
                    columnName = "purchase_transaction_id";
                break;
        }
    }


    public ArrayList getArrayList()
    {
        return arrayList;
    }

    public void addItem(JSONObject object) throws JSONException
    {
        arrayList.add(object);
        //total.add(object.getDouble("cost"));
        //computeTotalAmount();
        notifyDataSetChanged();
    }

    public void clearArrayList()
    {
        //System.out.println("In here");

        arrayList.clear();
        total.clear();
        notifyDataSetChanged();
    }

    /**
     * this method is called to delete an item.
     * @param position the position of the item in the data model
     */
    public void removeItem(final int position)
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject object = arrayList.get(position);
                    String transactID = object.getString(columnName);
                    if(mode.equals("sale_transaction"))
                        DatabaseManager.deleteSaleTransaction(transactID);
                    else if(mode.equals("purchase_transaction"))
                    {
                        Log.d(TAG,"about to delete purchase");
                        Logger.log(TAG,"about to delete purchase");
                        result = DatabaseManager.deletePurchaseTransaction(transactID);
                    }
                    arrayList.remove(position);
                    arrayList.trimToSize();
                    //update the UI
                    new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(result!=null && result.length()>0)
                                displayPurchaseDeletionNotification();
                            notifyDataSetChanged();
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
     * this method is called to display any messages from deletion of purchase entries.
     */
    private void displayPurchaseDeletionNotification()
    {
        try
        {
            dialog = UtilityClass.showCustomDialog(R.layout.notification_layout,reference);
            TextView textView = (TextView) dialog.findViewById(R.id.text);
            String text = "Some purchased items could not be deleted because the quantity to be deleted " +
                    "exceeds what is in stock \n\n";
            StringBuilder builder = new StringBuilder();
            builder.append(text);
            JSONObject object = null;
            for(int i=0; i<result.length();i++)
            {
                object = result.getJSONObject(i);
                builder.append(object.getString("item")+" in "+object.getString("category")+" current quantity is "+
                        object.getString("current_quantity")+", purchased quantity "+object.getString("quantity_to_delete")+"\n");
            }
            textView.setText(builder.toString());

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
    private void computeTotalAmount()
    {
        double sum = 0;
        for(double d : total)
            sum+=d;
        //reference.get().setTotal(sum);
    }
    @Override
    public int getCount()
    {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView= (LinearLayout)inflater.inflate(R.layout.search_result_item,parent,false);
            convertView.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());
            populateViews(convertView, position);
            initializeDeleteAndEditButtons(convertView, position);
            Logger.log(TAG,"New item "+convertView);
            Log.d(TAG,"New item "+convertView);
        }
        else
        {
            populateViews(convertView,position);
            initializeDeleteAndEditButtons(convertView,position);
            Log.d(TAG,"Convert "+convertView);
            Logger.log(TAG,"Convert "+convertView);
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
            JSONObject object = arrayList.get(position);
            Logger.log(TAG,"This is "+object);;
            Log.d(TAG,"This is "+object);;
            if (object.has(columnName))
            {
                String id = object.getString(columnName);
                id = "Txn # | "+id;
                ((TextView) view.findViewById(R.id.transact_id)).setText(id);
            }

            ((TextView)view.findViewById(R.id.customer_name)).setText(object.getString("name"));
            ((TextView)view.findViewById(R.id.amount_paid)).setText(UtilityClass.formatMoney(object.getDouble("amount_paid")));
            ((TextView)view.findViewById(R.id.time)).setText(UtilityClass.getTimeFromDateTime(object.getString("last_edited")));
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
    private void initializeDeleteAndEditButtons(final View view,final int position)
    {
        final LinearLayout editLayout = (LinearLayout)view.findViewById(R.id.edit);
        //if the transaction was suspended, update the text to resume
        try
        {
            final TextView editText = ((TextView) editLayout.findViewById(R.id.edit_text));
            final TextView editIcon = ((TextView) editLayout.findViewById(R.id.icon));
            final String suspended = arrayList.get(position).getString("suspended");
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        //run database access off UI thread
                        Logger.log(TAG,"JSONObject: "+arrayList.get(position));
                        Log.d(TAG,"JSONObject: "+arrayList.get(position));
                        shouldAllowEdit = isEditAllowed(arrayList.get(position).getString(columnName));

                        //populate results on UI thread
                        new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(!shouldAllowEdit)
                                {
                                    editText.setText("View");
                                    editIcon.setText(UtilityClass.getContext().getResources().getString(R.string.view));
                                }
                                else
                                {
                                    operation = "Edit";
                                    editIcon.setText(UtilityClass.getContext().getResources().getString(R.string.edit));
                                    editText.setText(operation);
                                }

                                if(suspended.equals("1"))
                                {
                                    operation = "Resume";
                                    editText.setText(operation);
                                    editIcon.setText(UtilityClass.getContext().getResources().getString(R.string.edit));
                                    //editLayout.setVisibility(View.VISIBLE);
                                }
                                //edit.setVisibility(View.V);
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
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        LinearLayout delete = (LinearLayout)view.findViewById(R.id.delete);
        editLayout.setTag(new Integer(position));
        delete.setTag(new Integer(position));
        //define what happens when edit is selected
        editLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Integer position = (Integer)v.getTag();
                    final int pos = position.intValue();
                    JSONObject object = arrayList.get(position);
                    Logger.log(TAG,"Objected from: "+object);
                    Log.d(TAG,"Objected from: "+object);
                    final String transactionID = object.getString(columnName);

                    //retrieve data on non-UI thread
                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            shouldAllowEdit = isEditAllowed(transactionID);
                            final JSONArray array = UtilityClass.fetchDataForSaleTransaction(transactionID);
                            //populate result on UI thread
                            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    displayResults(array,transactionID);
                                }
                            });
                        }
                    });

                    position = null;
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

        //define what happens when the delete button is pressed
        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               performDelete(v);
            }
        });

        //define what happens when print action is selected
        LinearLayout print = (LinearLayout)view.findViewById(R.id.print);
        print.setTag(new Integer(position));
        print.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Integer position = (Integer)v.getTag();
                final int pos = position.intValue();
                final JSONObject object = arrayList.get(position);
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            //String columns[] = {"name","sale_transaction_id", "amount_paid","suspended","sale_transaction.last_edited"};
                            String transactionID = object.getString(columnName);
                            JSONArray array = UtilityClass.fetchDataForSaleTransaction(transactionID);
                            Logger.log(TAG,"Result for printing : "+array.toString());
                            Log.d(TAG,"Result for printing : "+array.toString());
                            String customerName = array.getJSONObject(0).getString("name");
                            String user = array.getJSONObject(0).getString("user");

                            double total = UtilityClass.removeNairaSignFromString(array.getJSONObject(0).getString("total"));
                            double amountPaid = array.getJSONObject(0).getDouble("amount_paid");
                            String[] dateTime =UtilityClass.getDateAndTime(array.getJSONObject(0).getString("last_edited"));
                            BluetoothPrint bluetoothPrint = new BluetoothPrint(reference,transactionID,customerName,user,total,amountPaid,dateTime,array);
                            //BluetoothPrint bluetoothPrint = new BluetoothPrint(array);
                            Logger.log(TAG,"This is array: "+array);
                            Log.d(TAG,"This is array: "+array);
                            bluetoothPrint.beginPrintingReceipt();
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
     * private method to close dialog and set reference to null
     */
    private void dismissDialog()
    {
        dialog.dismiss();
        dialog = null;
    }

    /**
     * private method to define what happens when the ok button does
     */
    private void initializeEditNotAllowedDialog()
    {
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismissDialog();
            }
        });
        /*dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dismissDialog();
            }
        });*/
    }
    /**
     * this method is called when the delete button is pressed.
     * @param v the transaction whose delete button was pressed.
     */
    private void performDelete(View v)
    {
        final Integer position = (Integer)v.getTag();
        //retrieve transact id. This is run off the UI thread
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String transactID = arrayList.get(position).getString(columnName);
                    shouldAllowEdit = isEditAllowed(transactID);
                    //move processing to UI thread
                    new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(shouldAllowEdit)//if transaction was performed today, allow for delete
                            {
                                dialog = UtilityClass.showCustomDialog(R.layout.confirm_delete_layout,
                                        new WeakReference<Activity>((AppCompatActivity)reference.get()));
                                dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        removeItem(position);
                                        //notifyDataSetInvalidated();
                                        dismissDialog();
                                    }
                                });
                                dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        dismissDialog();
                                    }
                                });
                            }
                            else
                            {
                                dialog = UtilityClass.showCustomDialog(R.layout.edit_not_allowed,
                                        new WeakReference<Activity>((AppCompatActivity)reference.get()));
                                initializeEditNotAllowedDialog();
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
     * method called to display results after they have been retrieved from the database
     * @param array JSONArray containing the results
     * @param transactionID transaction ID for the given transaction.
     */
    private void displayResults(final JSONArray array,final String transactionID)
    {
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                WeakReference<ViewPager> ref = SalesViewPager.getViewPager();
                if(shouldAllowEdit)//if edit is allowed, we want to display the results in the
                                    // NewSalesFragment present in this viewpager
                {
                    //System.out.println(array);
                    NewSalesFragment.getReference().get().displayResults(transactionID, array, operation, shouldAllowEdit);
                    ref.get().setCurrentItem(2, true);
                }
                else//we want to create a dialog and display the results
                {
                    Logger.log(TAG,"Here's the array"+array.toString());
                    Log.d(TAG,"Here's the array"+array.toString());
                    //SaleViewActivity activity = new SaleViewActivity();
                    Dialog dialog = UtilityClass.showCustomDialog(R.layout.sales_view_layout,
                            new WeakReference<Activity>((AppCompatActivity)reference.get()));


                    new SalesViewLogic().displayItems(dialog,array);
                    dialog = null;
                }
            }
        });
    }

    /**
     * method to check if editing of a transaction is allowed.
     * @param transactID transact ID to look for
     * @return true if the transaction was performed today
     */
    private boolean isEditAllowed(String transactID)
    {
        try
        {
            JSONObject object = new JSONObject();
            String columns [] = {"last_edited"};
            object.put("tableName",mode);
            object.put("whereArgs","id = '"+transactID+"'");
            object.put("columns",columns);
            JSONArray array = DatabaseManager.fetchData(object);
            String lastEdited = array.getJSONObject(0).getString("last_edited");
            String now = UtilityClass.getDateTime(0,0,0,0);

            //set variable to determine if edit should be allowed
            shouldAllowEdit = Timestamp.valueOf(lastEdited).after(Timestamp.valueOf(now));

            object = null;
            columns = null;
            array = null;
            lastEdited = null;
            now = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return shouldAllowEdit;
    }

    private void displayTransactionToResume() throws JSONException
    {
        JSONObject object = null;
        EditText category = (EditText)dialog.findViewById(R.id.category);
        EditText item = (EditText)dialog.findViewById(R.id.item);
        EditText unit = (EditText)dialog.findViewById(R.id.unit);
        EditText quantity = (EditText)dialog.findViewById(R.id.unit);
        EditText cost = (EditText)dialog.findViewById(R.id.cost);


        category.setText(object.getString("category"));
        item.setText(object.getString("item"));
        unit.setText(object.getString("unit"));
        quantity.setText(object.getString("quantity"));
        cost.setText(object.getString("cost"));

        category = null;
        item = null;
        unit = null;
        quantity = null;
        cost = null;
        object = null;
    }



    private void addArrayListItem(int position)
    {
        try
        {
            JSONObject object = new JSONObject();
            EditText category = (EditText)dialog.findViewById(R.id.category);
            EditText item = (EditText)dialog.findViewById(R.id.item);
            EditText unit = (EditText)dialog.findViewById(R.id.unit);
            EditText quantity = (EditText)dialog.findViewById(R.id.unit);
            EditText cost = (EditText) dialog.findViewById(R.id.cost);

            object.put("category", category.getText().toString().trim());
            object.put("item", item.getText().toString().trim());
            object.put("unit", unit.getText().toString().trim());
            object.put("quantity", quantity.getText().toString().trim());
            object.put("cost", cost.getText().toString().trim());

            category = null;
            item = null;
            unit = null;
            quantity = null;
            cost = null;
            removeItem(position);
            addItem(object);
            object = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


}
