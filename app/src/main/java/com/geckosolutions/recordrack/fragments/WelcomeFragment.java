package com.geckosolutions.recordrack.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.ControlActivity;
import com.geckosolutions.recordrack.adapters.GridViewAdapter;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 8/5/17.
 */

public class WelcomeFragment extends Fragment
{
    private LinearLayout parent;
    private GridView gridView;
    private WeakReference<ControlActivity> reference;
    private LayoutInflater layoutInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        parent = (LinearLayout) inflater.inflate(R.layout.welcome_to_record_rack,group,false);
        layoutInflater = inflater;

        gridView = ((GridView)parent.findViewById(R.id.grid_view));
        gridView.setAdapter(new GridViewAdapter());
        init();
        return parent;
    }

    public void setReferenceToControlActivity(WeakReference<ControlActivity> reference)
    {
        this.reference = reference;
    }

    private void init()
    {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d("Selected item","Hello were");
                //UtilityClass.showToast("Hello there");
                //System.out.println("Hello were");
                reference.get().preOnItemClicked(position);
            }
        });
    }
}
