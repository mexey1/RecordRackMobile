package com.geckosolutions.recordrack.activities;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.FrameLayout;
import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.CustomSpinnerAdapter;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 5/15/16.
 */
public class SharedLayout
{
    private LinearLayout popupLayout;
    private PopupWindow popupWindow;
    private JSONArray searchResult;
    private JSONObject object;
    private CustomSpinnerAdapter spinnerAdapter;
    private volatile int category = -1, item =-1, edited_view = -1;//edited view is a flag for which view is being edited.
                                                        // 0 means the category edittext, 1 means the items layout,
                                                        // 2 means unit layout
    private volatile int STATE = -1, unit = -1;//state takes the value of -1 or 0. -1 means the edittext is still
                                   //being edited. 0 means the textview displayed in the popup has been clicked.
    private volatile double quantity,unitPrice;
    private LayoutInflater inflater;
    private LinearLayout mainLayout;
    private double [] prices;//this variable holds the prices for various units for the selected item.
    private int count;
    private final String TAG = "SharedLayout";

    public SharedLayout(ViewGroup parent)
    {
        inflater = LayoutInflater.from(UtilityClass.getContext());
        mainLayout = (LinearLayout)inflater.inflate(R.layout.shared_layout_new,parent,false);
        //init();
    }

    /**
     * this method is called to initialize the layout components
     */
    private void init()
    {
        Logger.log(TAG,"Hello there00...");
        Log.d("Record Rack","Hello there");
        //monitor what the user types in the category field and attempt to search for
        //suggestions
        ((EditText) mainLayout.findViewById(R.id.category)).addTextChangedListener(new TextWatcher()
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
                edited_view = 0;
                if (s.toString().length() > 0)
                {
                    Log.d("Record Rack","Hello there");
                    Logger.log(TAG,"Hello there00...");
                    if (STATE == -1)
                        findSuggestions(s.toString());
                } else
                    closePopupWindow();

                STATE = -1;
            }
        });

        //show dropdown for items belonging to the selected category
        mainLayout.findViewById(R.id.item_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                edited_view = 1;
                if (category == -1)
                    return;
                else
                    findSuggestions(Integer.toString(category));
            }
        });

        //show dropdown for units belonging to the selected item
        mainLayout.findViewById(R.id.unit_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                edited_view = 2;
                if (item == -1)
                    return;
                else
                    findSuggestions(Integer.toString(item));
            }
        });

        //turn on text change listener for the cost edittext
        ((CustomEditText)mainLayout.findViewById(R.id.cost)).enableTextChangedListener();
    }

    /**
     * this method constructs and searches for suggestions matching what the user has typed.
     * the search is done off the UI thread and the result is moved to the UI thread to be displayed
     * @param text the text to search for
     */
    private void findSuggestions(final String text)
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject object = new JSONObject();
                    if (edited_view == 0)//category field is being edited
                    {
                        String columnArgs[] = {"id", "category"};
                        String whereArgs = " category LIKE '" + text + "%'";
                        object.put("columnArgs", columnArgs);
                        object.put("tableName", "category");
                        object.put("whereArgs", whereArgs);
                    } else if (edited_view == 1) //item layout is being edited
                    {
                        String columnArgs[] = {"id", "item"};
                        String whereArgs = " category_id = '" + text + "' AND archived=0";
                        object.put("columnArgs", columnArgs);
                        object.put("tableName", "item");
                        object.put("whereArgs", whereArgs);
                    } else if (edited_view == 2) //units layout has been clicked
                    {
                        String columnArgs[] = {"id", "retail_price", "unit"};
                        String whereArgs = " item_id = '" + text + "'";
                        object.put("columnArgs", columnArgs);
                        object.put("tableName", "unit");
                        object.put("whereArgs", whereArgs);
                    }

                    searchResult = DatabaseManager.fetchData(object);
                    //run this piece on UI thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run()
                        {
                            if (popupWindow == null || !popupWindow.isShowing())
                            {
                                View view = null;
                                if (edited_view == 0)
                                    view = mainLayout.findViewById(R.id.category);
                                else if (edited_view == 1)
                                    view = mainLayout.findViewById(R.id.item_layout);
                                else if (edited_view == 2)
                                    view = mainLayout.findViewById(R.id.unit_layout);

                                if(searchResult.length() > 0 && edited_view == 0)
                                    ((EditText)view).setTextColor(getResources().getColor(R.color.black));
                                else if(searchResult.length() == 0 && edited_view ==0)
                                {
                                    ((EditText) view).setTextColor(getResources().getColor(R.color.pomegranate));
                                    return;
                                }
                                popupWindow = UtilityClass.showPopupWindow(view,mainLayout);
                                popupLayout = (LinearLayout) popupWindow.getContentView().findViewById(R.id.popup_layout);
                            }
                            displaySuggestion();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * this method closes the popup window
     */
    public void closePopupWindow()
    {
        if(popupWindow != null && popupWindow.isShowing())
            popupWindow.dismiss();
        popupWindow = null;
        popupLayout = null;
    }


    /**
     * this method is called to display suggestions in a popup window after the result
     * of the search has been returned.
     */
    private void displaySuggestion()
    {
        if(popupLayout != null)
            popupLayout.removeAllViews();
        //this is only instantiated when the units layout is selected
        if(edited_view == 2)
            prices = new double[searchResult.length()];

        count = 0;
        while(count < searchResult.length())
        {
            try
            {
                TextView textView = (TextView)inflater.inflate(R.layout.popup_window_item,popupLayout,false);
                if(searchResult.getJSONObject(count).has("category"))
                    textView.setText(searchResult.getJSONObject(count).getString("category"));
                else if(searchResult.getJSONObject(count).has("item"))
                    textView.setText(searchResult.getJSONObject(count).getString("item"));
                else
                    textView.setText(searchResult.getJSONObject(count).getString("unit"));
                if(edited_view == 2)//if unit layout is being populated, set the retail price as the tag
                    textView.setTag(searchResult.getJSONObject(count).getDouble("retail_price"));

                textView.setId(Integer.parseInt(searchResult.getJSONObject(count).getString("id")));

                int width = mainLayout.findViewById(R.id.category).getWidth() - UtilityClass.convertToPixels(10);

                textView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //if the popup being shown is that for category, do this
                        if (edited_view == 0)
                        {
                            category = v.getId();
                            STATE = 0;
                            ((EditText) mainLayout.findViewById(R.id.category)).setText(((TextView) v).getText());
                            //reset values shown in other fields
                            ((TextView) mainLayout.findViewById(R.id.item)).setText("");
                            ((EditText)mainLayout.findViewById(R.id.quantity)).setText("");
                            ((TextView) mainLayout.findViewById(R.id.cost)).setText("");
                            ((TextView) mainLayout.findViewById(R.id.unit)).setText("");

                        }

                        //if popup being shown is for items layout
                        else if (edited_view == 1)
                        {
                            item = v.getId();
                            ((TextView) mainLayout.findViewById(R.id.item)).setText(((TextView) v).getText());
                            ((EditText)mainLayout.findViewById(R.id.quantity)).setText("");
                            ((TextView) mainLayout.findViewById(R.id.cost)).setText("");
                            ((TextView) mainLayout.findViewById(R.id.unit)).setText("");
                        }

                        //if popup being shown is for unit layout
                        else if (edited_view == 2)
                        {
                            unit = v.getId();
                            double itemQuantity = 1;
                            EditText quantityText = (EditText)mainLayout.findViewById(R.id.quantity);
                            //if quantity field has some text in it, it should be multiplied by
                            //the cost for one item
                            if(quantityText.length()> 0)
                                itemQuantity = Double.parseDouble(quantityText.getText().toString());
                            unitPrice = (Double) v.getTag();
                            String cost = UtilityClass.formatMoney(itemQuantity*unitPrice);
                            ((TextView) mainLayout.findViewById(R.id.cost)).setText(cost);

                            //run database query off UI thread
                            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (edited_view == 2)//units layout is being edited
                                        quantity = DatabaseManager.retrieveCurrentQuantity((long)item);
                                }
                            });
                            //quantity = DatabaseManager.retrieveCurrentQuantity((long)item,(long)unit);
                            ((TextView) mainLayout.findViewById(R.id.unit)).setText(((TextView) v).getText());
                            quantityText = null;
                        }
                        closePopupWindow();
                    }
                });

                View view = new View(UtilityClass.getContext());
                view.setBackgroundColor(getResources().getColor(R.color.battleship_grey_light));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,UtilityClass.convertToPixels(1));
                params.setMargins(20, 0, 20, 0);
                view.setLayoutParams(params);
                popupLayout.addView(textView);
                popupLayout.addView(view);
                count++;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * this is public method that returns the current quantity of the item chosen
     * @return current quantity
     */
    public double getCurrentQuantity()
    {
        return quantity;
    }

    /**
     * this method checks to see if any field is empty
     * @return true if a field is empty, false otherwise
     */
    public boolean isAnyFieldEmpty()
    {
        boolean value = false;
        EditText category = (EditText)mainLayout.findViewById(R.id.category);
        TextView item = (TextView)mainLayout.findViewById(R.id.item);
        EditText price = (EditText)mainLayout.findViewById(R.id.cost);
        EditText quantity = (EditText)mainLayout.findViewById(R.id.quantity);
        TextView unit = (TextView)mainLayout.findViewById(R.id.unit);
        if(category.getText().toString().trim().length() == 0 || item.getText().toString().trim().length() == 0
            || price.getText().toString().trim().length() == 0 || quantity.getText().toString().trim().length() ==0
                || unit.getText().toString().trim().length() == 0) {
            value = true;
        }
        return value;
    }

    /**
     * method that returns the Resources object
     * @return the resources object associated with the context.
     */
    private Resources getResources()
    {
        return UtilityClass.getContext().getResources();
    }

    /**
     * method to obtain a reference to the main layout.
     * @return
     */
    public LinearLayout getMainLayout()
    {
        return mainLayout;
    }

    /**
     * this method cleans up resources used
     */
    public void cleanUp()
    {
        popupWindow = null;
        popupLayout = null;
        searchResult = null;
        inflater = null;
        mainLayout = null;
    }

    public void setJSONDataForItemDisplayed(JSONObject object)
    {
        this.object = object;
    }

    public void setCurrentQuantity(double currentQuantity)
    {
        this.quantity = currentQuantity;
    }

    /**
     * this method returns the category id for the selected category
     * @return category id returned as an int
     */
    public int getCategoryID() throws JSONException
    {
        if(category == -1 & object!=null)
            category = object.getInt("category_id");
        return category;
    }

    /**
     * this method returns the item id of the selected item
     * @return the item id returned as an int
     */
    public int getItemID() throws JSONException
    {
        if(item == -1 & object!=null)
            item = object.getInt("item_id");
        return item;
    }

    /**
     * this method returns the unit id of the selected item
     * @return the unit id returned as an int
     */
    public int getUnitID() throws JSONException
    {
        if(unit == -1 & object!=null)
            unit = object.getInt("unit_id");
        return unit;
    }

    /**
     * this method returns the retail price for the given item
     * @return the retail price. i.e the price for an item
     */
    public double getUnitPrice()
    {
        return unitPrice;
    }
}
