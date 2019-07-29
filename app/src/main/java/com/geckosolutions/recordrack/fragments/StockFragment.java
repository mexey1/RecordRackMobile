package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.ModifiedStockActivity;
import com.geckosolutions.recordrack.adapters.StockListAdapter;
import com.geckosolutions.recordrack.custom.CustomDialogFragment;
import com.geckosolutions.recordrack.interfaces.CustomDialogInterface;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.NewStockAdditionLogic;
import com.geckosolutions.recordrack.logic.StockFragmentLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * This fragment displays the stocks that are currently in the database.
 * Created by anthony1 on 1/22/16.
 */
public class StockFragment extends Fragment implements CustomDialogInterface
{
    private RelativeLayout relativeLayout;
    private Button button;
    private LayoutInflater layoutInflater;
    //private Dialog dialog;
    private ViewPager viewPager;
    private ProgressDialog dialog;
    private ListView listView;
    private static WeakReference<StockFragment> stockFragmentWeakReference;
    private static WeakReference<StockListAdapter> adapter;
    private static WeakReference<Dialog> dialogWeakReference;
    private static WeakReference<RelativeLayout> relativeLayoutWeakReference ;
    private CustomDialogFragment cdf;
    private static int position = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        relativeLayout = (RelativeLayout) inflater.inflate(R.layout.stock_layout,group,false);
        layoutInflater = inflater;
        stockFragmentWeakReference = new WeakReference<StockFragment>(this);
        init();
        return relativeLayout;
    }

    private void init()
    {
        WeakReference<Activity> ref = new WeakReference<Activity>((AppCompatActivity)getActivity());
        dialog = UtilityClass.getProgressDialog(ref);
        listView = (ListView)relativeLayout.findViewById(R.id.stock_list);
        adapter = new WeakReference<StockListAdapter>(new StockListAdapter(ref,
                        new WeakReference<android.support.v4.app.FragmentManager>(getChildFragmentManager())));
        dialogWeakReference = new WeakReference<Dialog>(dialog);
        relativeLayoutWeakReference = new WeakReference<RelativeLayout>(relativeLayout);
        listView.setAdapter(adapter.get());

        fetchStockData();
        dialog.show();

        button = (Button) relativeLayout.findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cdf = new CustomDialogFragment();
                cdf.onDialogShow(stockFragmentWeakReference.get());
                Bundle bundle = new Bundle();
                bundle.putInt("xml",R.layout.new_product_parent_layout);
                cdf.setArguments(bundle);
                cdf.setHeight(500);
                cdf.show(getChildFragmentManager(),"NewStock");


//                new NewStockAdditionLogic().init(cdf);
                //showNewStockActivity();
            }
        });
    }

    //initialize the dialog here
    @Override
    public void initDialog()
    {
        new NewStockAdditionLogic().init(cdf);
    }

    private void showNewStockActivity()
    {
        getActivity().startActivity(new Intent(UtilityClass.getContext(), ModifiedStockActivity.class));
    }

    public void fetchStockData()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(stockQuery);
    }

    /**
     * queries the database to retrieve stocks
     */
    private static Runnable stockQuery = new Runnable()
    {
        @Override
        public void run()
        {
            final JSONArray result = StockFragmentLogic.getStockList(true);
            //do UI related events, like displaying results.
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if(result.length() > 0)
                    {
                        if(relativeLayoutWeakReference.get() != null)
                        {
                            relativeLayoutWeakReference.get().findViewById(R.id.empty_stock_layout).setVisibility(View.GONE);
                            relativeLayoutWeakReference.get().findViewById(R.id.stock_list).setVisibility(View.VISIBLE);
                            adapter.get().clearStockArray();
                        }
                        //add each JSONObject to the adapter.
                        for (int count = 0; count < result.length(); count++)
                        {
                            try
                            {
                                if(adapter.get() != null)
                                    adapter.get().addItem(result.getJSONObject(count));
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(dialogWeakReference!=null && dialogWeakReference.get() != null)
                        dialogWeakReference.get().dismiss();
                    dialogWeakReference = null;
                }
            });
        }
    };

    public static WeakReference<StockFragment> getReference()
    {
        return stockFragmentWeakReference;
    }
}
