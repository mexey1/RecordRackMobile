package com.geckosolutions.recordrack.logic;

import android.app.Dialog;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.SaleViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by anthony1 on 9/14/16.
 */
public class SalesViewLogic
{
    private ListView listView;
    private SaleViewAdapter adapter;
    private JSONArray array;
    private Dialog dialog;

    private void init()
    {
        adapter = new SaleViewAdapter();
        listView = (ListView)dialog.findViewById(R.id.items_list);
        listView.setAdapter(adapter);
    }

    public void displayItems(Dialog dialog,JSONArray array)
    {
        try
        {
            this.dialog = dialog;
            init();
            JSONObject object = array.getJSONObject(0);
            TextView name = (TextView)dialog.findViewById(R.id.customer_name);
            TextView date = (TextView)dialog.findViewById(R.id.date);
            TextView timeField = (TextView)dialog.findViewById(R.id.time);
            TextView total = (TextView)dialog.findViewById(R.id.total);

            Timestamp timestamp = Timestamp.valueOf(object.getString("last_edited"));
            long time = timestamp.getTime();

            String dateString = UtilityClass.getDate(time)+" ";
            String timeString  = UtilityClass.getTimeFromDateTime(object.getString("last_edited"));

            name.setText(object.getString("name"));
            date.setText(dateString);
            timeField.setText(timeString);
            for (int count = 0; count<array.length();count++)
                adapter.addItem(array.getJSONObject(count));

            total.setText(UtilityClass.formatMoney(adapter.getSum()));
            name = null;
            date = null;
            timestamp = null;
            //calendar = null;
            dateString = null;
            timeString = null;
            timeField = null;
            total = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void finalize()
    {
        dialog = null;
        adapter = null;
        array = null;
    }

}
