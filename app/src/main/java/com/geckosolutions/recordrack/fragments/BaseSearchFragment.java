package com.geckosolutions.recordrack.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.geckosolutions.recordrack.R;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 6/5/16.
 * This is the base search fragment class that all other search fragments inherit from
 */
public abstract class BaseSearchFragment extends Fragment
{
    protected LinearLayout primaryView;
    protected static WeakReference<BaseSearchFragment> reference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
         primaryView = (LinearLayout)inflater.inflate(R.layout.search_layout,group,false);
         reference = new WeakReference(this);
         /*primaryView.findViewById(R.id.search_field).setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus)
             {
                 if (hasFocus)
                     primaryView.findViewById(R.id.search_layout).setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.search_view_focused));
                 else
                     primaryView.findViewById(R.id.search_layout).setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.search_view_unfocused));
             }
         });*/

        initSearch();
        return primaryView;
    }

    /**
     * this method should be implemented to initialize the search bar in the fragment
     */
    protected abstract void initSearch();
}
