package com.geckosolutions.recordrack.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.interfaces.NewStockFragmentInterface;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.util.ArrayList;

/**
 * Created by anthony1 on 2/5/16.
 */
public class NewStockDescriptionFragment extends Fragment
{
    private int POSITION = 0;
    private View primaryView;
    private LinearLayout roundButtonsLayout,unitsAndPricesLayout,unitAndPriceItem,quantityLayout, priceRelationship;
    private TextView bars, addButton;
    private int BAR_STATE = 0;  //the bar that displays or hides the + and option icons
    private LayoutInflater layoutInflater;
    private static int VIEWPOSITION = 0, UNITS = 0; //integer variable describing how many units were entered by the user
    private static volatile NewStockFragmentInterface unitsListener, summaryListener;
    private static ArrayList<String> unitsList,priceList,quantityList;
    private static String pageOneVariables[];//this holds the Category and Item name entered by the user
    private int UNITS_STATE;//this variable determines if page2 is being recreated(0) or a new item is being added(1)


    /*public NewStockDescriptionFragment(int position)
    {
        POSITION = position;
    }*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        if(savedInstance !=null)
            POSITION = savedInstance.getInt("position");
        layoutInflater = inflater;
        //primaryView = (LinearLayout)inflater.inflate(R.layout.new_sales_layout, group, false);
        if(POSITION == 0)
        {
            primaryView = inflater.inflate(R.layout.new_stock_layout_page1, group, false);
            initPage1();
        }
        else if(POSITION == 1)
        {
            primaryView = inflater.inflate(R.layout.new_stock_layout_page2, group, false);
            unitsAndPricesLayout = (LinearLayout)primaryView.findViewById(R.id.units_and_prices_lay);
            initPage2();
        }
        else if(POSITION == 2)
        {
            primaryView = inflater.inflate(R.layout.new_stock_layout_page3, group, false);
            initPage3();
        }

        else if (POSITION == 3)
        {
            primaryView = inflater.inflate(R.layout.new_stock_layout_page4, group, false);
            initPage4();
        }
        return primaryView;
    }

    private void initPage1()
    {

    }
    private void initPage2()
    {
        if(unitsList == null)
            unitsList = new ArrayList<>();
        if(priceList == null)
            priceList = new ArrayList<>();
        if(quantityList == null)
            quantityList = new ArrayList<>();
        addUnitAndPriceItem();
        //roundButtonsLayout = (LinearLayout)primaryView.findViewById(R.id.round_buttons_layout);
        addButton = (TextView)primaryView.findViewById(R.id.add_button);
        //bars = (TextView)primaryView.findViewById(R.id.bars);
        bars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BAR_STATE == 0) {
                    bars.setText(getResources().getText(R.string.x2));
                    bars.setTextColor(getResources().getColor(R.color.black));
                    bars.setPressed(true);
                    bars.setBackgroundDrawable(getResources().getDrawable(R.drawable.white_circle));
                    roundButtonsLayout.setVisibility(View.VISIBLE);
                    BAR_STATE = 1;
                } else {
                    roundButtonsLayout.setVisibility(View.GONE);
                    BAR_STATE = 0;
                    bars.setPressed(false);
                    bars.setText(getResources().getText(R.string.bars));
                    bars.setBackgroundDrawable(getResources().getDrawable(R.drawable.orange_round_button));
                    bars.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UNITS_STATE = 1;
                addUnitAndPriceItem();
            }
        });
    }

    private void initPage3()
    {

    }

    private void initPage4()
    {

    }

    private TextView addSummaryTexts(int position)
    {
        String text = quantityList.get(position)+" "+unitsList.get(position)+" at"+" \u20a6"+priceList.get(position);
        TextView textView = new TextView(UtilityClass.getContext());
        int height = UtilityClass.convertToPixels(50);
        int padding = UtilityClass.convertToPixels(10);
        int width  = LinearLayout.LayoutParams.MATCH_PARENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.black));
        textView.setPadding(padding, padding, padding, padding);
        return textView;
    }

    private void addUnitAndPriceItem()
    {
        WindowManager wm = (WindowManager)UtilityClass.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = getActivity().getWindow().getDecorView().getWidth();//findViewById(R.id.view_pager).getWidth();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
        //-(int)(30*dm.density)

        if(UNITS_STATE == 0)//recreation state
        {
            UNITS = 0;//we'd like to reset UNITS to 0 and re-add all elements in unitsList to the UI
            do{
                    unitAndPriceItem = null;
                    unitAndPriceItem = (LinearLayout)layoutInflater.inflate(R.layout.new_stock_layout_page2_1,unitsAndPricesLayout,false);
                    unitAndPriceItem.findViewById(R.id.item_layout).setLayoutParams(params);
                    //populateUnitsAndPrices();
                    unitsAndPricesLayout.addView(unitAndPriceItem);
                    UNITS++;
                }while (UNITS < unitsList.size());

        }
        else //this part gets run because UNITS_STATE is 1
        {
            unitAndPriceItem = null;
            unitAndPriceItem = (LinearLayout)layoutInflater.inflate(R.layout.new_stock_layout_page2_1,unitsAndPricesLayout,false);
            unitAndPriceItem.findViewById(R.id.item_layout).setLayoutParams(params);
            //populateUnitsAndPrices();
            unitsAndPricesLayout.addView(unitAndPriceItem);
            UNITS++;
        }
        unitAndPriceItem.setLayoutParams(params);
    }

    public static void resetVariables()
    {
        UNITS = 0;
        unitsList = null;
        priceList = null;
        quantityList = null;
    }
}
