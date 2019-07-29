package com.geckosolutions.recordrack.adapters;


import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 7/15/17.
 * Most of the logic used here was copied from SalesSearchListAdapter. It's a bad practice to do so,
 * but I couldn't help it...:'(
 */

public class PurchasesListAdapter extends SalesSearchListAdapter
{
    public PurchasesListAdapter(WeakReference<Activity> reference)
    {
        super(reference,1);
    }
}
