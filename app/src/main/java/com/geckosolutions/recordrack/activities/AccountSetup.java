package com.geckosolutions.recordrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class AccountSetup extends AppCompatActivity
{
    private EditText editTexts[] = new EditText[5];
    private String values [] = new String[5];
    private String columns[] = {"name","username","password","secret_question","answer"};
    private JSONObject jsonObject;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(UtilityClass.hasUserCompletedAccountSetup())
        {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.account_setup);
        editTexts[0] = (EditText)findViewById(R.id.name);
        editTexts[1] = (EditText)findViewById(R.id.username);
        editTexts[2] = (EditText)findViewById(R.id.password);
        editTexts[3] = (EditText)findViewById(R.id.security_question);
        editTexts[4] = (EditText)findViewById(R.id.answer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void done(View view)
    {
        //ContextTracker.setContext(getApplicationContext());
        try
        {
            int count = 0;
            jsonObject = new JSONObject();
            jsonObject.put("tableName","user");
            while(count < 5)
            {
                values[count] = editTexts[count].getText().toString().trim();
                jsonObject.put(columns[count],values[count]);
                count++;
            }
            UtilityClass.setAccountSetupWeakReference(new WeakReference<AccountSetup>(this));
            storeUserInfo();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void storeUserInfo()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(DatabaseManager.doesUsernameExist(values[1]))
                    {
                        UtilityClass.showToast("This username is already taken");
                        return;
                    }
                    long id = DatabaseManager.insertData(jsonObject);
                    if(id > 0)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                UtilityClass.setAccountSetupComplete();
                                //start new activity and pop all previously opened activities
                                Intent intent = new Intent(UtilityClass.getContext(), ControlActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                //finish();
                            }
                        });
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
