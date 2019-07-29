package com.geckosolutions.recordrack.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.MainActivityViewPagerAdapter;
import com.geckosolutions.recordrack.custom.CharacterInputFilter;
import com.geckosolutions.recordrack.custom.CustomEditText;
import com.geckosolutions.recordrack.logic.UtilityClass;
import com.geckosolutions.recordrack.logic.ThreadManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

public class MainActivity extends AppCompatActivity
{
    private LinearLayout offSwitch,onSwitch;
    private CustomEditText rackID;
    private TextView whatIsThis;
    private ProgressDialog dialog;
    private static String RACK_ID;
    private static WeakReference<MainActivity> reference;
    private static WeakReference<AppCompatActivity> ref;
    private Handler handler;
    private JSONObject jsonObject;
    private ViewPager viewPager;
    private MainActivityViewPagerAdapter adapter;
    private RelativeLayout mainLayout;
    private int previosPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        UtilityClass.setContext(getApplicationContext());
        if(!UtilityClass.getRackID().equals("null"))
        {
            startActivity(new Intent(this, StoreDetails.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        //logoTranslateAnimation();
        reference = new WeakReference<MainActivity>(this);
        ref = new WeakReference<AppCompatActivity>(this);
        handler = new Handler(Looper.getMainLooper());
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        whatIsThis = (TextView)findViewById(R.id.what_is_this);
        whatIsThis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String text = getResources().getString(R.string.rack_id_description);
                //UtilityClass.showMessageDialog(ref,"Rack ID",text);
                //showWhatThisIs();
            }
        });
        adapter = new MainActivityViewPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) mainLayout.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        rackID = (CustomEditText)mainLayout.findViewById(R.id.rack_id);
        initViewPager();
        //offSwitch = (LinearLayout)getLayoutInflater().inflate(R.layout.custom_off_switch, mainLayout, false);
        //onSwitch = (LinearLayout)getLayoutInflater().inflate(R.layout.custom_on_switch, mainLayout, false);
        //mainLayout.addView(offSwitch);
        //mainLayout.addView(onSwitch);
        //onSwitch.setVisibility(View.GONE);
        mainLayout.findViewById(R.id.get_started).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getStarted();
            }
        });
        rackID.setFilters(new InputFilter[]{new CharacterInputFilter(), new InputFilter.LengthFilter(10)});
    }

    /*private void loadProgress()
    {

        ThreadManager.getExecutorService().execute(new Runnable(){
            @Override
            public void run()
            {
                int count = 10;
                ProgressBar progressBar = (ProgressBar)findViewById(R.id.horizontal_progress);
                progressBar.setMax(100);
                while(count < 100)
                {
                    try
                    {
                        count+=10;
                        progressBar.setProgress(0);
                        progressBar.setProgress(count);
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }*/


    private void initViewPager()
    {
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                previosPosition = position-1;
                if(position == 0)
                {
                    //mainLayout.findViewById(R.id.pos1).setBackgroundResource(0);
                    //mainLayout.findViewById(R.id.pos2).setBackgroundResource(0);
                    mainLayout.findViewById(R.id.pos1).setBackgroundResource(R.drawable.grey_circle);
                    mainLayout.findViewById(R.id.pos2).setBackgroundResource(R.drawable.white_circle);

                }
                else if(position == 1)
                {
                    mainLayout.findViewById(R.id.pos2).setBackgroundResource(R.drawable.grey_circle);
                    mainLayout.findViewById(R.id.pos3).setBackgroundResource(R.drawable.white_circle);
                    mainLayout.findViewById(R.id.pos1).setBackgroundResource(R.drawable.white_circle);
                }
                else if(position == 2)
                {
                    mainLayout.findViewById(R.id.pos3).setBackgroundResource(R.drawable.grey_circle);
                    mainLayout.findViewById(R.id.pos4).setBackgroundResource(R.drawable.white_circle);
                    mainLayout.findViewById(R.id.pos2).setBackgroundResource(R.drawable.white_circle);
                }
                else if(position == 3)
                {
                    mainLayout.findViewById(R.id.pos4).setBackgroundResource(R.drawable.grey_circle);
                    mainLayout.findViewById(R.id.pos3).setBackgroundResource(R.drawable.white_circle);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void logoTranslateAnimation()
    {
        WindowManager windowManager = (WindowManager)(getSystemService(WINDOW_SERVICE));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        TranslateAnimation translateAnimation = new TranslateAnimation(0,0,0,(-1)*(displayMetrics.heightPixels/4));
        translateAnimation.setDuration(2000);
        translateAnimation.setFillAfter(true);
        translateAnimation.setInterpolator(new AnticipateOvershootInterpolator());
        ImageView imageView = (ImageView)findViewById(R.id.logo);
        //imageView.setAnimation(translateAnimation);
        imageView.startAnimation(translateAnimation);
        translateAnimation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                LinearLayout mainLayout = (LinearLayout)findViewById(R.id.main_layout);
                mainLayout.setVisibility(LinearLayout.VISIBLE);
                mainLayout = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }
        });
        imageView = null;
        windowManager = null;
        displayMetrics = null;
        translateAnimation = null;
    }

    /**
     * this private message shows a dialog when the user wants to see what piece of information is
     * being requested, in this case its the Rack ID.
     */

    private void getStarted()
    {
        UtilityClass.setMainActivityWeakReference(new WeakReference<MainActivity>(this));
        startActivity(new Intent(this, EmailAddressActivity.class));
    }

    public void register()
    {
        //dialog = ProgressDialog.show(this,"Record Rack","Confirming Rack ID",true,false);
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        RACK_ID = "ABCDEFGHIJ";
        UtilityClass.saveRackID(RACK_ID);
        //ThreadManager.getExecutorService().execute(registerRunnable);
        UtilityClass.setMainActivityWeakReference(new WeakReference<MainActivity>(this));
        startActivity(new Intent(this, StoreDetails.class));
    }

    /**
     * this method is called when the user attempts to register a Rack_ID
     * @param view a reference to the register button is passed here by android
     */
    public void register(View view)
    {
        if(rackID.getText().toString().length()==0 || rackID.getText().toString().length() < 10)
        {
            UtilityClass.showToast("Rack ID is either empty or incomplete. Please enter 10 digit ID");
            return;
        }
        //dialog = ProgressDialog.show(this,"Record Rack","Confirming Rack ID",true,false);
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        RACK_ID =rackID.getText().toString();
        UtilityClass.saveRackID(RACK_ID);
        //ThreadManager.getExecutorService().execute(registerRunnable);
        UtilityClass.setMainActivityWeakReference(new WeakReference<MainActivity>(this));
        startActivity(new Intent(this, StoreDetails.class));
    }

    /**
     * this is a runnable that handles registration of a Rack_ID. It is done off the UI thread.
     */
    private static Runnable registerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                HttpPost post = new HttpPost(UtilityClass.getServer()+"/Register_Server");
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("rack_id", RACK_ID));

                DefaultHttpClient httpClient = new DefaultHttpClient();
                post.setEntity(new UrlEncodedFormEntity(nameValuePair));
                HttpResponse response = httpClient.execute(post);

                BufferedInputStream bufferedInputStream = new BufferedInputStream(response.getEntity().getContent());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte b[] = new byte[8*1024];
                int read;
                while((read = bufferedInputStream.read(b)) != -1)
                {
                    baos.write(b,0,read);
                }
                reference.get().jsonObject = new JSONObject(new String(baos.toByteArray()));
                reference.get().dialog.dismiss();
                reference.get().handler.post(responseRunnable);
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };

    /**
     * this runnable is used to display server response to user. It is posted by a handler to the
     * UI thread.
     *
     */
    private static Runnable responseRunnable = new Runnable() {
        @Override
        public void run()
        {
            try
            {
                if(reference.get().jsonObject.getInt("status_code") != 0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(reference.get());
                    builder.setCancelable(true);
                    builder.setTitle("Rack ID");
                    builder.setMessage(reference.get().jsonObject.getString("status_message"));
                    builder.show();
                }
                else
                {
                    reference.get().startActivity(new Intent(reference.get(), StoreDetails.class));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void switchOff(View view)
    {
        view.setVisibility(View.GONE);
        offSwitch.setVisibility(View.VISIBLE);
    }

    public void switchOn(View view)
    {
        view.setVisibility(View.GONE);
        onSwitch.setVisibility(View.VISIBLE);
    }
}
