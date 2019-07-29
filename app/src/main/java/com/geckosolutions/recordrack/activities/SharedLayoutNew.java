package com.geckosolutions.recordrack.activities;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.SharedLayoutGridAdapter;
import com.geckosolutions.recordrack.adapters.SpinnerAdapter;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.custom.CustomSpinner;
import com.geckosolutions.recordrack.custom.CustomTextView;
import com.geckosolutions.recordrack.interfaces.CustomEdittextInterface;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.SharedLayoutLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by anthony1 on 8/31/17.
 */

public class SharedLayoutNew implements CustomEdittextInterface
{
    private LayoutInflater inflater;
    private ViewGroup mainLayout,category,itemParent;
    private String text,editItemName;
    private SharedLayoutGridAdapter adapter;
    private SpinnerAdapter spinnerAdapter;
    private String categoryName;
    private int count,pos,pos1=-1;
    private Hashtable<String,Integer> map1;
    //private ArrayList<SharedLayoutGridAdapter> list1;
    //private ArrayList<ViewGroup> list2;
    private ArrayList<ViewGroup> list1;
    private ArrayList<SharedLayoutGridAdapter> list2;
    private SharedLayoutLogic sharedLayoutLogic;
    private CustomEditText unitPrice,quantity;
    private CustomTextView total;
    private CustomSpinner spinner;
    private View previous;

    private final String TAG = "SharedLayoutNew";
    private AdapterView.OnItemSelectedListener listener;
    private int MODE; //0 for new, 1 for edit
    private long editItemID,editedUnitID;


    public SharedLayoutNew(ViewGroup parent,int mode)
    {
        inflater = LayoutInflater.from(UtilityClass.getContext());
        mainLayout = (ViewGroup) inflater.inflate(R.layout.shared_layout_new,parent,false);
        sharedLayoutLogic = new SharedLayoutLogic();
        spinnerAdapter = new SpinnerAdapter();
        MODE = mode;
        init();
    }

