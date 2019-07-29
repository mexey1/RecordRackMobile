package com.geckosolutions.recordrack.fragments;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geckosolutions.recordrack.R;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 9/26/16.
 */
public class ExpenseFragment extends IncomeFragment
{
    //private CoordinatorLayout coordinatorLayout;
    //private LayoutInflater layoutInflater;
    //private WeakReference<IncomeFragment> reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        coordinatorLayout = (CoordinatorLayout)inflater.inflate(R.layout.expense_fragment,group,false);
        layoutInflater = inflater;
        reference = new WeakReference<IncomeFragment>(this);
        mode = 1;
        tableName = "expense";
        init();
        return coordinatorLayout;
    }
}
