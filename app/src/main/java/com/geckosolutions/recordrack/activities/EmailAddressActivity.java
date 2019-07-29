package com.geckosolutions.recordrack.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.DataUploadClass;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.LoggerThread;
import com.geckosolutions.recordrack.logic.NetworkThread;
import com.geckosolutions.recordrack.logic.Tuples;
import com.geckosolutions.recordrack.logic.UtilityClass;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 8/13/17.
 */

public class EmailAddressActivity extends AppCompatActivity
{
    private static WeakReference<EmailAddressActivity> reference;
    private ProgressDialog progressDialog;
    private Dialog dialog;
    private StringBuilder builder;
    private String email;
    private static final String TAG = "EmailAddressActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_address_layout);
        Logger.log(TAG,"Email address activity is showing");
        reference = new WeakReference<EmailAddressActivity>(this);
        init();
    }

    private void init()
    {
        //when the continue button is pressed, we'd like to go to the server and request a code for the email address
        Logger.log(TAG,"Continue button pressed Email");
        findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                email = ((EditText)reference.get().findViewById(R.id.email_address)).getText().toString();
                Logger.log(TAG,"Email address is "+email);
                if(!email.contains("@"))
                {
                    Logger.log(TAG,"Email address is invalid");
                    Log.d(TAG,"Email address is invalid");
                    UtilityClass.showToast("Email address is invalid");
                }
                else
                {
                    displayProgressDialog("Contacting Record Rack servers");
                    NetworkThread.getNetworkThread().postNetworkTask(submitEmailAddress);
                }

                //Intent intent = new Intent(reference.get(),StoresListActivity.class);
                //UtilityClass.getProgressDialog(new WeakReference<Activity>(getParent()));
                //startActivity(intent);
                Logger.log(TAG,"This is reference: "+reference.get());
                System.out.println(reference.get());
            }
        });

        //when the export dialog button is pressed
        Logger.log(TAG,"Export log pressed");
        findViewById(R.id.export_log).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LoggerThread.getLoggerThread().postLoggerTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            byte b[] = new byte[4*1024];
                            int i = 0;
                            String logFile = UtilityClass.getLogPath()+"/Log.txt";
                            String exceptionFile = UtilityClass.getLogPath()+"/Exception.txt";
                            Logger.finish();
                            //read from logFile
                            FileInputStream fis = new FileInputStream(logFile);
                            FileInputStream fis1 = new FileInputStream(exceptionFile);

                            StringBuffer logs = new StringBuffer();
                            StringBuffer exceptions = new StringBuffer();
                            while ((i =fis.read(b,0,b.length)) !=-1)
                                logs.append(new String(b,0,i));
                            while ((i =fis1.read(b,0,b.length)) !=-1)
                                exceptions.append(new String(b,0,i));

                            //once done reading from the files, we'd love to send the mail
                            fis.close();
                            fis1.close();
                            sendMail(logs.toString(),exceptions.toString());
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void sendMail(final String log, final String exception)
    {
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run()
            {
                Log.d("Logs",log+" "+exception);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL,new String[]{"mgbachi_anthony@yahoo.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT,"Record Rack Mobile Logs");
                intent.putExtra(Intent.EXTRA_TEXT,log+"\n"+exception);


                startActivity(Intent.createChooser(intent,"Choose an app to send email"));
            }
        });

    }

    private void displayProgressDialog(String msg)
    {
        progressDialog = UtilityClass.getProgressDialog(new WeakReference<Activity>(reference.get()));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }
    private static Runnable submitEmailAddress = new Runnable()
    {
        @Override
        public void run()
        {
            Logger.log(TAG,"About contacting server for verification code");
            //the first tuple is the response code, the second is the response message
            final Tuples<String,String> serverResponse = DataUploadClass.emailAddressValidationCodeRequest(reference.get().email);
            //run on UI
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    reference.get().progressDialog.dismiss();

                    if(serverResponse == null)
                    {
                        UtilityClass.showToast("Couldn't connect to server. Please check your connection and try again.");
                        Logger.log(TAG,"Couldn't connect to server.");
                        return;
                    }
                    else if(serverResponse.getFirst().equals("200"))
                    {
                        Logger.log(TAG,serverResponse.getSecond());
                        Log.d("Debug RR",serverResponse.getSecond());
                        //UtilityClass.showToast(serverResponse.getSecond());

                        reference.get().dialog = UtilityClass.showCustomDialog(R.layout.enter_verification_code,
                                new WeakReference<Activity>(reference.get()));
                        reference.get().dialog.setCancelable(false);
                        //((EditText)reference.get().dialog.findViewById(R.id.one)).requestFocus();

                        //make every character entered to be upper case
                        reference.get().customizeEditText();

                        //define what happens when the submit button is pressed
                        reference.get().dialog.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                reference.get().submitVerificationCode();
                            }
                        });

                        reference.get().dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                reference.get().dialog.dismiss();
                            }
                        });
                    }

                }
            });

        }
    };

    private void customizeEditText()
    {
        EditText code = ((EditText)dialog.findViewById(R.id.verification_code));
        InputFilter [] editTextFilters = code.getFilters();
        InputFilter [] newFilter = new InputFilter[editTextFilters.length+1];
        System.arraycopy(editTextFilters,0,newFilter,0,editTextFilters.length);
        newFilter[editTextFilters.length] = new InputFilter.AllCaps();
        code.setFilters(newFilter);
    }

    /**
     * this method is called when the user attempts to submit a verification code
     */
    private void submitVerificationCode()
    {
        builder = new StringBuilder();
        EditText code = ((EditText)dialog.findViewById(R.id.verification_code));
        builder.append(code.getText().toString().trim());
       /* String one = ((EditText)reference.get().dialog.findViewById(R.id.one)).getText().toString();
        String two = ((EditText)reference.get().dialog.findViewById(R.id.two)).getText().toString();
        String three = ((EditText)reference.get().dialog.findViewById(R.id.three)).getText().toString();
        String four = ((EditText)reference.get().dialog.findViewById(R.id.four)).getText().toString();
        String five = ((EditText)reference.get().dialog.findViewById(R.id.five)).getText().toString();
        String six = ((EditText)reference.get().dialog.findViewById(R.id.six)).getText().toString();
        String seven = ((EditText)reference.get().dialog.findViewById(R.id.seven)).getText().toString();

        //if no string is empty
        if(one.length()>0 && two.length()>0 && three.length() >0 && four.length()>0 && five.length()>0 && six.length()>0 && seven.length()>0)
        {
            builder.append(one);
            builder.append(two);
            builder.append(three);
            builder.append(four);
            builder.append(five);
            builder.append(six);
            builder.append(seven);

            //submit the code off ui thread
            NetworkThread.getNetworkThread().postDatabaseTask(submitVerificationCodeRunnable);

        }*/
       if(code.length() == 7)
       {
           Logger.log(TAG,"Verifying code...Please wait...");
           reference.get().displayProgressDialog("Verifying code...Please wait...");
           NetworkThread.getNetworkThread().postNetworkTask(submitVerificationCodeRunnable);
       }
        else //if any is empty
            UtilityClass.showToast("Verification code should be 7 characters long...");
    }

    private static Runnable submitVerificationCodeRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            Logger.log(TAG,"in the verification code");
            Tuples<String,String> serverResponse = DataUploadClass.submitValidationCode(reference.get().email,reference.get().builder.toString());
            //now that we have submitted the code to the server, let's see what the server has to say
            final String resp_cd = serverResponse.getFirst();
            final String resp_msg = serverResponse.getSecond();
            Logger.log(TAG,"Verification message: "+resp_msg);
            //200 means code was correct and we should have a list of stores associated with this business.
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        //dismiss the verifying code progress dialog
                        reference.get().progressDialog.dismiss();

                        if(resp_cd.equals("200"))
                        {
                            reference.get().dialog.dismiss();
                            JSONArray array = new JSONArray(resp_msg);
                            Logger.log(TAG,"stores: "+array);
                            Log.d(TAG,"stores: "+array);
                            //no store currently associated with this business, display the StoreDetails activity
                            if(array.length() == 0)
                            {
                                Intent intent = new Intent(reference.get(), StoreDetails.class);
                                intent.putExtra("email",reference.get().email);
                                reference.get().startActivity(intent);
                            }
                            else //display store list activity and populate it with the list of stores
                            {
                                Intent intent = new Intent(reference.get(), StoresListActivity.class);
                                intent.putExtra("store_list",array.toString());
                                intent.putExtra("email",reference.get().email);
                                reference.get().startActivity(intent);
                            }
                        }
                        else
                        {
                            UtilityClass.showToast(resp_msg);
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}