    private void init()
    {
        quantity = (CustomEditText) mainLayout.findViewById(R.id.quantity);
        total = (CustomTextView) mainLayout.findViewById(R.id.total);
        quantity.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                try
                {
                    computeTotal();
                }
                catch (NumberFormatException e)
                {
                    UtilityClass.showToast("Quantity entered isn't a valid number");
                    e.printStackTrace();
                }
            }
        });
        unitPrice = (CustomEditText) mainLayout.findViewById(R.id.unit_price);
        unitPrice.enableTextChangedListener();
        unitPrice.setCustomEditTextChangeListener(this);
        //define the behavior of the spinner
        spinner = (CustomSpinner)mainLayout.findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        //((Spinner)mainLayout.findViewById(R.id.spinner))android.R.layout.simple_spinner_dropdown_item
        listener = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                //unitPos = position;
                Logger.log(TAG,"position " + position + "unit p " + sharedLayoutLogic.getUnitPrice(spinner.getSelectedPosition()));
                Log.d(TAG,"position " + position + "unit p " + sharedLayoutLogic.getUnitPrice(spinner.getSelectedPosition()));
                setUnitPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        };
        spinner.setOnItemSelectedListener(listener);
        spinner.setListener(listener);

        //define the behavior of the search box
        ((EditText)mainLayout.findViewById(R.id.search)).addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                text = s.toString();
                if(text.length()>0)
                {
                    DatabaseThread.getDatabaseThread().postDatabaseTask(query);
                    //UtilityClass.showToast(text);
                    Logger.log(TAG,"Text typed: "+text);
                    Log.d("Text typed",text);
                }
                else
                    clearParent(false);
            }
        });
    }

    //runnable to retrieve suggestions from db
    private Runnable query = new Runnable()
    {
        @Override
        public void run()
        {
            final JSONArray result = sharedLayoutLogic.getSuggestions(text);
            Log.d(TAG,"result "+result.toString());
            Logger.log(TAG,"result "+result.toString());
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        clearParent(result.length()>0);
                        JSONObject object1 = null;
                        for(int i=0;i<result.length();i++)
                        {
                            object1 = result.getJSONObject(i);
                            categoryName = object1.getString("category");
                            addItemToAdapter(object1.getString("item"),object1.getLong("id"));
                        }
                        if(result.length() >0)
                            addCategoryToParent();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /**
     * method called to add an item to the grid view
     * @param item item name being added
     * @param itemID ID of the item to be added
     */
    private void addItemToAdapter(final String item,final long itemID)
    {
        Log.d(TAG,"Item to be added "+item);
        Logger.log(TAG,"Item to be added "+item);
        if(map1==null && list1 == null && list2 ==null)
        {
            map1 = new Hashtable<>();//map containing all the category names and their corresponding position
            //list1 = new ArrayList<>();//holds grid adapter for each category created
           // list2 = new ArrayList<>();//holds a reference to each category returned from the search
            list1 = new ArrayList<>();//holds reference to each category to which items are added
            list2 = new ArrayList<>();//holds reference to all sharedlayout adapters
            Log.d(TAG,"New category being created "+category);
            Logger.log(TAG,"New category being created "+category);
        }
        if(map1.containsKey(categoryName))
        {
            itemParent = (LinearLayout)list1.get(map1.get(categoryName)).findViewById(R.id.grid_item_parent);
            View view = addCategoryItemToParentLayout(itemParent,item);
            Log.d(TAG, "category exists, adding new item :"+item+" itemID: "+itemID+" view:"+view);
            Logger.log(TAG, "category exists, adding new item :"+item+" itemID: "+itemID+" view:"+view);
            adapter = list2.get(map1.get(categoryName));
            adapter.addItem(item,itemID,view);
            view.setTag(map1.get(categoryName));
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    setClickListener(v,(Integer) v.getTag());
                }
            });
            //setClickListener(view,map1.get(categoryName));
        }
        else
        {
            Log.d(TAG,"Item being added to existing category"+categoryName+": item is :"+item);
            Logger.log(TAG,"Item being added to existing category"+categoryName+": item is :"+item);
            //create the layout containing grid view
            category = (ViewGroup) inflater.inflate(R.layout.shared_layout_parent,mainLayout,false);
            itemParent = (LinearLayout) category.findViewById(R.id.grid_item_parent);
            ((TextView)category.findViewById(R.id.category)).setText(categoryName);
            //setClickListener();
            //create adapter
            adapter = new SharedLayoutGridAdapter();

            //adapter.addItem(item,itemID);
            //((GridView)category.findViewById(R.id.grid_view)).setAdapter(adapter);
            map1.put(categoryName,pos);

            //list1.add(pos,category);
            //list2.add(pos++,adapter);
            View view = addCategoryItemToParentLayout(itemParent,item);
            adapter.addItem(item,itemID,view);
            view.setTag(pos);
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    setClickListener(v,(Integer) v.getTag());
                }
            });

            list1.add(pos,category);
            list2.add(pos++,adapter);
            //setClickListener(view,map1.get(categoryName));
        }

        //we select the item being edited here
        if(MODE == 1 && editItemID == itemID && item.equalsIgnoreCase(editItemName))//edit mode
            selectItemBeingEdited();


        //((GridView)category.findViewById(R.id.grid_view)).setSelection(adapter.getCount()-1);
    }

    private void selectItemBeingEdited()
    {
        pos1 = adapter.getCount()-1;
        final SharedLayoutGridAdapter adapter = list2.get(pos-1);
        Log.d(TAG,"Item selected: "+pos1);
        Logger.log(TAG,"Item selected: "+pos1);
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                sharedLayoutLogic.retrievePriceAndQuantityDetails(editItemID);
                final String units[] = sharedLayoutLogic.getUnits();

                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setItemSelectedBackground((View) adapter.getItem(pos1));
                        
                        for (int i=0; i<units.length;i++)
                        {
                            spinnerAdapter.addItem(units[i]);
                            if(editedUnitID == sharedLayoutLogic.getUnitID(i))
                                spinner.setSelection(i);
                        }
                        //setItemSelectedBackground((View) adapter.getItem(pos1));
                    }
                });
            }
        });
    }

    private void computeTotal()
    {
        String q = quantity.getText().toString();
        String p = unitPrice.getText().toString();
        double q1 = Double.parseDouble(q.length()==0?"0":q);
        double p1 = UtilityClass.removeNairaSignFromString(p.length()==0?"0":p);
        double t = q1*p1;
        total.setText(UtilityClass.formatMoney(t));
    }

    /**
     * this method defines what happens when a grid view item is pressed.
     */
    private void setClickListener(View view, int position)
    {
        setItemSelectedBackground(view);
        final long itemID = list2.get(position).getItemID(view);
        //run database queries off UI thread
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                //when an item is clicked, we'd need to retrieve important details about the
                //item
                sharedLayoutLogic.retrievePriceAndQuantityDetails(itemID);
                //unitPos = 0;
                //get all units for the selected item.this is on the UI thread
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String [] units = sharedLayoutLogic.getUnits();
                        ((Spinner)mainLayout.findViewById(R.id.spinner)).setSelection(0);
                        //clear what was added to adapter previously
                        spinnerAdapter.clearItems();
                        for (String u:units)
                            spinnerAdapter.addItem(u);
                    }
                });

            }
        });
        /*((GridView)category.findViewById(R.id.grid_view)).setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                setItemSelectedBackground(view);

                final long itemID = ((SharedLayoutGridAdapter)parent.getAdapter()).getItemID(position);
                //run database queries off UI thread
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //when an item is clicked, we'd need to retrieve important details about the
                        //item
                        sharedLayoutLogic.retrievePriceAndQuantityDetails(itemID);
                        //unitPos = 0;
                        //get all units for the selected item.this is on the UI thread
                        new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String [] units = sharedLayoutLogic.getUnits();
                                ((Spinner)mainLayout.findViewById(R.id.spinner)).setSelection(0);
                                //clear what was added to adapter previously
                                spinnerAdapter.clearItems();
                                for (String u:units)
                                    spinnerAdapter.addItem(u);
                            }
                        });

                    }
                });
            }
        });*/
    }

    /**
     * this method is called to set the unit price based on the unit selected
     */
    private void setUnitPrice()
    {
        unitPrice.setText(UtilityClass.formatMoney(sharedLayoutLogic.getUnitPrice(spinner.getSelectedPosition())));
        computeTotal();
    }

    /**
     * this method is called to toggle background of the selected item
     * @param view the item being selected.
     */
    private void setItemSelectedBackground(View view)
    {
        if(previous != null)
        {
            previous.findViewById(R.id.item).setBackgroundColor(UtilityClass.getContext().getResources().getColor(R.color.white));
            ((TextView)previous.findViewById(R.id.item)).setTextColor(UtilityClass.getContext().getResources().getColor(R.color.wet_asphalt));
        }
        view.findViewById(R.id.item).setBackgroundColor(UtilityClass.getContext().getResources().getColor(R.color.peterriver));
        ((TextView)view.findViewById(R.id.item)).setTextColor(UtilityClass.getContext().getResources().getColor(R.color.white));

        Logger.log(TAG,"Item selected background");
        Log.d(TAG,"Item selected background");
        previous = view;
    }


    private void addCategoryToParent()
    {
        for (int i=0;i<list1.size();i++)
        {
            category = list1.get(i);
            ((ViewGroup)mainLayout.findViewById(R.id.parent)).addView(category);
        }
        /*for (int i=0;i<list2.size();i++)
        {
            category = list1.get(i);
            count = list1.get(i).getCount();
            GridView gridView = (GridView) category.findViewById(R.id.grid_view);
            int gridWidth = mainLayout.getWidth();
            int childWidth = UtilityClass.convertToPixels(120);


            int rowCount = gridWidth/childWidth;
            int n = (count%rowCount)==0?(count/rowCount):(count/rowCount)+1;

            int height = (UtilityClass.convertToPixels(130*n));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height);
            gridView.setLayoutParams(params);
            ((ViewGroup)mainLayout.findViewById(R.id.parent)).addView(category);
        }

        Enumeration enumeration = map1.keys();
        //Collection vals=  map1.values();
        while (enumeration.hasMoreElements())
        {
            String key = (String) enumeration.nextElement();
            System.out.println("key "+key+" value "+map1.get(key));
        }*/
    }


    /**
     * method called to create and add a category item to it's parent layout
     * @param parent the parent view to which the newly created item should be added
     * @param text name of item being added
     * @return the newly added item
     */
    private View addCategoryItemToParentLayout(ViewGroup parent, String text)
    {
        View convertView = inflater.inflate(R.layout.shared_layout_item,parent,false);
        ((TextView)convertView.findViewById(R.id.item)).setText(text);
        parent.addView(convertView);
        return convertView;
    }

    /**
     * method to obtain a reference to the main layout.
     * @return
     */
    public ViewGroup getMainLayout()
    {
        return mainLayout;
    }

    /**
     * this method is called when there is no result to be shown
     * @param shouldShowNoResult
     */
    private void clearParent(boolean shouldShowNoResult)
    {
        if(list1!=null && list1.size()>0)
        {
            for(View category : list1)
                ((ViewGroup)mainLayout.findViewById(R.id.parent)).removeView(category);
        }

        if (shouldShowNoResult)
            mainLayout.findViewById(R.id.parent).findViewById(R.id.no_content).setVisibility(View.GONE);
        else
            mainLayout.findViewById(R.id.parent).findViewById(R.id.no_content).setVisibility(View.VISIBLE);
        categoryName = null;
        pos=0;
        map1 = null;
        list1 = null;
        list2 = null;
        /*if(list2!=null && list2.size()>0)
        {
            for (int i=0;i<list2.size();i++)
                ((ViewGroup)mainLayout.findViewById(R.id.parent)).removeView(list2.get(i));
        }

        if (shouldShowNoResult)
           mainLayout.findViewById(R.id.parent).findViewById(R.id.no_content).setVisibility(View.GONE);
        else
            mainLayout.findViewById(R.id.parent).findViewById(R.id.no_content).setVisibility(View.VISIBLE);
        categoryName = null;
        pos=0;
        map1 = null;
        list1 = null;
        list2 = null;*/
    }

    @Override
    public void onTextChanged(String g)
    {
        computeTotal();
    }

    /**
     * this method checks to see if any field is empty
     * @return true if a field is empty, false otherwise
     */
    public boolean isAnyFieldEmpty()
    {
        boolean value = false;
        TextView unit = (TextView)mainLayout.findViewById(R.id.unit);
        if(previous == null) {
            UtilityClass.showToast("Please select an item to continue...");
            value = true;
        }
        else if(quantity.getText().toString().length()==0 || unitPrice.getText().length() == 0)
            value = true;
        return value;
    }

    public void setJSONDataForItemDisplayed(JSONObject object)
    {
        //this.object = object;
    }

    public void setCurrentQuantity(double d)
    {

    }

    public String getCategory()
    {
        return sharedLayoutLogic.getCategory();
    }

    public String getItemName()
    {
        return sharedLayoutLogic.getItemName();
    }

    public long getUnitID()
    {
        return sharedLayoutLogic.getUnitID(spinner.getSelectedPosition());
    }

    public String getUnit()
    {
        return sharedLayoutLogic.getUnit(spinner.getSelectedPosition());
    }

    public long getItemID()
    {
        return sharedLayoutLogic.getItemID();
    }

    public double getUnitPrice()
    {
        return UtilityClass.removeNairaSignFromString(unitPrice.getText().toString());
    }

    public double getCost()
    {
        return UtilityClass.removeNairaSignFromString(total.getText().toString());
    }

    public double getQuantity()
    {
        return Double.parseDouble(quantity.getText().toString());
    }

    public double getCurrentQuantity()
    {
        return sharedLayoutLogic.getCurrentQuantity();
    }

    public void setItemBeingEdited(String itemBeingEdited,  long id, long unitID)
    {
        editItemName = itemBeingEdited;
        editItemID = id;
        editedUnitID = unitID;
    }

}
