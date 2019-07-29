package com.geckosolutions.recordrack.logic;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.ModifiedStockActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by anthony1 on 1/9/17.
 * This class is responsible for handling what happens in the UnitRelationship side when registering
 * a new product for the first time.
 */

public class UnitRelationship
{
    //private LinearLayout layout;
    private ViewGroup group;
    private  String [] units;
    private int [] values;//this represents base unit equivalent
    private LayoutInflater inflater;
    private ArrayList<View> views;
    private volatile int count,pos;//pos is used with views while count is used with units
    private Dialog dialog;
    private QuantityPriceRelationship qrp;
    private WeakReference<QuantityPriceRelationship> quantityPriceRelationshipWeakReference;
    private ViewGroup parent;
    private final String TAG="UnitRelationship";
    private NewStockAdditionLogic stockAdditionLogic;


    public UnitRelationship(ViewGroup grp, NewStockAdditionLogic logic)
    {
        //create parent container for
        inflater = LayoutInflater.from(UtilityClass.getContext());
        //create the layout for unit relationship
        parent = grp;//(LinearLayout) inflater.inflate(R.layout.unit_relationship_layout,grp,false);
        units = new String[5];//only 4 unit relationship is permitted
        values = new int[5];
        views = new ArrayList<>();
        group = grp;
        stockAdditionLogic = logic;
        parent.post(new Runnable()
        {
            @Override
            public void run()
            {
                //add first unit and set value
                View view = newUnit(inflater,parent);
                //((TextView)view.findViewById(R.id.unit1)).setText("Default Base");
                addViewToLayout(view);
                //add base unit to the list of units
                //values[count] = 1;
                //units[count++] = "Default Base";
            }
        });

        //define what happens when the plus button is pressed
        parent.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAddClicked();
            }
        });

        //define what happens when the next button is clicked
        parent.findViewById(R.id.next).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                onNextPressed();
            }
        });

        //define what happens when help button is pressed

        parent.findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                stockAdditionLogic.displayHelpDialog(R.layout.sub_units_entry_help);
            }
        });
    }

    /**
     * defines what happens when the next button is pressed.
     */
    private void onNextPressed()
    {
        if(getLastUnit().length() == 0)
        {
            UtilityClass.showToast("You have one or more empty fields");
            return;
        }

        if(stockAdditionLogic != null)
        {
            units[count] = getLastUnit();
            populateValues();
            stockAdditionLogic.onUnitRelationshipCompleted(units);
            //msa.getDialog().setContentView(msa.getQuantityPriceRelationship().getQuantityPriceLayout());
            Logger.log(TAG,"Units");
            for(int i=0; i< units.length;i++)
            {
                Logger.log(TAG,"Units::"+units[i] == null?"null":units[i] +" "+values[i]);
                Log.d(TAG,"Units::"+units[i] == null?"null":units[i] +" "+values[i]);
            }

        }
        Log.d(TAG,"Next pressed");
        Logger.log(TAG,"Next pressed");
        //UtilityClass.showToast("Next pressed");
    }

    /**
     * private method to collect and store unit relationship values in an array
     */
    private void populateValues()
    {
        LinearLayout container = ((LinearLayout)parent.findViewById(R.id.container));
        int count = container.getChildCount();
        values[0] = 1;
        insertBaseUnit(getLastUnit());
        for(int loop=1; loop<=count;loop++)
        {
            String val = ((EditText) container.getChildAt(loop-1).findViewById(R.id.value)).getText().toString();
            values[loop] = Integer.parseInt(val)*values[loop-1];
        }
    }

    /**
     * method to inflate a new unit to be added
     * @param inflater layout inflater
     * @param group view group
     * @return the inflated new unit layout
     */
    private View newUnit(LayoutInflater inflater, ViewGroup group)
    {
        final LinearLayout layout =  (LinearLayout)inflater.inflate(R.layout.unit_relationship_item,group,false);
        layout.findViewById(R.id.item_layout).setLayoutParams(UtilityClass.getParamsForHorizontalScrollView(group.findViewById(R.id.container).getWidth()));
        layout.findViewById(R.id.delete).setTag(pos);//id telling what position in the container layout this view resides
        final HorizontalScrollView sv = (HorizontalScrollView)layout.findViewById(R.id.scrollView);
        layout.findViewById(R.id.value).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                v.requestFocusFromTouch();
                return true;
            }
        });

        layout.findViewById(R.id.value).setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {

            }
        });
        /*((TextView)layout.findViewById(R.id.value)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                    UtilityClass.showToast("hello there");
                    ((HorizontalScrollView)layout.findViewById(R.id.scrollView)).scrollTo(-layout.getWidth(),0);
                }
            }
        });*/
        //define what happens when the delete button is pressed
        layout.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onDelete((Integer)v.getTag());
            }
        });
        return layout;
    }

    private void addViewToLayout(View view)
    {
        ((LinearLayout)(parent.findViewById(R.id.container))).addView(view);
        views.add(view);
        pos++;
    }

    /**
     * private method to retrieve the last unit entered
     * @return last unit as string
     */
    private String getLastUnit()
    {
        return ((EditText)views.get(pos-1).findViewById(R.id.unit2)).getText().toString();
    }
    /**
     * method called when the add button is pressed.
     */
    private void onAddClicked()
    {
        //if 4 items have been added, tell user it has reached max
        if(pos == 4)
        {
            UtilityClass.showToast("Maximum number of unit relationship reached");
            return;
        }
        String text = getLastUnit();
        if(text.length() == 0)
        {
            UtilityClass.showToast("Please enter a unit");
            text = null;
            return;
        }
        View view = newUnit(inflater,parent);
        addViewToLayout(view);
        ((EditText)view.findViewById(R.id.unit1)).setText(text);
        ((EditText)view.findViewById(R.id.unit1)).setEnabled(false);

        if(count ==0)
            insertBaseUnit(text);
        else
            units[count++] = text;
        //values[count] = 1;
    }

    /**
     * this method is called to make sure the base unit is inserted into the array
     */
    private void insertBaseUnit(String text)
    {
        if(count == 0)
        {
            LinearLayout container = ((LinearLayout)parent.findViewById(R.id.container));
            String string = ((EditText)container.getChildAt(count).findViewById(R.id.unit1)).getText().toString().trim();
            units[count++] = string;
            units[count++] = text;
        }
    }

    /**
     * method called when the delete button is pressed
     * @param p the position in the layout to delete from.
     */
    private void onDelete(int p)
    {
        if(p == 0)
        {
            UtilityClass.showToast("Cannot delete base unit");
            return;
        }
        //remove the view from the layout
        ((ViewGroup)parent.findViewById(R.id.container)).removeViewAt(p);
        //remove from ArrayList
        views.remove(p);

        //if deleted unit is the last, then only set unit[pos] = null
        if(p == pos-1)
            units[p+1] = null;
        else
        {
            //move the next unit to take the place of the deleted one
            units[p+1] = units[p+2];
            //setup next unit

            View view = ((ViewGroup) parent.findViewById(R.id.container)).getChildAt(p).findViewById(R.id.unit1);
            ((TextView) view.findViewById(R.id.unit1)).setText(units[p]);
            view.setTag(p);
        }
        //decrement count and pos
        pos--;
        count--;
    }

    public void setQuantityPriceRelationshipWeakReference(WeakReference<QuantityPriceRelationship> reference)
    {
        quantityPriceRelationshipWeakReference = reference;
    }

    /**
     * called to retrieve the various units entered by a user
     * @return the units as entered by the user.
     */
    public String [] getUnits()
    {
        return units;
    }

    /**
     * called to return the base unit relationship values
     * @return unit relationship values
     */
    public int [] getValues()
    {
        return values;
    }

    @Override
    public void finalize()
    {
        group = null;
        units = null;
        values = null;
        inflater = null;
        views = null;
        dialog = null;
        qrp = null;
        //reference = null;
        quantityPriceRelationshipWeakReference = null;
        parent = null;
    }
    //not used for now
    public ViewGroup getUnitRelationShipLayout()
    {
        return parent;
    }
}
