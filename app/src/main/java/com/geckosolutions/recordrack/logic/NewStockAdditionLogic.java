package com.geckosolutions.recordrack.logic;

import android.app.Activity;
import android.app.Dialog;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.NewStockViewPagerAdapter;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.fragments.StockFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 5/27/17.
 */

public class NewStockAdditionLogic
{
    private DialogFragment fragment;
    private ViewPager viewPager;
    private NewStockViewPagerAdapter adapter;
    private LayoutInflater inflater;
    private boolean hasSubUnits;
    private UnitRelationship unitRelationship;
    private QuantityPriceRelationship quantityPriceRelationship;
    private String categoryName,itemName, unit, quantity, price;
    private int pageCount;
    private Tuples[] quantityPrice;
    private String [] units;
    private int [] values;//this represent the base unit equivalent
    private LinearLayout viewPagerPosition;
    /**
     * this method is called to initialize the DialogFragment
     * @param fragment
     */
    public void init(DialogFragment fragment)
    {
        if(this.fragment !=null)
            return;
        this.fragment = fragment;
        inflater = LayoutInflater.from(UtilityClass.getContext());
        Log.d("init called",Boolean.toString(fragment.getDialog() == null));
        viewPager = (ViewPager) fragment.getDialog().findViewById(R.id.view_pager);
        viewPagerPosition = (LinearLayout)fragment.getDialog().findViewById(R.id.positions);
        adapter = new NewStockViewPagerAdapter();
        addViewToAdapter(R.layout.new_product_name_layout);
        viewPager.setAdapter(adapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position)
            {
                viewPagerPosition.findViewById(position).setBackgroundResource(R.drawable.grey_circle);
                if(position>0)
                    viewPagerPosition.findViewById(position-1).setBackgroundResource(R.drawable.white_circle);
                if(position == 0 || (pageCount-position) ==2)
                    viewPagerPosition.findViewById(position+1).setBackgroundResource(R.drawable.white_circle);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }

    private void addViewToAdapter(int layout)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(layout,viewPager,false);
        int pos = adapter.addView(viewGroup);
        addPositionIndicator(pos);
        //adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(pos,true);
        if(pos == 0 )
            initPageOne(viewGroup);
        else if(pos == 1)
            initPageTwo(viewGroup);
        else if(pos==2 && hasSubUnits)
            initUnitRelationship(viewGroup);
        else if(pos ==2 && !hasSubUnits)
            initNoSubunitsLayout(viewGroup);
        else if(pos==3)
            initQuantityPriceRelationship(viewGroup);

        pageCount++;

    }



    /**
     * this method is called to create and add the position indicator for the viewpager
     * @param pos the position this indicator represents
     */
    private void addPositionIndicator(int pos)
    {
        View view = inflater.inflate(R.layout.view,viewPagerPosition,false);
        view.setBackgroundResource(R.drawable.grey_circle);
        view.setId(pos);
        viewPagerPosition.addView(view);
    }


