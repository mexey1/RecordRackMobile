package com.geckosolutions.recordrack.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.StoreListAdapter;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 8/14/17.
 */

public class StoresListActivity extends AppCompatActivity
{
    private JSONArray storeList;
    private LayoutInflater inflater;
    private ListView listView;
    private LinearLayout parent;
    private StoreListAdapter adapter;
    private Button newStore;
    private String email;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.store_list_activity);
            Intent intent = getIntent();
            storeList = new JSONArray(intent.getStringExtra("store_list"));
            email = getIntent().getStringExtra("email");
            inflater = LayoutInflater.from(UtilityClass.getContext());
            listView = (ListView) findViewById(R.id.list_view);
            adapter = new StoreListAdapter(new WeakReference<Activity>(this));
            newStore = (Button)findViewById(R.id.new_store);
            listView.setAdapter(adapter);
            init();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void init()
    {
        try
        {
            JSONObject object = null;
            for (int i=0; i<storeList.length();i++)
            {
                object = storeList.getJSONObject(i);
                adapter.addItem(object);
            }

            //enable new store button
            newStore.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(UtilityClass.getContext(),StoreDetails.class);
                    intent.putExtra("email",email);
                    startActivity(intent);
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
