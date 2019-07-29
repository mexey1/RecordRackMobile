package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.DebtorsFragmentAdapter;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 9/10/16.
 */
public class DebtorsFragment extends Fragment
{
    private CoordinatorLayout coordinatorLayout;
    private LayoutInflater layoutInflater;
    private ListView debtorList;
    private static WeakReference<DebtorsFragment> reference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        coordinatorLayout = (CoordinatorLayout)inflater.inflate(R.layout.debtor_layout,group,false);
        layoutInflater = inflater;
        reference = new WeakReference<DebtorsFragment>(this);
        init();
        return coordinatorLayout;
    }

    private void init()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(queryDebtors);

    }

    private static Runnable queryDebtors = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                JSONObject object = new JSONObject();

                object.put("tableName","debtor");
                object.put("columnArgs",new String[]{"debtor.id","debt","last_due_date","preferred_name","client_id"});
                object.put("join",UtilityClass.getJoinQuery("client","debtor.client_id = client.id"));
                final JSONArray array = DatabaseManager.fetchData(object);


                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(array.length() > 0)
                        {
                            DebtorsFragment fragment = reference.get();
                            fragment.debtorList = (ListView)fragment.coordinatorLayout.findViewById(R.id.debtor_list);
                            fragment.debtorList.setAdapter(new DebtorsFragmentAdapter(array,fragment.getChildFragmentManager()
                                                                            , new WeakReference<Activity>(fragment.getActivity())));
                            fragment.coordinatorLayout.findViewById(R.id.empty_debtor_layout).setVisibility(View.GONE);
                            fragment.debtorList.setVisibility(View.VISIBLE);
                            fragment = null;
                        }
                    }
                });

                //System.out.println(array);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };
}
