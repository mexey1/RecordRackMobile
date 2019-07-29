package com.geckosolutions.recordrack.fragments;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.logic.DatabaseThread;

import java.io.IOException;

/**
 * Created by anthony1 on 5/20/17.
 */

public class MainActivityFragments extends Fragment
{
    private ViewGroup parent;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {

        int position = getArguments().getInt("position");
        if(position == 0)
            parent = (RelativeLayout)inflater.inflate(R.layout.activity_main_child0,group,false);
        else if(position == 1)
        {
            parent = (LinearLayout) inflater.inflate(R.layout.activity_main_child1,group,false);

        }
        else if(position == 2)
        {
            parent = (LinearLayout) inflater.inflate(R.layout.activity_main_child2,group,false);
            //initFragmentTwo();
        }
        else if(position == 3)
        {
            parent = (LinearLayout) inflater.inflate(R.layout.activity_main_child3,group,false);
            //initFragmentThree();
        }


        return parent;
    }


}
