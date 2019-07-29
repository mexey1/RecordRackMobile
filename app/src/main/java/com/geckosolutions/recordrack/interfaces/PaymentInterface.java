package com.geckosolutions.recordrack.interfaces;

import android.app.Activity;

import com.geckosolutions.recordrack.logic.PaymentOptions;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 2/1/16.
 * Interface has to be implemented to interact with the payment options dialog
 */
public interface PaymentInterface
{
    /**
     * implement this method to pass to the payment dialog the total amount to be paid
     */
    public double getTotal();

    /**
     * implement this method to get the amount paid by customer
     * @param amountPaid
     */
    public void setAmountPaid(double amountPaid);

    /**
     * method for retrieving the amount paid
     * @return the amount paid as a double.
     */
    public double getAmountPaid();

    /**
     * implement this method to return a valid transction id. Return -1 if you want a transanct id
     * to be generated for you.
     * @return the transaction id
     */
    public long getDebtTransactionID();

    public long getClientID();
    public void setClientID(long clientID);
    public void setDueDate(String dueDate);
    //public void saveToDatabase(String suspend, String archived);

    //this method is called to save data to the database. It attempts to run it off
    //the UI thread.
    public void onSaveToDatabase(String suspend, String archived);
    public WeakReference<Activity> getActivityWeakReference();
    public PaymentOptions getPaymentOptions();
}
