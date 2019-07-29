package com.geckosolutions.recordrack.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.MetricAffectingSpan;
import android.text.style.TextAppearanceSpan;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.SaleViewAdapter;
import com.geckosolutions.recordrack.fragments.NewSalesFragment;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by anthony1 on 9/12/16.
 */
public class SaleViewActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sales_view_layout);
        //reference = new WeakReference<SaleViewActivity>(this);

        //listView = (ListView)findViewById(R.id.items_list);
        //init();
    }


}
