package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.PricelistAdapter;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.PricelistLogic;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 6/4/17.
 */

public class PricelistFragment extends Fragment
{
    private ViewGroup primaryView;
    private final String TAG="PricelistFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        primaryView = (ViewGroup) inflater.inflate(R.layout.pricelist_layout,group,false);
        init();
        return primaryView;
    }

    private void init()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
            @Override
            public void run()
            {
                PricelistLogic logic = new PricelistLogic(new WeakReference<Activity>(getActivity()));
                final PricelistAdapter adapter = new PricelistAdapter(logic);
                final JSONArray array = logic.getPriceList();
                Logger.log(TAG,"Pricelist object: "+array.toString());
                Log.d(TAG, "Pricelist object: " + array.toString());

                //move to UI thread
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        ((ListView)primaryView.findViewById(R.id.price_list)).setAdapter(adapter);
                        if(array.length() > 0)
                        {
                            try
                            {
                                primaryView.findViewById(R.id.empty_price_list_layout).setVisibility(View.GONE);
                                primaryView.findViewById(R.id.price_list).setVisibility(View.VISIBLE);
                                for (int i = 0; i < array.length(); i++)
                                    adapter.addItem(array.getJSONObject(i));
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }

                        }
                    }
                });
            }
        });

    }
}
