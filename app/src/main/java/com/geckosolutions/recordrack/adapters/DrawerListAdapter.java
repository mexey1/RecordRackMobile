package com.geckosolutions.recordrack.adapters;

import android.graphics.Bitmap;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomTextView;
import com.geckosolutions.recordrack.custom.FontAwesomeTextView;
import com.geckosolutions.recordrack.logic.UtilityClass;
import com.geckosolutions.recordrack.logic.PrepareBitmap;
import com.geckosolutions.recordrack.logic.ThreadManager;

import android.os.Handler;

/**
 * Created by anthony1 on 9/1/15.
 */
public class DrawerListAdapter extends BaseAdapter
{
    private String[] icons , text = {"Dashboard", "Sales", "Purchases", "Stock","Price List", "Debtors","Income","Expenses", "Notes","Settings"};;
    private LayoutInflater layoutInflater;
    private int colors[];
    private static ImageView imageView;
    private String name;

    public DrawerListAdapter()
    {
       icons = UtilityClass.getContext().getResources().getStringArray(R.array.drawer_item_icons);
       layoutInflater = LayoutInflater.from(UtilityClass.getContext());
       colors = new int[]{R.color.pomegranate,R.color.orange1,R.color.blue1,R.color.emerald,R.color.midnight_blue,R.color.red,R.color.applegreen,R.color.battleship_grey,R.color.wisteria,R.color.peterriver};
    }

    public void setName(String name)
    {
        this.name = name;
    }
    @Override
    public int getCount() {
        return text.length+1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = null;
        if(convertView == null)
        {
            if (position == 0)
            {
                LinearLayout layout = (LinearLayout)layoutInflater.inflate(R.layout.profile_layout,parent,false);
                imageView = (ImageView)layout.findViewById(R.id.pix);
                ((TextView)layout.findViewById(R.id.name)).setText(name);
                ThreadManager.getExecutorService().execute(circularImageRunnable);
                view = layout;
            }
            else if(convertView == null)
            {
                LinearLayout linearLayout = (LinearLayout)layoutInflater.inflate(R.layout.drawer_items,parent,false);
                TextView textView = (TextView)linearLayout.findViewById(R.id.text);
                FontAwesomeTextView fontAwesomeTextView = (FontAwesomeTextView)linearLayout.findViewById(R.id.icon);
                textView.setText(text[position - 1]);
                fontAwesomeTextView.setTextColor(UtilityClass.getContext().getResources().getColor(colors[position - 1]));//colors[position]
                fontAwesomeTextView.setText(icons[position - 1]);
                //linearLayout.setBackgroundDrawable(ContextTracker.getContext().getResources().getDrawable(R.drawable.drawer_listview_item));
                view = linearLayout;
            }
        }
        else
        {
            if (position == 0)
            {
                LinearLayout layout = (LinearLayout)layoutInflater.inflate(R.layout.profile_layout,parent,false);
                imageView = (ImageView)layout.findViewById(R.id.pix);
                ((TextView)layout.findViewById(R.id.name)).setText(name);
                ThreadManager.getExecutorService().execute(circularImageRunnable);
                view = layout;
            }
            else
            {
                LinearLayout linearLayout = (LinearLayout)layoutInflater.inflate(R.layout.drawer_items,parent,false);
                TextView textView = (TextView)linearLayout.findViewById(R.id.text);
                FontAwesomeTextView fontAwesomeTextView = (FontAwesomeTextView)linearLayout.findViewById(R.id.icon);
                textView.setText(text[position-1]);
                fontAwesomeTextView.setTextColor(UtilityClass.getContext().getResources().getColor(colors[position-1]));
                fontAwesomeTextView.setText(icons[position-1]);
                view = linearLayout;
            }
        }
        return view;
    }

    private static Runnable circularImageRunnable = new Runnable()
    {
        @Override
        public void run()
        {

            final Bitmap image = PrepareBitmap.drawCircularImage(UtilityClass.convertToPixels(70), UtilityClass.convertToPixels(70));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    imageView.setImageBitmap(image);
                }
            });
        }
    };
}