    /**
     * this initializes the contents of the first page in the viewpager. This is the page
     * containing the product name and item name fields.
     */
    private void initPageOne(final ViewGroup viewGroup)
    {
        viewGroup.findViewById(R.id.next).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditText cat = (EditText) viewGroup.findViewById(R.id.category) ;
                EditText cat1 = (EditText) viewGroup.findViewById(R.id.item_name);
                if(cat.getText().length() ==0 || cat1.getText().length()==0)
                    UtilityClass.showToast("You have one or more empty fields...");
                else
                {
                    categoryName = cat.getText().toString().trim();
                    itemName = cat1.getText().toString().trim();
                    if(pageCount >= 2)//if the user already loaded up a second page, just scroll
                        viewPager.setCurrentItem(1,true);
                    else
                        addViewToAdapter(R.layout.sub_unit_enquiry_layout);
                }
            }
        });

        viewGroup.findViewById(R.id.help).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayHelpDialog(R.layout.new_product_name_help);
            }
        });
    }

    public void displayHelpDialog(int layout)
    {
        final Dialog dialog = UtilityClass.showCustomDialog(layout,
                new WeakReference<Activity>(fragment.getActivity()));
        dialog.setCancelable(false);
        dialog.findViewById(R.id.got_it).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
    }

    private void removePositionIndicator(int pos)
    {
        viewPagerPosition.removeViewAt(pos);
    }

    /**
     * this defines what happens when the buttons in the sub_unit_enquiry layout would do.
     * @param viewGroup
     */
    private void initPageTwo(final ViewGroup viewGroup)
    {
        viewGroup.findViewById(R.id.no).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(hasSubUnits)//if user chose has sub-units before, remove all pages after current page
                {
                    int temp=pageCount;
                    for (int i=2;i<temp;i++)
                    {
                        adapter.removeView(viewPager,i);
                        removePositionIndicator(i);
                        pageCount--;
                    }
                }
                hasSubUnits = false;
                addViewToAdapter(R.layout.no_sub_unit_layout);
            }
        });

        viewGroup.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!hasSubUnits)//if user chose has sub-units before, remove all pages after current page
                {
                    int temp=pageCount;
                    for (int i=2;i<temp;i++)
                    {
                        adapter.removeView(viewPager,i);
                        removePositionIndicator(i);
                        pageCount--;
                    }
                }
                hasSubUnits = true;
                //unitRelationship = new UnitRelationship(viewGroup);
                if(pageCount>=3)
                    viewPager.setCurrentItem(2,true);
                else
                    addViewToAdapter(R.layout.unit_relationship_layout);
            }
        });

        viewGroup.findViewById(R.id.what_are_sub_units).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                displayHelpDialog(R.layout.sub_unit_help);
            }
        });
    }

    private void initNoSubunitsLayout(final ViewGroup viewGroup)
    {
        ((CustomEditText) viewGroup.findViewById(R.id.price)).enableTextChangedListener();
        viewGroup.findViewById(R.id.done).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditText cat = (EditText) viewGroup.findViewById(R.id.unit) ;
                EditText cat1 = (EditText) viewGroup.findViewById(R.id.quantity);
                CustomEditText cat2 = (CustomEditText) viewGroup.findViewById(R.id.price);

                if(cat.getText().length()==0 || cat1.getText().length()==0 || cat2.getText().length()==0)
                    UtilityClass.showToast("You have one or more empty fields...");
                else
                {
                    unit = cat.getText().toString().trim();
                    quantity = cat1.getText().toString().trim();
                    price = cat2.getText().toString().trim();

                    //define insertion into the database
                    onDoneButtonPressed();
                }
            }
        });
    }

    private void initUnitRelationship(ViewGroup viewGroup)
    {
        unitRelationship = new UnitRelationship(viewGroup,this);
    }

    private void initQuantityPriceRelationship(ViewGroup viewGroup)
    {
        quantityPriceRelationship = new QuantityPriceRelationship(viewGroup,this);
        quantityPriceRelationship.setUnits(units);
    }

    public void onUnitRelationshipCompleted(String units[])
    {
        this.units = units;
        if(pageCount >=4)
            viewPager.setCurrentItem(3,true);
        else
            addViewToAdapter(R.layout.quantity_price_relationship);
    }

    public void onDoneButtonPressed()
    {
        if(hasSubUnits)
        {
            quantityPrice = quantityPriceRelationship.getQuantityAndPrice();
            values = unitRelationship.getValues();
        }
        DatabaseThread.getDatabaseThread().postDatabaseTask(runnable);
    }

    /**
     * this runnable object defines the insertions into the database
     */
    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                long item_id = -1, category_id = -1,unit_id = -1, quantity_id = -1;
                //insert category into the database

                JSONObject category = new JSONObject();
                category.put("tableName","category");
                category.put("category",categoryName);
                category.put("archived","0");
                category.put("created",UtilityClass.getDateTime());
                category.put("last_edited",UtilityClass.getDateTime());
                category.put("user_id", Integer.toString(UtilityClass.getCurrentUserID()));
                //System.out.println("this is the id "+ DatabaseManager.insertData(category));
                category_id = DatabaseManager.insertData(category);

                if(category_id > 0)
                {
                    //insert item into the database
                    category = null;
                    category = new JSONObject();
                    category.put("tableName","item");
                    category.put("category_id", Long.toString(category_id));
                    category.put("item",itemName);
                    category.put("created",UtilityClass.getDateTime());
                    category.put("last_edited",UtilityClass.getDateTime());
                    category.put("user_id", Integer.toString(UtilityClass.getCurrentUserID()));
                    item_id = DatabaseManager.insertData(category);
                    if(item_id == -1) {
                        UtilityClass.showToast("An item with the same name already exists for this category.");
                        return;
                    }
                }

                //if there are subunits, we want to do this
                if(item_id > 0 && hasSubUnits)
                {
                    unit_id = insertIntoUnitTable(item_id,units[0],(Double)quantityPrice[0].getSecond(),values[0]);
                    for(int i = 1; i< quantityPrice.length; i++)
                    {
                        //insert unit into the database
                        insertIntoUnitTable(item_id,units[i],(Double)quantityPrice[i].getSecond(),values[i]);
                    }
                }
                else if(item_id > 0)
                    unit_id = insertIntoUnitTable(item_id,unit,UtilityClass.removeNairaSignFromString(price),1);

                if(unit_id > 0 && hasSubUnits)
                {
                    double total = 0;
                    for (int i=0; i<quantityPrice.length; i++)
                        total += ((Double)quantityPrice[i].getFirst()* values[i]);
                    quantity_id = insertIntoCurrentQuantityTable(item_id,total);
                    quantity = Double.toString(total);
                }
                else if(unit_id >0)
                    quantity_id = insertIntoCurrentQuantityTable(item_id,Double.parseDouble(quantity));

                if(quantity_id == -1)
                    UtilityClass.showToast("This unit already exists. Edit instead of inserting");
                else
                {
                    DatabaseManager.populateInitialQuantity(item_id,unit_id,quantity);
                    UtilityClass.showToast("This item was correctly saved into the database");
                    StockFragment.getReference().get().fetchStockData();
                    fragment.getDialog().dismiss();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private long insertIntoUnitTable(long item_id, String unit, double price, int baseUnitEqui) throws JSONException
    {
        //insert unit into the database
        JSONObject category = null;

        category = new JSONObject();
        category.put("tableName","unit");
        category.put("item_id",Long.toString(item_id));
        category.put("unit", unit);
        //these have to be changed in the future
        category.put("base_unit_equivalent", baseUnitEqui);
        category.put("cost_price","0");
        category.put("is_default","0");
        category.put("currency","NGN");
        category.put("archived","0");
        category.put("short_form", unit.substring(0,3));

        category.put("retail_price", price);
        category.put("created",UtilityClass.getDateTime());
        category.put("last_edited",UtilityClass.getDateTime());
        category.put("user_id", Integer.toString(UtilityClass.getCurrentUserID()));
        return DatabaseManager.insertData(category);
    }

    private long insertIntoCurrentQuantityTable(long item_id, double quantity) throws JSONException
    {
        JSONObject category = new JSONObject();
        category.put("tableName","current_quantity");
        category.put("item_id",Long.toString(item_id));
        //category.put("unit_id", Long.toString(unit_id));
        category.put("quantity", quantity);
        category.put("created",UtilityClass.getDateTime());
        category.put("last_edited",UtilityClass.getDateTime());
        category.put("user_id", Integer.toString(UtilityClass.getCurrentUserID()));
        return DatabaseManager.insertData(category);
    }

    public DialogFragment getFragment()
    {
        return fragment;
    }
}
