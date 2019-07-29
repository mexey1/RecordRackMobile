package com.geckosolutions.recordrack.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.DataUploadClass;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.NetworkThread;
import com.geckosolutions.recordrack.logic.Tuples;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;


public class StoreDetails extends AppCompatActivity {

    private ActionBar actionBar;
    private EditText editTexts[] = new EditText[5];
    private String values [] = new String[5];
    private final int TABLE_NUMBER =1;
    private static JSONObject jsonObject;
    private String [] columns = {"name","address","type","establishment_year","phone_number"};
    private String [] editTextValues = new String[5];
    private static WeakReference<AppCompatActivity> weakReference;
    private static WeakReference<StoreDetails> reference;
    private String email = null;
    private ProgressDialog progressDialog;
    private static boolean canEnter = true;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(UtilityClass.hasUserCompletedStoreDetails())
        {
            startActivity(new Intent(this, AccountSetup.class));
            finish();
            return;
        }
        setContentView(R.layout.store_details);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        editTexts[0] = (EditText)findViewById(R.id.business_name);
        editTexts[1] = (EditText)findViewById(R.id.business_address);
        editTexts[2] = (EditText)findViewById(R.id.business_type);
        editTexts[3] = (EditText)findViewById(R.id.est_year);
        editTexts[4] = (EditText)findViewById(R.id.phone_number);
        email = getIntent().getStringExtra("email");
        weakReference = new WeakReference<AppCompatActivity>(this);
        reference = new WeakReference<StoreDetails>(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_store_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        NavUtils.navigateUpTo(this, new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onContinueClick(View view)
    {
       try
       {
           canEnter = false;
           int count = 0;
           boolean shouldStoreData = false;
           jsonObject = new JSONObject();
           jsonObject.put("tableName", "business_details");
           while(count < 5)
           {
               editTextValues[count] = editTexts[count].getText().toString().trim();
               jsonObject.put(columns[count],editTextValues[count]);
               if(editTextValues[count].length() == 0)
               {
                   UtilityClass.showToast("You have an empty field");
                   shouldStoreData = false;
                   break;
               }
               else
                   shouldStoreData = true;
               count++;
           }
           if(shouldStoreData)
           {
               displayProgressDialog("Contacting Record Rack servers...");
               NetworkThread.getNetworkThread().postNetworkTask(registerStoreWithServer);
           }

       }
       catch (JSONException e)
       {
           e.printStackTrace();
       }


        /*synchronized (UtilityClass.getContext())
        {
            try
            {
                UtilityClass.getContext().wait(5000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                return;
            }
        }
        if(DatabaseThread.getContinueFlag())
        {
            UtilityClass.setStoreDetailsComplete();
            DatabaseThread.setContinueFlag(false);
            startActivity(new Intent(this, AccountSetup.class));
            finish();
        }
        else
            UtilityClass.showToast("Data was not inserted into database");*/

    }

    private void displayProgressDialog(String msg)
    {
        progressDialog = UtilityClass.getProgressDialog(new WeakReference<Activity>(reference.get()));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private static Runnable registerStoreWithServer = new Runnable()
    {
        @Override
        public void run()
        {
            Tuples<String, String> serverResponse = DataUploadClass.registerStoreWithServer(reference.get().email);
            if(serverResponse.getFirst().equals("200"))
            {
                UtilityClass.saveRackID(serverResponse.getSecond());
                Log.d("Debug RR","This is the rack_id "+serverResponse.getSecond());
                //UtilityClass.showToast("This is the rack_id "+serverResponse.getSecond());
                //dismiss dialog on UI thread
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        reference.get().progressDialog.dismiss();
                    }
                });
                //once we have registered the store with the server, we can go ahead and store the details
                reference.get().saveStoreDetails();
            }
        }
    };

    private void saveStoreDetails()
    {
        try
        {
            jsonObject.put("created",UtilityClass.getDateTime());
            jsonObject.put("last_edited",UtilityClass.getDateTime());
            DatabaseThread.getDatabaseThread().postDatabaseTask(dbRunnable);
            UtilityClass.setStoreDetailsWeakReference(new WeakReference<StoreDetails>(this));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        //UtilityClass.prepareDatabaseEntry("business_details", DatabaseManager.INSERT,values);
    }

    private static Runnable dbRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                long id = DatabaseManager.insertData(jsonObject);
                if(id > 0)
                {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run()
                        {
                            if(weakReference.get() != null)
                            {
                                UtilityClass.setStoreDetailsComplete();
                                weakReference.get().startActivity(new Intent(weakReference.get(), AccountSetup.class));
                                //weakReference.get().finish();
                            }
                        }
                    });
                }
                else
                {
                    Log.d("Debug RR","Data was not inserted into database");
                    //UtilityClass.showToast("Data was not inserted into database");
                }
                //canEnter = true;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        actionBar = null;
        int count =0;
        while(count < 5)
        {
            values[count] = null;
            count++;
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}
