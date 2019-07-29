package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomTextView;
import com.geckosolutions.recordrack.custom.FontAwesomeTextView;
import com.geckosolutions.recordrack.interfaces.ProgressInterface;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.LoggerThread;
import com.geckosolutions.recordrack.logic.NetworkThread;
import com.geckosolutions.recordrack.logic.ReceiptPrintingThread;
import com.geckosolutions.recordrack.logic.Settings;
import com.geckosolutions.recordrack.logic.ThreadManager;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 11/18/17.
 */

public class SettingsFragment extends Fragment implements ProgressInterface
{
    private ViewGroup parent;
    private Dialog dialog;
    private ProgressBar bar;
    private int max;
    private String TAG = "SettingsFragment";
    private WeakReference<SettingsFragment> reference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        parent = (ViewGroup) inflater.inflate(R.layout.settings_layout,group,false);
        init();
        return parent;
    }

    private void init()
    {
        reference = new WeakReference<SettingsFragment>(this);
        //retrieve settings from DB and update the UI
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                final boolean isPrintEnabled = Settings.isPrintingEnabled();
                final String printerName = Settings.getPrinterName();

                //update UI accordingly
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(isPrintEnabled)
                        {
                            //display on switch
                            enablePrinting();
                        }
                        else
                        {
                            //display on switch
                            disablePrinting();
                        }
                        ((TextView)parent.findViewById(R.id.printer_name)).setText(printerName);
                    }
                });
            }
        });

        //define what happens when the on switch is pressed. This should turn off printing
        parent.findViewById(R.id.on_switch).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                disablePrinting();
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                    @Override
                    public void run()
                    {
                        Settings.disablePrinting();

                    }
                });
            }
        });

        //define what happens when the on switch is pressed. This should turn on printing
        parent.findViewById(R.id.off_switch).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                enablePrinting();
                DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                    @Override
                    public void run()
                    {
                        Settings.setPrintingEnabled();

                    }
                });
            }
        });

        //define what happens when force backup is pressed
        parent.findViewById(R.id.force_bkup).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.log(TAG,"Force backup called");
                dialog = UtilityClass.showCustomDialog(R.layout.confirm_delete_layout,new WeakReference<Activity>((AppCompatActivity)getActivity()));
                ((CustomTextView)dialog.findViewById(R.id.text)).setText("This would replace data on the server with data on this device. Do you wish to proceed?");
                dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                        dialog = null;
                        dialog = UtilityClass.showCustomDialog(R.layout.progress_dialog,new WeakReference<Activity>((AppCompatActivity)getActivity()));
                        bar = (ProgressBar) dialog.findViewById(R.id.horizontal_progress);
                        dialog.setCancelable(false);
                        Logger.log(TAG,"Force backup started");
                        Settings.beginUpload(reference.get());
                        //Settings.beginUpload();
                    }
                });

                dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Logger.log(TAG,"Force backup cancelled");
                        dialog.dismiss();
                        dialog = null;
                    }
                });

            }
        });

        //listen for touch on the force upload is pressed. this just changes background color and all
        //but doesn't begin uploading to the server
        parent.findViewById(R.id.force_bkup).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction()==MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(getResources().getColor(R.color.peterriver));
                    ((FontAwesomeTextView)parent.findViewById(R.id.cloud_up)).setTextColor(getResources().getColor(R.color.white));
                    ((CustomTextView)parent.findViewById(R.id.force_bk_txt)).setTextColor(getResources().getColor(R.color.white));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bottom_border));
                    ((FontAwesomeTextView)parent.findViewById(R.id.cloud_up)).setTextColor(getResources().getColor(R.color.turquoise));
                    ((CustomTextView)parent.findViewById(R.id.force_bk_txt)).setTextColor(getResources().getColor(R.color.battleship_grey));
                }
                return false;
            }
        });

        //define what happens when force download is pressed
        parent.findViewById(R.id.force_dwnld).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.log(TAG,"Force download pressed");
                dialog = UtilityClass.showCustomDialog(R.layout.confirm_delete_layout,new WeakReference<Activity>((AppCompatActivity)getActivity()));
                ((CustomTextView)dialog.findViewById(R.id.text)).setText("This would replace data on this device with data from the server. Do you wish to proceed?");
                dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Logger.log(TAG,"user wants to proceed with download");
                        dialog.dismiss();
                        dialog = null;
                        dialog = UtilityClass.showCustomDialog(R.layout.progress_dialog,new WeakReference<Activity>((AppCompatActivity)getActivity()));
                        bar = (ProgressBar) dialog.findViewById(R.id.horizontal_progress);
                        dialog.setCancelable(false);
                        Settings.beginDownload(reference.get());
                        //Settings.beginUpload();
                    }
                });

                dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Logger.log(TAG,"User cancelled download");
                        dialog.dismiss();
                        dialog = null;
                    }
                });

            }
        });

        //listen for touch on the force download is pressed. this just changes background color and all
        //but doesn't begin downloading to the device
        parent.findViewById(R.id.force_dwnld).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction()==MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(getResources().getColor(R.color.peterriver));
                    ((FontAwesomeTextView)parent.findViewById(R.id.cloud_down)).setTextColor(getResources().getColor(R.color.white));
                    ((CustomTextView)parent.findViewById(R.id.force_dwn_txt)).setTextColor(getResources().getColor(R.color.white));
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bottom_border));
                    ((FontAwesomeTextView)parent.findViewById(R.id.cloud_down)).setTextColor(getResources().getColor(R.color.orange1));
                    ((CustomTextView)parent.findViewById(R.id.force_dwn_txt)).setTextColor(getResources().getColor(R.color.battleship_grey));
                }
                return false;
            }
        });

        parent.findViewById(R.id.export_log).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LoggerThread.getLoggerThread().postLoggerTask(new Runnable() {
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





    private void disablePrinting()
    {
        parent.findViewById(R.id.on_switch).setVisibility(View.GONE);
        parent.findViewById(R.id.off_switch).setVisibility(View.VISIBLE);
    }

    private void enablePrinting()
    {
        parent.findViewById(R.id.on_switch).setVisibility(View.VISIBLE);
        parent.findViewById(R.id.off_switch).setVisibility(View.GONE);
    }

    @Override
    public void setMax(final int max)
    {
        this.max = max;
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run()
            {
                bar.setMax(max);
            }
        });

    }

    @Override
    public void setProgress(final int progress)
    {
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                bar.setProgress(progress);
            }
        });
    }

    @Override
    public void dismissDialog()
    {
        dialog.dismiss();
    }
}
