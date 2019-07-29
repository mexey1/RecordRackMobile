package com.geckosolutions.recordrack.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.SharedLayout;
import com.geckosolutions.recordrack.activities.SharedLayoutNew;
import com.geckosolutions.recordrack.adapters.NewSalesListAdapter;
import com.geckosolutions.recordrack.interfaces.PaymentInterface;
import com.geckosolutions.recordrack.logic.BluetoothPrint;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.PaymentOptions;
import com.geckosolutions.recordrack.logic.ReceiptPrintingThread;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * this fragment handles processing of new sales. If a sale is to be performed, this is the
 * fragment that handles it.
 * Created by anthony1 on 1/26/16.
 */
public class NewSalesFragment extends Fragment implements PaymentInterface
{
    private LinearLayout newItem,itemLayout;
    protected CoordinatorLayout primaryView;
    private ImageView imageView;
    protected LayoutInflater layoutInflater;
    private HorizontalScrollView hScrollView;
    private ListView listView;
    private FloatingActionButton addButton;
    protected NewSalesListAdapter adapter;

    protected String tableName = "sale_item";
    protected Dialog dialog;
    private TextView totalField;
    //protected SharedLayout sharedLayout;
    protected  SharedLayoutNew sharedLayoutNew;
    private Button checkOut;
    private String  total,transactionID,operation,dueDate;
    private double totalAmount,balanceValue,amountPaid,amountEntered;
    private DatePicker datePicker;
    private static WeakReference<NewSalesFragment> reference;
    private boolean shouldAllowEdit = true;
    private int position;
    private static volatile String text;
    private long clientID;
    protected boolean isUsedByPurchase = false, purchasePaymentIsFromSales;
    private String name = null;
    private ArrayList list;
    private final String TAG="NewSalesFragment";


