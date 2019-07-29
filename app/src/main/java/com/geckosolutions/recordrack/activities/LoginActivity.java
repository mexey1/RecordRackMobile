package com.geckosolutions.recordrack.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.BluetoothPrint;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.NetworkThread;
import com.geckosolutions.recordrack.logic.ReceiptPrintingThread;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 1/21/16.
 */
public class LoginActivity extends AppCompatActivity
{
    private EditText username, password,uname,answer;//uname and answer are used for password retrieval
    private LinearLayout usernameLayout, passwordLayout;
    private TextView userIcon, passIcon,forgotPassword;
    private Dialog dialog;
    private final String TAG = "LoginActivity";
    private WeakReference<Activity> reference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        reference = new WeakReference<Activity>(this);
        init();
    }

    /**
     * a private method called to setup basic functionalities of this activity
     */
    private void init()
    {
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        userIcon = (TextView)findViewById(R.id.user_icon);
        passIcon = (TextView)findViewById(R.id.pass_icon);
        usernameLayout = (LinearLayout)findViewById(R.id.username_layout);
        passwordLayout = (LinearLayout)findViewById(R.id.password_layout);
        forgotPassword = (TextView)findViewById(R.id.forgot_password);

        username.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    usernameLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_inner_layout_focused));
                    userIcon.setTextColor(getResources().getColor(R.color.turquoise));
                } else {
                    usernameLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_inner_layout_unfocused));
                    userIcon.setTextColor(getResources().getColor(R.color.battleship_grey));
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus) {
                    passwordLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_inner_layout_focused));
                    passIcon.setTextColor(getResources().getColor(R.color.turquoise));

                } else {
                    passwordLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_inner_layout_unfocused));
                    passIcon.setTextColor(getResources().getColor(R.color.battleship_grey));
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog = UtilityClass.showCustomDialog(R.layout.password_retrieval_layout,reference);
                initPasswordRetrievalDialog();
            }
        });

    }

    private void initPasswordRetrievalDialog()
    {
        ((EditText)dialog.findViewById(R.id.username)).addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(s.length() >0)
                    ((TextView)dialog.findViewById(R.id.search)).setTextColor(getResources().getColor(R.color.green_sea));
                else
                    ((TextView)dialog.findViewById(R.id.search)).setTextColor(getResources().getColor(R.color.battleship_grey));
            }
        });

        uname = (EditText) dialog.findViewById(R.id.username);
        answer = (EditText) dialog.findViewById(R.id.answer);
        //retrieve secret question
        dialog.findViewById(R.id.search).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final String result[] = getFieldsForPasswordRetrieval(uname.getText().toString(),"secret_question");
                        if(result == null)
                            UtilityClass.showToast("Username specified wasn't found");
                        else
                        {
                            //update on ui thread
                            new android.os.Handler (Looper.getMainLooper()).post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    ((TextView)dialog.findViewById(R.id.security_question)).setText(result[0]);
                                }
                            });
                        }
                    }
                });

            }
        });

        dialog.findViewById(R.id.retrieve_pass).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final String result[] = getFieldsForPasswordRetrieval(uname.getText().toString(),"answer","password");
                        if(result == null)
                            UtilityClass.showToast("Username specified wasn't found");
                        else
                        {
                            if(answer.getText().toString().equalsIgnoreCase(result[0]))
                            {
                                UtilityClass.showToast("Your password is " + result[1]);
                                dialog.dismiss();
                            }
                            else
                                UtilityClass.showToast("Answer provided doesn't match what is in store for this user");
                        }
                    }
                });
            }
        });
    }

    private String[] getFieldsForPasswordRetrieval(final String username,String... columnName)
    {
        String result[] = null;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName","user");
            object.put("columnArgs",columnName);
            object.put("whereArgs","username = '"+username+"'");
            final JSONArray array = DatabaseManager.fetchData(object);
            if(array.length() == 0)
                result = null;
            else
            {
                result = new String[columnName.length];
                for (int i=0; i<columnName.length;i++)
                    result[i] = array.getJSONObject(0).getString(columnName[i]);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public void login(View view)
    {
        //setThreadPriorities();

        Log.d(TAG,UtilityClass.getRackID());

        //int p =5/0;
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                //verify login calls networking functions, so should be done off UI thread
                final boolean result = DatabaseManager.verifyLoginDetails(username.getText().toString().trim(),
                        password.getText().toString().trim());

                //move to UI thread
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(result)
                        {
                            Intent intent = new Intent(UtilityClass.getContext(),ControlActivity.class);
                            intent.putExtra("name",username.getText().toString());
                            startActivity(intent);
                            finish();
                            //call the populate initial quantity method defined in the Database class
                            callInitialQuantity();
                        }
                        else
                            UtilityClass.showToast("Invalid login details");
                    }
                });
            }
        });
    }

    private void callInitialQuantity()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                DatabaseManager.populateInitialQuantity();
            }
        });
    }
}
