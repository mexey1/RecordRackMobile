package com.geckosolutions.recordrack.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomDialogFragment;
import com.geckosolutions.recordrack.interfaces.CustomDialogInterface;
import com.geckosolutions.recordrack.interfaces.PaymentInterface;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.DebtorPaymentHistoryLogic;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.PaymentOptions;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 9/10/16.
 * This adapter class is responsible for populating and initializing the various debtors available.
 * It defines what happens when View, Pay, Delete buttons are pressed
 */
public class DebtorsFragmentAdapter extends BaseAdapter implements CustomDialogInterface,PaymentInterface
{

    private JSONArray debtors;
    private LayoutInflater inflater;
    private FragmentManager fm;
    private WeakReference<Activity> activityWeakReference;
    private PaymentOptions paymentOptions;
    private double totalDebt=-1, amountPaid;
    private String dueDate;
    private long debtTransactionID;
    private CustomDialogFragment cdf;
    private int pos;
    private View selectedView;
    private DebtorsFragmentAdapter adapter;
    private final String TAG = "DebtorsFragmentAdapter";
    public DebtorsFragmentAdapter(JSONArray debtors, FragmentManager fm, WeakReference<Activity> ref)
    {
        this.debtors = debtors;
        this.fm = fm;
        this.activityWeakReference = ref;
        inflater = LayoutInflater.from(UtilityClass.getContext());
        adapter = this;
        Log.d(TAG,"Debtors detail: "+debtors);
        Logger.log(TAG,"Debtors detail: "+debtors);
    }
    @Override
    public int getCount()
    {
        return debtors.length();
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
        if(convertView == null)
            convertView = createNewDebtor(position,parent);

        return convertView;
    }

    /**
     * private method to create a new debtor
     * @param position the position of the debtor in the layout/data model
     * @param parent a parent viewgroup needed to instantiate a new layout
     * @return the new layout/debtor
     */
    private View createNewDebtor(final int position, ViewGroup parent)
    {
        final LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.debtor,parent,false);
        layout.findViewById(R.id.debtor_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView());