    private PaymentOptions paymentOptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        //primaryView = (LinearLayout)inflater.inflate(R.layout.new_sales_layout, group, false);
        primaryView = (CoordinatorLayout)inflater.inflate(R.layout.new_sales_layout, group, false);
        reference = new WeakReference<NewSalesFragment>(this);
        layoutInflater = inflater;
        init();
        return primaryView;
    }

    protected void setTextHint(String text)
    {
        ((EditText)primaryView.findViewById(R.id.customer_name)).setHint(text);
    }

    /**
     * initialize basic functionalities
     */
    protected void init()
    {
        addButton = (FloatingActionButton)primaryView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayNewItemDialog(null,-1,0);
            }
        });

        //this handles what happens when checkout button is clicked
        primaryView.findViewById(R.id.checkout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(adapter.getCount() > 0)
                {
                    //saveToDatabase();
                    total = null;
                    total = ((TextView)primaryView.findViewById(R.id.total)).getText().toString();
                    paymentOptions = new PaymentOptions(new WeakReference<PaymentInterface>(reference.get()));
                }
            }
        });
        listView = (ListView)primaryView.findViewById(R.id.items_list);
        adapter = new NewSalesListAdapter(new WeakReference<>(this));
        listView.setAdapter(adapter);
        totalField = (TextView)primaryView.findViewById(R.id.total);

        //define what happens when the option button(Suspend) is clicked
        primaryView.findViewById(R.id.options).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(adapter.getCount() > 0)
                {
                    final Dialog dialog = UtilityClass.showCustomDialog(R.layout.suspend_resume_options,
                            new WeakReference<Activity>((AppCompatActivity)getActivity()));
                    dialog.findViewById(R.id.suspend).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            tableName = "suspend";
                            saveToDatabase("1","0");
                        }
                    });

                    dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        primaryView.findViewById(R.id.options).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = UtilityClass.showCustomDialog(R.layout.sales_layout_options,new WeakReference<Activity>(getActivity()));
                dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                        dialog = null;
                    }
                });

                dialog.findViewById(R.id.suspend_layout).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        suspendTransaction();
                    }
                });

                dialog.findViewById(R.id.delete_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        clearTransaction();
                    }
                });
            }
        });
    }

    /**
     * this method is called to suspend the current transaction.
     * For now this method has not been implemented
     */
    private void suspendTransaction()
    {

    }

    private void clearTransaction()
    {
        adapter.clearArrayList();
        resetValues();
        dialog.dismiss();
        dialog = null;
    }

    /**
     * this method is called to create and display the dialog for adding a new item
     * @return a reference to the dialog
     */
    public Dialog displayNewItemDialog(JSONObject object,double currentQuantity,int mode)
    {

        //dialog is created here
        dialog = UtilityClass.showCustomDialog(R.layout.item_for_sale, new WeakReference<Activity>((AppCompatActivity) getActivity()));
        //set the itemID and categoryID incase the user doesn't modify this view
        sharedLayoutNew = new SharedLayoutNew((LinearLayout) dialog.findViewById(R.id.parent_view),mode);
        if(object !=null)
        {
            sharedLayoutNew.setJSONDataForItemDisplayed(object);
            sharedLayoutNew.setCurrentQuantity(currentQuantity);
        }
        ((LinearLayout) dialog.findViewById(R.id.parent_view)).addView(sharedLayoutNew.getMainLayout());

        //done button is told what to do here
        dialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (sharedLayoutNew.isAnyFieldEmpty())
                    UtilityClass.showToast("You have empty fields");
                else
                {
                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                onDoneButtonPressed();
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

        //this is done to properly close and free up resources used by the popup window
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                //sharedLayout.closePopupWindow();
                Logger.log(TAG,"Dialog is dismissed");
                Log.d(TAG,"Dialog is dismissed");
            }
        });

        return dialog;
    }
    /**
     * this method describes what happens when the done button is pressed
     * @return returns true or false if the user completed the addition of a new item or not
     */
    public boolean onDoneButtonPressed() throws JSONException
    {
        if (sharedLayoutNew.isAnyFieldEmpty())
        {
            UtilityClass.showToast("You have empty fields");
            return false;
        }
        double quantity = sharedLayoutNew.getQuantity();
        quantity*= DatabaseManager.getBaseUnitEquivalent(sharedLayoutNew.getItemID(),sharedLayoutNew.getUnitID());
        Logger.log(TAG,"Current quantity: "+sharedLayoutNew.getCurrentQuantity() +": sold quantities: "+adapter.getSoldQuantities(sharedLayoutNew.getItemID())+" quantity entered: "+quantity);
        Log.d(TAG,"Current quantity: "+sharedLayoutNew.getCurrentQuantity() +": sold quantities: "+adapter.getSoldQuantities(sharedLayoutNew.getItemID())+" quantity entered: "+quantity);

        double quantityRemaining = sharedLayoutNew.getCurrentQuantity() - adapter.getSoldQuantities(sharedLayoutNew.getItemID()) - quantity;
        if (quantityRemaining < 0)
        {
            UtilityClass.showToast("Quantity entered exceeds what's in stock ...");
            return false;
        }
        if(quantity == 0)
        {
            UtilityClass.showToast("Quantity entered should be greater than zero ...");
            return false;
        }
        else
        {
            addNewItem();
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    dialog.dismiss();
                }
            });
        }
        return true;
    }

    /**
     * this private method is called when the checkout button is pressed. It attempts to save
     * data entered into the database. For now, this method is run on the UI thread, like every other
     * method that has to do with database, it has to go off the UI thread.
     */
    protected void saveToDatabase(String suspend, String archived)
    {
        list = adapter.getArrayList();
        //Hashtable<Long,Double> itemsSold = adapter.getAddedItems();
        //long transact_id = DatabaseManager.getLastTransactID(tableName);//+1;
        try
        {
            JSONObject object = null;
            JSONObject object1 = null;
            if(transactionID != null && operation!=null && operation.equals("Resume"))
            {
                //delete entries from the sale transaction table
                object1 = new JSONObject();
                object1.put("tableName","sale_transaction");
                object1.put("whereArgs"," id = '"+transactionID+"'");
                DatabaseManager.deleteData(object1);

                //delete entries from the sale item table
                object1 = new JSONObject();
                object1.put("tableName","sale_item");
                object1.put("whereArgs"," sale_transaction_id = '"+transactionID+"'");
                DatabaseManager.deleteData(object1);
                object1 = null;
            }
            else if(transactionID!=null && operation!=null && operation.equals("Edit"))//perform operations for when the operation is edit
            {
                /***********ALGORITHM FOR EDITS*************
                 * i. re-add the quantities sold to the current quantity
                 * ii. flag the transaction as archived
                 */
                 DatabaseManager.deleteSaleTransaction(transactionID);
                Logger.log(TAG,"Transaction deleted");
                Log.d(TAG,"Transaction deleted");
            }

            if(((EditText)primaryView.findViewById(R.id.customer_name)).getText().length() == 0)
                name = "Customer";
            else
                name = ((EditText)primaryView.findViewById(R.id.customer_name)).getText().toString();
            //insert the sale details into the sale_transaction table. The returned id would be used
            //in the sale_item table
            object = new JSONObject();
            object.put("tableName", "sale_transaction");
            object.put("name", name);
            object.put("suspended", suspend);
            object.put("amount_paid",amountPaid);
            object.put("total",totalAmount);
            object.put("archived",archived);
            object.put("created", UtilityClass.getDateTime());
            object.put("last_edited", UtilityClass.getDateTime());
            object.put("user_id", UtilityClass.getCurrentUserID());
            long sale_transaction_id = DatabaseManager.insertData(object);
            transactionID = Long.toString(sale_transaction_id);
            object = null;
            int count = 0;
            double balance = amountPaid;
            while (count < list.size())
            {
                object1 = (JSONObject)list.get(count);
                Logger.log(TAG,"Item being sold "+object1.toString());
                Log.d(TAG,"Item being sold "+object1.toString());
                //save the items sold
                object = new JSONObject();
                object.put("tableName", tableName);//table name is sale_item/suspend
                object.put("sale_transaction_id", sale_transaction_id);
                object.put("item_id",object1.getString("item_id"));
                object.put("unit_id",object1.getString("unit_id"));
                object.put("unit_price",object1.getString("unit_price"));
                if(balance > object1.getDouble("cost"))
                {
                    object.put("cost", object1.getDouble("cost"));
                    balance-=object1.getDouble("cost");
                }
                else
                {
                    object.put("cost",balance);
                    balance = 0;
                }
                object.put("quantity",object1.getString("quantity"));
                double _quantity = Double.parseDouble(object1.getString("quantity"))*
                                    DatabaseManager.getBaseUnitEquivalent(Long.parseLong(object1.getString("item_id")),Long.parseLong(object1.getString("unit_id")));
                object.put("_quantity",_quantity);
                object.put("currency",UtilityClass.getCurrency());
                object.put("archived",archived);

                //add amount paid and balance later
                object.put("created", UtilityClass.getDateTime());
                object.put("last_edited", UtilityClass.getDateTime());
                object.put("user_id", UtilityClass.getCurrentUserID());
                DatabaseManager.insertData(object);
                object = null;

                //update current quantity table iff transaction is not suspended and not archived
                if(suspend.equals("0") && archived.equals("0"))
                {
                    object = new JSONObject();
                    object.put("tableName", "current_quantity");
                    object.put("item_id",object1.getString("item_id"));
                    //object.put("unit_id", object1.getString("unit_id"));
                    //add provision to perform all quantity operations in base unit
                    double currentQuantity = DatabaseManager.retrieveCurrentQuantity(object1.getLong("item_id"));//,object1.getLong("unit_id"));    //object1.getDouble("current_quantity");
                    //int bue = DatabaseManager.getBaseUnitEquivalent(object1.getLong("item_id"),object1.getLong("unit_id"));//retrieve base unit equivalent for the given item and unit
                    //double quantity = Double.parseDouble(object1.getString("quantity"))*bue;
                    //Double.parseDouble(object1.getString("quantity"));
                    object.put("quantity",currentQuantity - _quantity);
                    object.put("created",UtilityClass.getDateTime());
                    object.put("last_edited",UtilityClass.getDateTime());
                    object.put("user_id", UtilityClass.getCurrentUserID());

                    long id = DatabaseManager.insertData(object);
                    if(id == -1)//if the item already exists in the db, we update instead of insert
                    {
                        String where = "item_id = '"+object1.getString("item_id")+"'";//+"' AND unit_id ='"+object1.getString("unit_id")+"'";
                        DatabaseManager.updateData(object, where);
                    }
                }
                object = null;
                count++;
            }

            Log.d(TAG,"total amount:"+totalAmount);
            Log.d(TAG,"amount paid:"+amountPaid);
            balanceValue = totalAmount - amountPaid;
            Log.d(TAG,"balance:"+balanceValue);
            Log.d(TAG,"total = "+totalAmount+": amountPaid "+amountPaid+": balance "+balanceValue);
            if(balanceValue > 0)
                DatabaseManager.saveDebtTransaction(clientID,totalAmount,amountPaid,dueDate,"sale",sale_transaction_id);


            UtilityClass.showToast("Sales entries were saved properly");
            //clear the data structure backing up the listview
            //reset values
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    adapter.clearArrayList();
                    resetValues();
                }
            });


            //print recipt on receipt printing thread
            if(Build.VERSION.SDK_INT >= 23)
            {
                //first we'd check if we have the coarse location permission
                //if true, we'd proceed...else we'd request
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
                if(permissionCheck== PackageManager.PERMISSION_DENIED)
                {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);
                    Logger.log(TAG,"Bluetooth discovery starting");
                    Log.d(TAG,"Bluetooth discovery starting");
                }
                else
                    proceedWithPrinting();
            }
            else
                proceedWithPrinting();

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * this method is called to print the receipt for the sale transaction
     */
    private void proceedWithPrinting()
    {
        BluetoothPrint bluetoothPrint = new BluetoothPrint(new WeakReference<Activity>((Activity)getActivity()),
                name,UtilityClass.getCurrentUserName(),list,totalAmount,amountPaid,UtilityClass.getDateAndTime(),transactionID);
        //UtilityClass.getD
        bluetoothPrint.beginPrintingReceipt();
    }

    /**
     * this method overrides the method defined in PaymentInterface. It checks to see if it's running
     * on the UI thread or not.
     * @param suspend string indicating if the entry should be suspended.
     * @param archived string indicating if the entry should be archived.
     */
    @Override
    public void onSaveToDatabase(final String suspend, final String archived)
    {
        if(isUsedByPurchase)
        {
            final Dialog dialog = UtilityClass.showCustomDialog(R.layout.confirm_delete_layout,new WeakReference<Activity>(getActivity()));
            ((TextView)dialog.findViewById(R.id.text)).setText("Would you like this payment to be deducted from today's revenue?");
            ((Button)dialog.findViewById(R.id.confirm)).setText("Yes");
            ((Button)dialog.findViewById(R.id.cancel)).setText("No");

            dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    purchasePaymentIsFromSales = true;
                    proceedSaveToDatabase(suspend,archived);
                    dialog.dismiss();
                }
            });

            dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    purchasePaymentIsFromSales = false;
                    proceedSaveToDatabase(suspend,archived);
                    dialog.dismiss();
                }
            });
        }
        else
            proceedSaveToDatabase(suspend,archived);
    }


    private void proceedSaveToDatabase(final String suspend, final String archived)
    {
        if(Looper.getMainLooper().getThread() == Thread.currentThread())
        {
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
            {
                @Override
                public void run()
                {
                    saveToDatabase(suspend,archived);
                }
            });
        }
        else
            saveToDatabase(suspend,archived);
    }

    /**
     * private method to reset values to default in preparation for a new transaction
     */
    private void resetValues()
    {
        transactionID = null;
        operation = null;
        amountPaid = 0;
        amountEntered = 0;
        balanceValue = 0;
        totalField.setText("");
        ((EditText)primaryView.findViewById(R.id.customer_name)).setText("");
    }
    /**
     * this method is called when the done button is clicked. It pushes the data to the
     * adapter backing up the sales list.
     */
    protected void addNewItem()
    {
        try
        {
            JSONObject object = new JSONObject();

            object.put("category",sharedLayoutNew.getCategory());
            object.put("item",sharedLayoutNew.getItemName());
            object.put("item_id",sharedLayoutNew.getItemID());
            object.put("unit",sharedLayoutNew.getUnit());
            object.put("unit_id",sharedLayoutNew.getUnitID());
            object.put("quantity",sharedLayoutNew.getQuantity());
            object.put("current_quantity",sharedLayoutNew.getCurrentQuantity());
            object.put("unit_price",sharedLayoutNew.getUnitPrice());
            object.put("cost",sharedLayoutNew.getCost());

            adapter.addItem(object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * method to display sold items for editing purposes
     * @param transactID the id for the transaction in question
     * @param array a JSON array representing each item to be added
     * @param operation a string flag that indicates if the operation being performed is one of
     *                  edit or resuming a suspended transaction.
     * @param shouldAllowEdit boolean to determine if the item can be edited or not.
     */
    public void displayResults(String transactID,final JSONArray array, String operation, boolean shouldAllowEdit)
    {
        adapter.clearArrayList();
        totalField.setText("");
        totalAmount = 0;
        balanceValue = 0;
        amountPaid = 0;
        amountEntered = 0;
        this.transactionID = transactID;
        this.operation = operation;
        this.shouldAllowEdit = shouldAllowEdit;
        this.disableFab(shouldAllowEdit);
        adapter.clearArrayList();

        try
        {
            ((EditText)primaryView.findViewById(R.id.customer_name)).setText(array.getJSONObject(0).getString("name"));
            //listView.removeAllViews();
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        for(int i=0; i<array.length();i++)
                            adapter.addItem(array.getJSONObject(i));
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


    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return  new WeakReference<Activity>(getActivity());
    }

    /**
     * for debts, the client id is passed in here from the customer class after a customer has been
     * selected to associate with the debt.
     * @param clientID
     */
    @Override
    public void setClientID(long clientID)
    {
        this.clientID = clientID;
        //saveToDatabase("0","0");
        Logger.log(TAG,"This is the client id "+clientID);
        Log.d(TAG,"This is the client id "+clientID);
    }

    public long getClientID()
    {
        return 0;
    }

    /**
     * method to set the due date
     * @param dueDate
     */
    @Override
    public void setDueDate(String dueDate)
    {
        this.dueDate = dueDate;
    }
    /**
     * private method to enable or disable the floating action button
     * @param enable
     */
    private void disableFab(boolean enable)
    {
        addButton.setEnabled(enable);
    }

    /**
     * method called from the payment option class to set the amount actually paid
     * @param a the amount paid
     */
    @Override
    public void setAmountPaid(double a)
    {
        amountPaid = a;
    }

    /**
     * method for retrieving the amount paid
     * @return the amount paid
     */
    public double getAmountPaid()
    {
        return amountPaid;
    }
    /**
     * returns a reference to this fragment
     * @return
     */
    public static WeakReference<NewSalesFragment> getReference()
    {
        return reference;
    }
    /**
     * method to retrieve whether the item is editable.
     * @return
     */
    public static boolean shouldAllowEdit()
    {
        return reference.get().shouldAllowEdit;
    }

    /**
     * this method is called to return the total for all the sales in this transaction
     * @return the total amount of money for this sale
     */
    @Override
    public double getTotal()
    {
        return totalAmount;
    }

    /**
     * this method is called from the adapter associated with this class to set the total value
     */
    public void setTotal(double t)
    {
        totalAmount = t;
        totalField.setText(UtilityClass.formatMoney(t));
    }

    @Override
    public PaymentOptions getPaymentOptions()
    {
        return paymentOptions;
    }

    @Override
    public long getDebtTransactionID()
    {
        return -1;
    }

    public void setItemBeingEdited(String itemBeingEdited,  long id, long unitID)
    {
        sharedLayoutNew.setItemBeingEdited(itemBeingEdited,id, unitID);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults)
    {
        //System.out.println("Response received");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Logger.log(TAG,"Permission granted");
            Log.d(TAG,"Permission granted");
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
            {
                @Override
                public void run()
                {
                    proceedWithPrinting();
                }
            });
        }
    }
}