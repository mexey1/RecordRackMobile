package com.geckosolutions.recordrack.fragments;

import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by anthony1 on 5/22/16.
 */
public class NewPurchaseFragment extends NewSalesFragment
{


    protected String tableName = "purchase_item";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        View view = super.onCreateView(inflater,group,savedInstance);
        setTextHint("Supplier's name");
        isUsedByPurchase = true;
        return view;
    }

    @Override
    public boolean onDoneButtonPressed()
    {
        if (sharedLayoutNew.isAnyFieldEmpty())
        {
            UtilityClass.showToast("You have empty fields");
            return false;
        }
        double quantity = Double.parseDouble(((EditText) dialog.findViewById(R.id.quantity)).getText().toString());
        if(quantity == 0)
            UtilityClass.showToast("Quantity entered should be greater than zero ...");
        else
        {
            dialog.dismiss();
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                @Override
                public void run()
                {
                    addNewItem();
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
        ArrayList list = adapter.getArrayList();
        //long transact_id = DatabaseManager.getLastTransactID(tableName);//+1;
        try
        {
            JSONObject object = new JSONObject();
            JSONObject object1 = null;

            String name = null;
            if(((EditText)primaryView.findViewById(R.id.customer_name)).getText().length() == 0)
                name = "Vendor";
            else
                name = ((EditText)primaryView.findViewById(R.id.customer_name)).getText().toString();
            //insert the sale details into the sale_transaction table. The returned id would be used
            //in the sale_item table
            object = new JSONObject();
            object.put("tableName", "purchase_transaction");
            object.put("name", name);
            object.put("suspended", suspend);
            object.put("archived",archived);
            object.put("created", UtilityClass.getDateTime());
            object.put("last_edited", UtilityClass.getDateTime());
            object.put("user_id", UtilityClass.getCurrentUserID());
            object.put("amount_paid",getAmountPaid());
            object.put("ded_frm_rev",purchasePaymentIsFromSales?1:0);
            long purchase_transaction_id = DatabaseManager.insertData(object);
            object = null;
            int count = 0;
            while (count < list.size())
            {
                object1 = (JSONObject)list.get(count);
                //save the items purchased
                object = new JSONObject();
                object.put("tableName", tableName);
                String itemID = object1.getString("item_id");
                String unitID = object1.getString("unit_id");
                String quantity = object1.getString("quantity");
                int bue = DatabaseManager.getBaseUnitEquivalent(Long.parseLong(itemID),
                        Long.parseLong(unitID));
                double qty = Double.parseDouble(quantity)*bue;

                object.put("purchase_transaction_id", purchase_transaction_id);
                object.put("item_id",itemID);
                object.put("unit_id",unitID);
                object.put("unit_price",object1.getString("unit_price"));
                object.put("cost",object1.getString("cost"));
                object.put("quantity",object1.getString("quantity"));
                object.put("_quantity",Double.toString(qty));
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
                    //UtilityClass.showToast("Data inserted "+qty);
                    DatabaseManager.updateCurrentQuantity(itemID, Double.toString(qty),0);
                }
                    itemID = null;
                    unitID = null;
                    quantity = null;
                object = null;
                count++;
            }
            UtilityClass.showToast("Purchases were saved properly");
            //clear the data structure backing up the listview
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    adapter.clearArrayList();
                    //resetValues();
                }
            });
            //reset the transaction id to null
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


}