package com.geckosolutions.recordrack.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.geckosolutions.recordrack.fragments.NewPurchaseFragment;
import com.geckosolutions.recordrack.fragments.PurchasesFragment;

/**
 * Created by anthony1 on 7/10/17.
 */

public class PurchaseViewPagerAdapter extends FragmentPagerAdapter
{
    private NewPurchaseFragment newPurchaseFragment;
    private PurchasesFragment purchaseFragment;
    public PurchaseViewPagerAdapter(FragmentManager manager)
    {
        super(manager);
    }
    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment = null;
        if(position == 0)
        {
            if(purchaseFragment == null)
                purchaseFragment = new PurchasesFragment();
            fragment = purchaseFragment;
        }
        else
        {
            if(newPurchaseFragment == null)
                newPurchaseFragment = new NewPurchaseFragment();
            fragment = newPurchaseFragment;
        }
        return fragment;
    }

    @Override
    public int getCount()
    {
        return 2;
    }
}
