package com.geckosolutions.recordrack.activities;

import android.app.Activity;
import android.app.Dialog;
import android.nfc.Tag;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.interfaces.PaymentInterface;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * This class is responsible for handling operations dealing with a customer
 * Created by anthony1 on 9/7/16.
 */
public class CustomerClass
{
    private Dialog customerDialog;
    private static String text;
    private static WeakReference<CustomerClass> reference;
    private JSONArray searchResult;
    private LinearLayout popupLayout;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private static WeakReference<PaymentInterface> paymentInterfaceWeakReference;
    private EditText searchField,customerName,address,phoneNumber;
    private long clientID;
    private String name,phone,add;
    private final String TAG = "CustomerClass";
    /**
     * this method is called to display the customer dialog when the credit option is chosen at
     * checkout.
     */

    public CustomerClass(WeakReference<PaymentInterface> ref)
    {
        paymentInterfaceWeakReference = ref;
        reference = new WeakReference<CustomerClass>(this);
        layoutInflater = LayoutInflater.from(UtilityClass.getContext());
        displayCustomerDialog();
    }

    private void displayCustomerDialog()
    {
        customerDialog = UtilityClass.showCustomDialog(R.layout.new_customer,
                                paymentInterfaceWeakReference.get().getActivityWeakReference());
        searchField = (EditText)customerDialog.findViewById(R.id.phone_number_field);
        customerName = ((EditText) customerDialog.findViewById(R.id.customer_name));
        address = ((EditText) customerDialog.findViewById(R.id.address));
        phoneNumber = (EditText)customerDialog.findViewById(R.id.phone_number);
        //monitor the phone number field as a user types
        searchField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                text = s.toString();
                DatabaseThread.getDatabaseThread().postDatabaseTask(customerSearch);

            }
        });

        //define what happens when the add button is pressed
        /*customerDialog.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(phoneNumberField.getText().toString().trim().length() == 11)
                    onCustomerSelected();//this is not supposed to be here. there's no harm in deleting this entire method
                else
                    UtilityClass.showToast("Phone number must be 11 digits");
            }
        });*/

        //here, we define what happens when the user decides to add a new customer
        customerDialog.findViewById(R.id.add_customer).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showCustomerdetails();
            }
        });

        //define what happens when the done button is pressed
        customerDialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                name = customerName.getText().toString().trim();
                add = address.getText().toString().trim();
                phone = phoneNumber.getText().toString().trim();
                if(name.length() == 0 || add.length() == 0 || phone.length() == 0)
                {
                    UtilityClass.showToast("You have empty fields. Please provide some information about the customer ");
                    return;
                }
                DatabaseThread.getDatabaseThread().postDatabaseTask(saveNewCustomer);
                /*if(searchResult==null || searchResult.length() == 0)
                    DatabaseThread.getDatabaseThread().postDatabaseTask(saveNewCustomer);
                else
                {
                    onCustomerSelected();
                    customerDialog.dismiss();
                    customerDialog = null;
                }*/
            }
        });

        //define what happens when the cancel button is pressed
        customerDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                reference.get().customerDialog.dismiss();
                reference.get().customerDialog = null;
            }
        });
    }

    private void showCustomerdetails()
    {
        reference.get().customerDialog.findViewById(R.id.add_customer_desc).setVisibility(View.GONE);
        reference.get().customerDialog.findViewById(R.id.customer_details).setVisibility(View.VISIBLE);
    }

    /**
     * this method is called when we've saved the customer we want. We go ahead returning the
     * client id and closing the payment options dialog.
     */
    private void onCustomerSelected()
    {
        paymentInterfaceWeakReference.get().setClientID(clientID);
        paymentInterfaceWeakReference.get().getPaymentOptions().transactionCompleted("0","0");
    }

    /**
     * runnable for searching for customer phone number
     */
    private static Runnable customerSearch = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                synchronized (reference.get())
                {
                    JSONObject object = new JSONObject();
                    object.put("tableName","client");
                    object.put("columns",new String[]{"id","preferred_name","address","phone_number"});
                    String whereArgs = " phone_number LIKE '%" + text + "%' LIMIT 20";
                    object.put("whereArgs",whereArgs);
                    reference.get().searchResult = DatabaseManager.fetchData(object);

                    //call this on the UI thread
                    new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //get reference to the search layout
                            View view = reference.get().customerDialog.findViewById(R.id.search_layout);
                            TextView userIcon = (TextView) view.findViewById(R.id.user_icon);
                            if(reference.get().searchResult.length() > 0)
                            {
                                if(reference.get().searchField.getText().toString().trim().length() == 0)
                                {
                                    Log.d(reference.get().TAG,"Empty search result. Dialog would be closed");
                                    Logger.log(reference.get().TAG,"Empty search result. Dialog would be closed");
                                    userIcon.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.battleship_grey));
                                    reference.get().closePopupWindow();
                                    return;
                                }
                                else
                                {
                                    Log.d(reference.get().TAG,"Search returned result.");
                                    Logger.log(reference.get().TAG,"Search returned result.");
                                    userIcon.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.turquoise));
                                }
                                //userIcon = null;
                            }
                            else if(reference.get().searchResult.length() == 0)
                            {
                                Log.d(reference.get().TAG,"Empty search result. Dialog would be closed!!");
                                Logger.log(reference.get().TAG,"Empty search result. Dialog would be closed!!");
                                if(reference.get().searchField.getText().toString().trim().length() == 0)
                                    userIcon.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.battleship_grey));
                                else
                                    userIcon.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.pomegranate));
                                reference.get().closePopupWindow();
                                //UtilityClass.showToast("Popup closed");
                                //-userIcon = null;
                                return;
                            }
                            reference.get().displaySuggestion();
                        }
                    });
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };

    private static Runnable saveNewCustomer = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                //before inserting into the database, we'd have to check if client already exists
                long result = DatabaseManager.getClientID(reference.get().phone);
                Log.d(reference.get().TAG,"Saving new customer");
                Logger.log(reference.get().TAG,"Saving new customer");
                if(result == 0)//no user currently exists with the phone number
                {
                    JSONObject object = new JSONObject();
                    object.put("tableName","client");
                    object.put("preferred_name",reference.get().name);
                    object.put("address",reference.get().add);
                    object.put("phone_number",reference.get().phone);
                    object.put("alternate_phone_number",reference.get().phone);
                    object.put("created",UtilityClass.getDateTime());
                    object.put("last_edited",UtilityClass.getDateTime());
                    object.put("user_id",UtilityClass.getCurrentUserID());
                    reference.get().clientID = DatabaseManager.insertData(object);
                    Log.d(reference.get().TAG,"New client id is:"+reference.get().clientID);
                    Logger.log(reference.get().TAG,"New client id is:"+reference.get().clientID);
                    object = null;
                }
                else
                {
                    reference.get().clientID = result;
                    reference.get().onCustomerSelected();
                }

                //close the popup window
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        reference.get().onCustomerSelected();
                        reference.get().closePopupWindow();
                        reference.get().customerDialog.dismiss();
                        reference.get().customerDialog = null;
                    }
                });

            }
            catch (JSONException e)
            {
                e.printStackTrace();;
            }
        }
    };

    /**
     * this method closes the popup window
     */
    public void closePopupWindow()
    {
        if(popupWindow != null && popupWindow.isShowing())
            popupWindow.dismiss();
        popupWindow = null;
        popupLayout = null;
        //customerDialog = null;
    }

    /**
     * this method is called to display suggestions in a popup window after the result
     * of the search has been returned.
     */
    private void displaySuggestion()
    {
        Log.d(reference.get().TAG,"Display suggestion called");
        Logger.log(reference.get().TAG,"Display suggestion called");
        if(popupLayout != null)
            popupLayout.removeAllViews();
        else
        {
            popupWindow = UtilityClass.showPopupWindow(customerDialog.findViewById(R.id.search_layout), (ViewGroup) customerDialog.findViewById(R.id.search_layout));
            popupLayout = (LinearLayout) reference.get().popupWindow.getContentView().findViewById(R.id.popup_layout);
        }

        int count = 0;
        while(count < searchResult.length())
        {
            try
            {
                Log.d(TAG,"YOLO");
                Logger.log(TAG,"YOLO");
                final JSONObject object = searchResult.getJSONObject(count);
                String name = object.getString("preferred_name");
                String phoneNumber = object.getString("phone_number");
                name = name.length()>0?name : "Customer";
                LinearLayout parent = (LinearLayout) layoutInflater.inflate(R.layout.popup_window_item,popupLayout,false);
                ((TextView)parent.findViewById(R.id.name)).setText(name);
                ((TextView)parent.findViewById(R.id.phone_number)).setText(phoneNumber);
                parent.setTag(object);
                int width = popupLayout.getWidth() - UtilityClass.convertToPixels(10);
                //define what happens when a customer is selected
                parent.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        try
                        {
                            JSONObject object1 = (JSONObject) v.getTag();
                            reference.get().phoneNumber.setText(object1.getString("phone_number"));
                            reference.get().address.setText(object1.getString("address"));
                            reference.get().customerName.setText(object1.getString("preferred_name"));
                            reference.get().clientID = object1.getLong("id");
                            showCustomerdetails();
                            reference.get().closePopupWindow();
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                popupLayout.addView(parent);

                if(!popupWindow.isShowing())
                {
                    View anchor = customerDialog.findViewById(R.id.search_layout);
                    int w = (((ViewGroup) customerDialog.findViewById(R.id.search_layout)).getWidth() - width)/2;
                    popupWindow.showAsDropDown(anchor,w,10);
                }

                Log.d(TAG,"is showing?"+popupWindow.isShowing());
                Log.d(TAG,"layout "+popupLayout.getWidth()+":"+popupLayout.getHeight());
                Log.d(TAG,"window "+popupWindow.getWidth()+":"+popupWindow.getHeight());

                Logger.log(TAG,"is showing?"+popupWindow.isShowing());
                Logger.log(TAG,"layout "+popupLayout.getWidth()+":"+popupLayout.getHeight());
                Logger.log(TAG,"window "+popupWindow.getWidth()+":"+popupWindow.getHeight());

                parent = null;

                count++;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}
