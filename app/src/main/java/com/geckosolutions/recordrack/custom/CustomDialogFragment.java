package com.geckosolutions.recordrack.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.geckosolutions.recordrack.interfaces.CustomDialogInterface;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.lang.ref.WeakReference;

/**
 * Created by anthony1 on 4/22/17.
 * To use this class, when creating the fragment, create Bundle argument and insert the
 * xml to inflate as "xml"
 */

public class CustomDialogFragment extends DialogFragment
{
    private ViewGroup parent;
    private boolean isShowing;
    private CustomDialogInterface customDialogInterface;
    private View view;
    private Dialog dialog;
    private int height;
    private boolean onStopped;
    private final String TAG ="CustomDialogFragment";



    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        int viewToInflate = getArguments().getInt("xml");
        parent = (ViewGroup) inflater.inflate(viewToInflate,group,false);
        UtilityClass.showCustomDialog(getDialog());
        return parent;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle bundle)
    {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setContentView(layoutForDialog);
        //dialog.setContentView(view);

        return  dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG,"In the onCreateView method ");
        Logger.log(TAG, "In the onCreateView method ");
        LayoutInflater inflater1 = dialog.getLayoutInflater();
        view = inflater1.inflate(getArguments().getInt("xml"),container,false);
        Log.d(TAG,"This is the height of the view: "+Integer.toString(view.getHeight()));
        Logger.log(TAG,"This is the height of the view: "+Integer.toString(view.getHeight()));
        return view;
    }

    /*@Override
    public void show(FragmentManager manager, String name)
    {
        super.show(manager,name);

        isShowing = true;
        Log.d("is act null",String.valueOf(getActivity()==null));
        customDialogInterface.initDialog();
    }*/

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d(TAG,"In the onStart method");
        Logger.log(TAG,"In the onStart method");
        Log.d(TAG,"is act null? "+String.valueOf(getActivity()==null));
        Logger.log(TAG,"is act null? "+String.valueOf(getActivity()==null));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = UtilityClass.convertToPixels(height);
                //WindowManager.LayoutParams.WRAP_CONTENT;
        //UtilityClass.convertToPixels(600);
        //dialog.show();
        dialog.getWindow().setAttributes(lp);
        if(!onStopped)//if dialog wasn't previously being shown, then initialize dialog
            customDialogInterface.initDialog();
    }


    public void onDialogShow(CustomDialogInterface dialogInterface)
    {
        customDialogInterface = dialogInterface;
    }

    /**
     * call this method to set the height of the dialog. The height is the value in dp
     * @param height
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        parent = null;
        customDialogInterface = null;
        view = null;
        dialog = null;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        onStopped = true;
    }
}
