package com.geckosolutions.recordrack.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.SalesViewPagerAdapter;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.custom.FontAwesomeTextView;
import com.geckosolutions.recordrack.fragments.StockFragment;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.QuantityPriceRelationship;
import com.geckosolutions.recordrack.logic.Tuples;
import com.geckosolutions.recordrack.logic.UnitRelationship;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 5/2/16.
 */
public class ModifiedStockActivity extends AppCompatActivity
{
    private ActionBar actionBar;
    private CustomEditText category,item,unit,quantity,price;
    private byte count;
    private TextView advanced;
    private Button submit;
    private String categoryText,itemText,unitText,quantityText,priceText;
    private FontAwesomeTextView info, moreUnits;
    private WeakReference<Activity> reference;
    private static WeakReference<ModifiedStockActivity> ref;
    private Dialog dialog;
    private UnitRelationship unitRelationship;
    private QuantityPriceRelationship quantityPriceRelationship;
    private Tuples[] quantityPrice;
    private String [] units;
    private int [] values;//this represent the base unit equivalent


    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.modified_stocks_layout);
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.turquoise));

        //actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        reference = new WeakReference<Activity>(this);
        ref = new WeakReference<ModifiedStockActivity>(this);
        init();
        //actionBar.setDisplayShowCustomEnabled(true);
    }

    private void init()
    {
        category = (CustomEditText)findViewById(R.id.category);
        item = (CustomEditText)findViewById(R.id.item);
        unit = (CustomEditText)findViewById(R.id.unit);
        quantity = (CustomEditText)findViewById(R.id.quantity);
        price = (CustomEditText)findViewById(R.id.price);
        price.enableTextChangedListener();
        info = (FontAwesomeTextView)findViewById(R.id.info);
        moreUnits = (FontAwesomeTextView)findViewById(R.id.more_units);

        info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String text = reference.get().getResources().getString(R.string.base_unit_description);
                UtilityClass.showMessageDialog(reference,"Base unit description", Html.fromHtml(text));
            }
        });

        moreUnits.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(unit.getText().toString().isEmpty())
                {
                    unit.requestFocus();
                    UtilityClass.showToast("Please enter a base unit to continue");
                }
                else
                {
                    //dialog = UtilityClass.showCustomDialog(R.layout.unit_relationship_layout,reference);
                    dialog = UtilityClass.showCustomDialog(R.layout.sales_info_item,reference);
                    //add unit relationship view to layout
                    //unitRelationship = new UnitRelationship((LinearLayout)dialog.findViewById(R.id.parent),ref);
                    ((ViewGroup)dialog.findViewById(R.id.parent)).addView(unitRelationship.getUnitRelationShipLayout());

                    //add quantity and price, then set it's visibility to GONE
                    //quantityPriceRelationship = new QuantityPriceRelationship((LinearLayout)dialog.findViewById(R.id.parent),ref);
                    ((ViewGroup)dialog.findViewById(R.id.parent)).addView(quantityPriceRelationship.getQuantityPriceLayout());
                    quantityPriceRelationship.getQuantityPriceLayout().setVisibility(View.GONE);
                    //((LinearLayout) dialog.findViewById(R.id.container)).addView(quantity.getUnitRelationShipLayout());

                    //quantityPriceRelationship.setUnitRelationshipWeakReference(new WeakReference<UnitRelationship>(unitRelationship));
                    unitRelationship.setQuantityPriceRelationshipWeakReference(new WeakReference<QuantityPriceRelationship>(quantityPriceRelationship) );

                    //set references to null
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {
                        @Override
                        public void onDismiss(DialogInterface d)
                        {
                            unitRelationship = null;
                            quantityPriceRelationship = null;
                            dialog = null;
                        }
                    });
                }
            }
        });

        advanced = (TextView)findViewById(R.id.advanced);
        advanced.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //startActivity(new Intent(UtilityClass.getContext(), NewStockActivity.class));
            }
        });

        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                categoryText = category.getText().toString();
                itemText = item.getText().toString();
                quantityText = quantity.getText().toString();
                unitText = unit.getText().toString();
                priceText = price.getText().toString();
                if(categoryText.length() > 0 && itemText.length() > 0 && unitText.length() > 0 &&
                        ((quantityText.length()> 0 && priceText.length() > 0) || values!=null))
                {
                    DatabaseThread thread = DatabaseThread.getDatabaseThread();
                    //thread.postDatabaseTask(runnable);
                }
                else
                    UtilityClass.showToast("You have one or more empty fields");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
        //return super.onOptionsItemSelected(item);
    }

    /**
     * returns a reference to the dialog used by this class
     * @return dialog
     */
    public Dialog getDialog()
    {
        return dialog;
    }

    /**
     * returns a reference to the unit relationship object.
     * @return unit relationship object.
     */
    public UnitRelationship getUnitRelationship()
    {
        return unitRelationship;
    }

    /**
     * returns a reference to quantity price relationship
     * @return quantity price relationship object.
     */
    public QuantityPriceRelationship getQuantityPriceRelationship()
    {
        return quantityPriceRelationship;
    }

    public static String getBaseUnit()
    {
        return ref.get().unit.getText().toString();
    }

    /**
     * this method is called when the unit relationship has been completely entered and the user
     * presses submit.
     */
    public void onUnitRelationshipCompleted()
    {
        if(dialog!= null && dialog.isShowing())
            dialog.dismiss();
        units = unitRelationship.getUnits();
        values = unitRelationship.getValues();
        quantityPrice = quantityPriceRelationship.getQuantityAndPrice();
        unitRelationship = null;
        quantityPriceRelationship = null;

        //we would like to disable the quantity and price edit-texts
        quantity.setEnabled(false);
        price.setEnabled(false);

        //we'd also like to change the views info and more unit to reveal view and edit options
        findViewById(R.id.info).setVisibility(View.GONE);
        findViewById(R.id.more_units).setVisibility(View.GONE);
        findViewById(R.id.view).setVisibility(View.VISIBLE);
        findViewById(R.id.delete).setVisibility(View.VISIBLE);
    }


}



