package com.geckosolutions.recordrack.logic;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.CustomerClass;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.interfaces.CalendarInterface;
import com.geckosolutions.recordrack.interfaces.PaymentInterface;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 4/25/17.
 * This class is responsible for providing payment options dialog during checkout. It provides the
 * user different options of accepting payments.
 */


public class PaymentOptions implements CalendarInterface
{
    /**
     * this method creates, initializes and displays the dialog when the cash method of payment is
     * selected.
     */
    private Dialog paymentOptionDialog,cashOptionDialog,customerDialog;
    private WeakReference<PaymentInterface> ref;
    private double balanceValue, amountEntered,amountPaid;
    private String total;
    private CalendarInterface calendarInterface;
    private final String TAG = "PaymentOptions";

    public PaymentOptions(WeakReference<PaymentInterface>reference)
    {
        Log.d(TAG,"This is Reference: "+reference);
        this.ref = reference;
        Log.d(TAG,"This is PaymentInterface object: "+ref.get());
        calendarInterface = this;
        total = UtilityClass.formatMoney(ref.get().getTotal());
        //this.reference = new WeakReference<Activity>(((Fragment)reference.get()).getActivity());
        showPaymentMethodDialog();
    }

    /**
     * this private method creates and displays the payment method dialog
     */
    private void showPaymentMethodDialog()
    {
        Log.d(TAG,"This is PaymentInterface object in payment method1: "+ref.get());
        Logger.log(TAG,"This is PaymentInterface object in payment method1: "+ref.get());

        if(paymentOptionDialog != null && paymentOptionDialog.isShowing())
            paymentOptionDialog.dismiss();
        if(cashOptionDialog != null && cashOptionDialog.isShowing())
            cashOptionDialog.dismiss();

        paymentOptionDialog = null;
        Log.d(TAG,"This is PaymentInterface object in payment method2: "+ref.get());
        paymentOptionDialog = UtilityClass.showCustomDialog(R.layout.payment_method, ref.get().getActivityWeakReference());

        ((TextView)paymentOptionDialog.findViewById(R.id.amount_due)).setText(total);

        //this handles what happens when cash option method is selected.
        paymentOptionDialog.findViewById(R.id.cash_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showCashOptionDialog();
            }
        });

        paymentOptionDialog.findViewById(R.id.suspend).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paymentOptionDialog.dismiss();
                transactionCompleted("1","0");
            }
        });

        //this handles what happens when the credit option is clicked. it basically displays a
        //date picker dialog and responds to user's actions.
        paymentOptionDialog.findViewById(R.id.credit_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new CalendarClass(calendarInterface,"Select a date when this debt should be paid");
            }
        });
    }

    /**
     * method called to display the cash option payment dialog
     */
    private void showCashOptionDialog()
    {
        if(paymentOptionDialog != null && paymentOptionDialog.isShowing())
            paymentOptionDialog.dismiss();
        if(cashOptionDialog != null && cashOptionDialog.isShowing())
            cashOptionDialog.dismiss();

        paymentOptionDialog = null;
        cashOptionDialog = null;

        cashOptionDialog = UtilityClass.showCustomDialog(R.layout.cash_checkout_layout, ref.get().getActivityWeakReference());
        ((TextView)cashOptionDialog.findViewById(R.id.total)).setText(total);
        ((CustomEditText)cashOptionDialog.findViewById(R.id.amount_paid)).enableTextChangedListener();
        cashOptionDialog.findViewById(R.id.back).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showPaymentMethodDialog();
            }
        });
        //this piece monitors the amount being entered and calculates the change by subtracting the value from total
        ((EditText)cashOptionDialog.findViewById(R.id.amount_paid)).addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                TextView total = (TextView)cashOptionDialog.findViewById(R.id.total);
                TextView balance = (TextView)cashOptionDialog.findViewById(R.id.balance);

                double totalValue = UtilityClass.removeNairaSignFromString(total.getText().toString());
                double amount = UtilityClass.removeNairaSignFromString(s.toString());

                Log.d(TAG,"Total to be paid:"+totalValue);
                Log.d(TAG,"amount entered:"+amount);

                if(amount == -1)//error occurred parsing the money string
                {
                    balance.setText("");
                    return;
                }

                balanceValue = amount - totalValue;
                Log.d(TAG,"balance:"+balanceValue);
                amountEntered = amount;
                if(balanceValue < 0)
                    balance.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.pomegranate));
                else
                    balance.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.turquoise));

                balance.setText(UtilityClass.formatMoney(Math.abs(balanceValue)));

                balance = null;
                total = null;
            }
        });

        //this handles what happens when submit is clicked.
        cashOptionDialog.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                amountPaid+=amountEntered;
                Log.d(TAG,"amount paid:"+amountPaid);
                Log.d(TAG,"This is PaymentInterface object when submit clicked: "+ref.get());
                Logger.log(TAG,"amount paid:"+amountPaid);
                Logger.log(TAG,"This is PaymentInterface object when submit clicked: "+ref.get());
                if(balanceValue < 0)
                {
                    total=  UtilityClass.formatMoney(Math.abs(balanceValue));
                    showPaymentMethodDialog();
                    Logger.log(TAG,"amount paid "+amountPaid+" balance "+balanceValue);
                    Log.d(TAG,"amount paid "+amountPaid+" balance "+balanceValue);
                }
                else
                    transactionCompleted("0","0");
            }
        });
    }

    /**
     * method called when the dialog has fulfilled its purpose
     * @param suspend should the transaction be suspended
     * @param archive should the transaction be archived
     */
    public void transactionCompleted(String suspend, String archive)
    {
        ref.get().setAmountPaid(amountPaid);
        ref.get().onSaveToDatabase(suspend,archive);

        if(cashOptionDialog!= null && cashOptionDialog.isShowing())
            cashOptionDialog.dismiss();
        if(paymentOptionDialog!= null && paymentOptionDialog.isShowing())
            paymentOptionDialog.dismiss();
    }

    /**
     * this method is called from the calendar class to set the date for payment of the debt
     * @param datePicked the date chosen by the user
     */
    @Override
    public void onDatePicked(long datePicked)
    {
        int days = UtilityClass.getDateDifferenceFromToday(datePicked);
        if(days < 0)
        {
            UtilityClass.showToast("Due date cannot be before Today...Please select a date in the future date");
            return;
        }
        ref.get().setDueDate(UtilityClass.getDateTimeForSql(datePicked));
        if(ref.get().getClientID() ==0)
            new CustomerClass(ref);
        else
            ref.get().getPaymentOptions().transactionCompleted("0","0");
    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return  ref.get().getActivityWeakReference();
    }

}
