package com.geckosolutions.recordrack.logic;

import android.app.Activity;
import android.app.Dialog;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.ModifiedStockActivity;
import com.geckosolutions.recordrack.adapters.UnitRelationshipAdapter;
import com.geckosolutions.recordrack.custom.CustomEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 1/13/17.
 */

public class QuantityPriceRelationship
{
    private ViewGroup layout;
    private LayoutInflater inflater;
    private ViewGroup group;
    private Tuples tuples[];
    private JSONArray array;
    private NewStockAdditionLogic stockAdditionLogic;
    private final String TAG="QuantityPriceRelation";
    //private WeakReference<UnitRelationship> unitRelationshipWeakReference;

    public QuantityPriceRelationship(ViewGroup group, NewStockAdditionLogic logic)
    {
        this.group = group;
        stockAdditionLogic = logic;
        inflater = LayoutInflater.from(UtilityClass.getContext());
        layout = group;
        //define what happens when the previous button is pressed
        //define what happens when the submit button is pressed
        layout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onSubmitPressed();
            }
        });
        //((ListView)layout.findViewById(R.id.container)).setAdapter(new UnitRelationshipAdapter());
    }

    /**
     * this method defines what happens when the submit button is pressed
     */
    private void onSubmitPressed()
    {
        array = null;
        array = new JSONArray();
        JSONObject object = null;
        LinearLayout container = (LinearLayout)layout.findViewById(R.id.container);
        int count  = container.getChildCount();
        tuples = new Tuples[count];
        String quantityText=null,priceText = null;

        for (int i=0; i<count; i++)
        {
            tuples[i] = new Tuples<Double,Double>();
            quantityText = ((EditText)container.getChildAt(i).findViewById(R.id.quantity)).getText().toString();
            priceText = ((EditText)container.getChildAt(i).findViewById(R.id.price)).getText().toString();

            if(quantityText.length()==0 || priceText.length()==0)
            {
                UtilityClass.showMessageDialog(new WeakReference<Activity>(stockAdditionLogic.getFragment().getActivity()),
                                                "Empty fields", new SpannableString("The fields cannot be left empty. If you do not have the item in this unit, put a zero (0) instead"));
                return;
                //UtilityClass.showToast("");
            }
            double quantity = Double.parseDouble(quantityText);
            double price = UtilityClass.removeNairaSignFromString(priceText);
            tuples[i].setValues(quantity,price);
        }

        for (int i = 0; i<tuples.length;i++)
        {
            Logger.log(TAG,"First value::"+tuples[i].getFirst()+" Second Value::"+tuples[i].getSecond());
            Log.d(TAG, "First value::" + tuples[i].getFirst() + " Second Value::" + tuples[i].getSecond());
        }


        stockAdditionLogic.onDoneButtonPressed();
    }

    public ViewGroup getQuantityPriceLayout()
    {
        return layout;
    }

    /**
     * This method is called to display the various units entered by the user.
     * @param units an array containing all units entered in the unit relationship layout.
     */
    public void setUnits(String... units)
    {
        ViewGroup container = (ViewGroup) layout.findViewById(R.id.container);
        if(container.getChildCount() == 0)
        {
            for(int count=0; count<units.length; count++)
            {
                if(units[count] != null)
                {
                    ViewGroup child = (ViewGroup) inflater.inflate(R.layout.quantity_price_value, group, false);
                    ((CustomEditText)child.findViewById(R.id.price)).enableTextChangedListener();
                    ((TextView) child.findViewById(R.id.unit)).setText(units[count]);
                    container.addView(child);
                }
            }
        }
    }

    /**
     * this method is called to retrieve the different quantities
     * @return
     */
    public Tuples[] getQuantityAndPrice()
    {
        return tuples;
    }
    @Override
    public void finalize()
    {
        group = null;
        layout = null;
        //reference = null;
        inflater = null;
        group = null;
        array = null;
    }

    /**
     *
     * @param reference

    public void setUnitRelationshipWeakReference(WeakReference<UnitRelationship> reference)
    {
        unitRelationshipWeakReference = reference;
    }*/


}
