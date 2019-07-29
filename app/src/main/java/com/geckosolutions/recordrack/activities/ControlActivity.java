package com.geckosolutions.recordrack.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.DrawerListAdapter;
import com.geckosolutions.recordrack.fragments.DashboardFragment;
import com.geckosolutions.recordrack.fragments.DebtorsFragment;
import com.geckosolutions.recordrack.fragments.ExpenseFragment;
import com.geckosolutions.recordrack.fragments.IncomeFragment;
import com.geckosolutions.recordrack.fragments.PricelistFragment;
import com.geckosolutions.recordrack.fragments.NewPurchaseFragment;
import com.geckosolutions.recordrack.fragments.PurchaseViewPager;
import com.geckosolutions.recordrack.fragments.SalesViewPager;
import com.geckosolutions.recordrack.fragments.SettingsFragment;
import com.geckosolutions.recordrack.fragments.StockFragment;
import com.geckosolutions.recordrack.fragments.WelcomeFragment;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.Settings;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.lang.ref.WeakReference;
import java.util.List;

public class ControlActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private ActionBar actionBar;
    private Dialog dialog;
    private FragmentManager fragmentManager;
    private WeakReference<ControlActivity> reference;
    private int STATE = -1;
    private final String TAG = "ControlActivity";
    private String activity[] = new String[]{"dashboard","sales","purchases","stock","pricelist","debtors","income","expenses","notes","settings"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayOptions(getSupportActionBar().DISPLAY_SHOW_HOME | getSupportActionBar().DISPLAY_SHOW_TITLE);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.wet_asphalt));
        reference = new WeakReference<ControlActivity>(this);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList = (ListView)findViewById(R.id.left_drawer);
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);

        //code to make the drawerList have a width 2/3 of the screen width
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        UtilityClass.setContext(getApplicationContext());
        int width = (int)((float)dm.widthPixels*0.5);
        wm = null;
        dm = null;
        drawerList.getLayoutParams().width = width;
        DrawerListAdapter adapter = new DrawerListAdapter();
        adapter.setName(getIntent().getStringExtra("name"));
        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(this);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        fragmentManager = getSupportFragmentManager();
        closePreviousActivities();
        displayGridView();

    }

    private void displayGridView()
    {
        if(fragmentManager.findFragmentByTag("Welcome Fragment") == null)
        {
            hideFragments();
            WelcomeFragment fragment = new WelcomeFragment();
            fragment.setReferenceToControlActivity(reference);
            fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Welcome Fragment").commit();
            fragmentManager.executePendingTransactions();
            fragmentManager.beginTransaction().show(fragment);

            fragment = null;
        }
        else
        {
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Welcome Fragment"));
            //hideFragments();
        }
    }

    /**
     * method called to dismiss previous activities
     */
    private void closePreviousActivities()
    {
        UtilityClass.dismissAccountSetupActivity();
        UtilityClass.dismissMainActivity();
        UtilityClass.dissmissStoreDetails();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            /*if(STATE == 5)
                startActivity(new Intent(getApplicationContext(),NewStockActivity.class));
                //UtilityClass.showDialog(R.layout.stock_options_layout,new WeakReference<AppCompatActivity>(this));*/
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle bundle)
    {
        super.onPostCreate(bundle);
        drawerToggle.syncState();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        final int pos = position-1;
        drawerLayout.closeDrawer(Gravity.LEFT,true);
        preOnItemClicked(pos);
    }

    /**
     * this method is called first to determine if the information dialog should be displayed or not
     * @param pos the position of the item that was clicked.
     */
    public void preOnItemClicked(final int pos)
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean result = Settings.shouldShowDescriptionDialog(activity[pos]);
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        onItemClicked(pos,result);
                    }
                });

            }
        });
    }

    /**
     * this method is called to display the different fragments when an item is selected
     * @param position the integer position that was selected
     */
    private void onItemClicked(int position, boolean shouldShowInfoDialog)
    {
        if(position < 0)
            return;

        if(shouldShowInfoDialog)
        {
            dialog = UtilityClass.showCustomDialog(R.layout.information_layout,new WeakReference<Activity>(reference.get()));
            Logger.log(TAG,"is dialog null "+Boolean.toString(dialog == null));
            Log.d(TAG, "is dialog null " + Boolean.toString(dialog == null));
            dialog.findViewById(R.id.ok).setTag(position);
            dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final int pos = (Integer)(v.getTag());
                    dialog.dismiss();

                    //store flags to not show this dialog again
                    DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Settings.setShouldShowDescriptionDialog(activity[pos]);
                        }
                    });
                }
            });
        }

        if(position == 0)
        {
            if(shouldShowInfoDialog)
            {
                String text = "Dashboard shows you a summary of your revenues. You can select a date to view how money came in or left the business";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }

            if(fragmentManager.findFragmentByTag("Dashboard Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new DashboardFragment();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Dashboard Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);

                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Dashboard Fragment"));
                //hideFragments();
            }
        }
        else if(position == 1)
        {
            if(shouldShowInfoDialog)
            {
                String text = "In Sales, you can perform new sales, look up old sales or view how each item sold for a given day contributed towards overall revenue";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }

            if(fragmentManager.findFragmentByTag("Sales Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new SalesViewPager();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Sales Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
                //this causes the options dialog to show up.User selects what they want to do
                /*final Dialog dialog = UtilityClass.showCustomDialog(R.layout.sales_layout_options,
                                new WeakReference<AppCompatActivity>((AppCompatActivity) this));
                //this handles what happens when new sales is clicked.

                dialog.findViewById(R.id.new_sales).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Fragment fragment = new NewSalesFragment();
                        fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Sales Fragment").commit();
                        fragmentManager.executePendingTransactions();
                        fragmentManager.beginTransaction().show(fragment);
                        fragment = null;
                        dialog.dismiss();
                    }
                });
                //this handles what happens when edit sales is clicked.

                dialog.findViewById(R.id.edit_sales).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Fragment fragment = new SalesSearchFragment();
                        fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Sales Search Fragment").commit();
                        fragmentManager.executePendingTransactions();
                        fragmentManager.beginTransaction().show(fragment);
                        fragment = null;
                        dialog.dismiss();
                    }
                });*/

            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Sales Fragment"));
                //hideFragments();
            }
        }
        else if(position == 2)
        {
            if(shouldShowInfoDialog)
            {
                String text = "When you receive a resupply of items already entered via Stock, Purchase is the go to place";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }

            if(fragmentManager.findFragmentByTag("Purchase Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new PurchaseViewPager();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Purchase Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Purchase Fragment"));
                //hideFragments();
            }
        }
        else if(position == 3)
        {
            if(shouldShowInfoDialog)
            {
                String text = "Stock allows you add new items to be sold. You can also delete an item if it's no longer being sold ";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }

            if(fragmentManager.findFragmentByTag("Stock Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new StockFragment();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Stock Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Stock Fragment"));
                //hideFragments();
            }
        }
        else if(position == 4)
        {
            if(shouldShowInfoDialog)
            {
                String text = "Price list allows you view and change prices of items you sell";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }

            if(fragmentManager.findFragmentByTag("Pricelist Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new PricelistFragment();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Pricelist Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Pricelist Fragment"));
                //hideFragments();
            }
        }
        else if(position == 5)
        {
            if (shouldShowInfoDialog)
            {
                String text = "People who owe you and their payment history can be viewed from here";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }

            if(fragmentManager.findFragmentByTag("Debtors Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new DebtorsFragment();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Debtors Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Debtors Fragment"));
                //hideFragments();
            }
        }
        else if(position == 6)
        {
            if(shouldShowInfoDialog)
            {
                String text = "Monies that you'd like to include as part of today's revenue but didn't come through sales can be added from here";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }

            if(fragmentManager.findFragmentByTag("Income Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new IncomeFragment();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Income Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Income Fragment"));
                //hideFragments();
            }
        }
        else if(position == 7)
        {
            if(shouldShowInfoDialog)
            {
                String text = "We understand you need to pay for some services and would like it deducted from today's revenue, Expenses is just for that";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }
            if(fragmentManager.findFragmentByTag("Expense Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new ExpenseFragment();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Expense Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Expense Fragment"));
                //hideFragments();
            }
        }
        else if(position == 9)
        {
            if(shouldShowInfoDialog)
            {
                String text = "Want to change the way the app behaves? Settings lets you do just that";
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
            }
            if(fragmentManager.findFragmentByTag("Settings Fragment") == null)
            {
                hideFragments();
                Fragment fragment = new SettingsFragment();
                fragmentManager.beginTransaction().add(R.id.content_frame,fragment,"Settings Fragment").commit();
                fragmentManager.executePendingTransactions();
                fragmentManager.beginTransaction().show(fragment);
                fragment = null;
            }
            else
            {
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Settings Fragment"));
                //hideFragments();
            }
        }


    }

    private void displayStockOptions()
    {

    }

    /**
     * method to hide all fragments currently added to the MusicStoreFragment
     */
    private void hideFragments()
    {
        List<Fragment> frag =  fragmentManager.getFragments();
        if(frag != null)
        {
            for(Fragment fragment :frag)
            {
                /*if(fragmentManager.findFragmentByTag("Transfer") == fragment)
                    fragmentManager.beginTransaction().hide(fragment).commit();
                else*/
                if(fragment != null)
                {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                    fragmentManager.executePendingTransactions();
                }
            }
        }
        frag = null;
    }
}