        try
        {
            JSONObject object = debtors.getJSONObject(position);
            TextView debtor = (TextView) layout.findViewById(R.id.debtor);
            TextView debt = (TextView) layout.findViewById(R.id.debt);
            TextView dd = (TextView) layout.findViewById(R.id.due_date);
            Log.d(TAG,"Debtor details: "+object.toString());
            Logger.log(TAG,"Debtor details: "+object.toString());
            debtor.setText(object.getString("preferred_name"));//set debtor name
            String d = object.getString("last_due_date");//get due date
            long dateDue = UtilityClass.getDateAsLong(d);
            int diff = UtilityClass.getDateDifferenceFromToday(dateDue);//get diff between today's date and due date
            String text = "Due date:";
            ForegroundColorSpan span = null;

            if(diff == 0)//if due date is today, mark it as orange
                span = new ForegroundColorSpan(UtilityClass.getContext().getResources().getColor(R.color.orange1));
            else if(diff > 0)//if in the future, mark it green
                span = new ForegroundColorSpan(UtilityClass.getContext().getResources().getColor(R.color.turquoise));
            else //if due, mark it pomegranate
                span = new ForegroundColorSpan(UtilityClass.getContext().getResources().getColor(R.color.pomegranate));

            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            stringBuilder.append("Due date: "+UtilityClass.getDate(dateDue));
            stringBuilder.setSpan(span,text.length(),stringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            dd.setText(stringBuilder);
            debt.setText(UtilityClass.formatMoney(object.getDouble("debt")));

            //set the tag for the current debtor to the position value passed during this method call
            layout.findViewById(R.id.view).setTag(position);
            layout.findViewById(R.id.pay).setTag(position);
            //define what happens when the view button is pressed
            layout.findViewById(R.id.view).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    final int tag = (Integer)v.getTag();
                    Log.d(TAG,"User pressed the View button");
                    Logger.log(TAG,"User pressed the View button");
                    cdf = new CustomDialogFragment();
                    cdf.onDialogShow(adapter);
                    Bundle bundle  = new Bundle();
                    //what xml would be inflated
                    bundle.putInt("xml",R.layout.debt_payment_layout);
                    cdf.setArguments(bundle);
                    cdf.setHeight(500);
                    cdf.show(fm,"DebtorPayment");
                    //((TextView)cdf.getDialog().findViewById(R.id.name)).setText(debtors.getJSONObject(tag).getString("name"));
                    pos = tag;

                }
            });

            //define what happens when the pay button is pressed
            layout.findViewById(R.id.pay).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    //move query to database thread.
                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                //check to see if there are multiple debts associated with this debtor.
                                JSONObject object = debtors.getJSONObject((int)v.getTag());
                                String id =object.getString("id");
                                JSONObject query = new JSONObject();
                                query.put("columnArgs",new String[]{"debt_transaction.id","min(balance) as balance"});
                                query.put("tableName","debtor");
                                String q1 = UtilityClass.getJoinQuery("debt_transaction","debtor.id=debt_transaction.debtor_id");
                                String q2 = UtilityClass.getJoinQuery("debt_payment","debt_transaction.id=" +
                                        "debt_payment.debt_transaction_id");
                                query.put("whereArgs","debtor_id="+id+" ");
                                query.put("join",q1+q2);
                                query.put("extra"," GROUP BY debt_payment.debt_transaction_id");
                                final JSONArray result = DatabaseManager.fetchData(query);
                                //move to UI thread
                                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            //if multiple debts exist, ask user to choose which to pay for
                                            if(result.length() > 1)
                                                chooseTransactionToPayFor(result,v);
                                                //if only one debt exists, set necessary parameters and display payment options
                                            else
                                            {
                                                totalDebt = result.getJSONObject(0).getDouble("balance");
                                                debtTransactionID = result.getJSONObject(0).getLong("id");
                                                if(totalDebt >0)
                                                    displayPaymentOptions(v);
                                                else
                                                    UtilityClass.showToast("This debt has been fully paid.");
                                            }
                                            Log.d(TAG,"This is all debts owed by customer :"+result.toString());
                                            Logger.log(TAG,"This is all debts owed by customer :"+result.toString());
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
                    });
                }
            });
            stringBuilder = null;
            span = null;
            text = null;
            debt = null;
            debtor = null;
            dueDate = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return layout;
    }

    /**
     * this method is defined in the CustomDialogInterface
     */
    @Override
    public void initDialog()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new DebtorPaymentHistoryLogic().onViewPressed(cdf,debtors.getJSONObject(pos));
                    cdf = null;
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveDebtPaymentToDatabase(final int position)
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.d(TAG,"About to save debt payment to database");
                    Logger.log(TAG,"About to save debt payment to database");
                    JSONObject object = debtors.getJSONObject(position);
                    Log.d(TAG,"Debtor details: "+object.toString());
                    Logger.log(TAG,"Debtor details: "+object.toString());
                    if (dueDate == null)
                        dueDate = UtilityClass.getDateTimeForSql(UtilityClass.getCurrentDateTimeMillis());
                    String debtorID = object.getString("id");// debtTransactionID = null;
                    /*object = null;
                    object = new JSONObject();
                    object.put("tableName","debt_transaction");
                    object.put("columns",new String[]{"id"});
                    object.put("whereArgs","debtor_id="+debtorID);
                    JSONArray array = DatabaseManager.fetchData(object);
                    if(array.length() > 0)
                        debtTransactionID = array.getJSONObject(0).getString("id");
                    //make changes here
                    //DatabaseManager.makeDebtPayment(Long.parseLong(debtorID),Long.parseLong(debtTransactionID),totalDebt,amountPaid,dueDate);*/
                    DatabaseManager.makeDebtPayment(Long.parseLong(debtorID),debtTransactionID,totalDebt,amountPaid,dueDate,false);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void chooseTransactionToPayFor(final JSONArray array, final View pay) throws JSONException
    {
        double balance =0;
        Dialog dialog = UtilityClass.showCustomDialog(R.layout.transaction_selection_layout,activityWeakReference);
        LayoutInflater inflater = LayoutInflater.from(UtilityClass.getContext());
        LinearLayout container = (LinearLayout) dialog.findViewById(R.id.container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,UtilityClass.convertToPixels(50));
        for (int i=0; i<array.length(); i++)
        {
            JSONObject object = array.getJSONObject(i);
            TextView textView = (TextView) inflater.inflate(R.layout.textview,container,false);

            //we have to check if the balance for the transaction is greater than 0
            balance = Double.parseDouble(object.getString("balance"));
            if (balance>0)
            {
                textView.setText(object.getString("balance"));
                textView.setTag(object.getInt("id"));
                textView.setLayoutParams(params);
                textView.setClickable(true);
                textView.setTag(i);
                textView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        try
                        {
                            Log.d(TAG,"Transaction to pay for was chosen.");
                            Logger.log(TAG,"Transaction to pay for was chosen.");
                            totalDebt = array.getJSONObject((int)v.getTag()).getDouble("balance");
                            debtTransactionID = array.getJSONObject((int)v.getTag()).getLong("id");
                            displayPaymentOptions(pay);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                container.addView(textView);
            }
        }
    }

    private void displayPaymentOptions(View v)
    {
        //define what happens when the pay button is pressed
        //we create a payment interface object
        /*PaymentInterface pi = new PaymentInterface()
        {
            @Override


        };*/
        selectedView = v;
        WeakReference<PaymentInterface> reference = new WeakReference<PaymentInterface>(this);
        paymentOptions = new PaymentOptions(reference);
    }

    public double getTotal()
    {
        return totalDebt;
        //double t = 0;
                /*try
                {
                    JSONObject object = debtors.getJSONObject((int)v.getTag());
                    totalDebt = Double.parseDouble(object.getString("debt"));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }*/
    }

    @Override
    public void setAmountPaid(double a)
    {
        amountPaid = a;
    }

    @Override
    public double getAmountPaid()
    {
        return amountPaid;
    }

    @Override
    public long getClientID()
    {
        long t = 0;
        try
        {
            JSONObject object = debtors.getJSONObject((int)selectedView.getTag());
            t = Long.parseLong(object.getString("client_id"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public void setClientID(long clientID)
    {

    }

    @Override
    public void setDueDate(String d)
    {
        dueDate = d;
    }

    @Override
    public void onSaveToDatabase(String suspend, String archived)
    {
        saveDebtPaymentToDatabase((int)selectedView.getTag());
    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return activityWeakReference;
    }

    @Override
    public PaymentOptions getPaymentOptions()
    {
        return paymentOptions;
    }

    @Override
    public long getDebtTransactionID()
    {
        return debtTransactionID;
    }
}
