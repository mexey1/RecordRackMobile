package com.geckosolutions.recordrack.logic;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.activities.CustomerClass;
import com.geckosolutions.recordrack.fragments.NewSalesFragment;
import com.geckosolutions.recordrack.interfaces.CalendarInterface;
import com.geckosolutions.recordrack.interfaces.PaymentInterface;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Created by anthony1 on 9/8/16.
 */
public class CalendarClass
{
    private WeakReference<PaymentInterface> paymentInterfaceWeakReference;
    private CalendarInterface calendarInterface;
    private CalendarView calendarView;
    private String title;
    private Calendar calendar;
    private Dialog dateDialog;
    private final String TAG = "CalendarClass";

    public CalendarClass(CalendarInterface calendarInterface, String title)
    {
        this.calendarInterface = calendarInterface;
        this.title = title;
        displayCalendar();
    }

    private void displayCalendar()
    {
        dateDialog = UtilityClass.showCustomDialog(R.layout.due_date_picker_layout, calendarInterface.getActivityWeakReference());
        ((TextView)dateDialog.findViewById(R.id.text)).setText(title);
        dateDialog.setCancelable(false);
        //datePicker = (DatePicker)dateDialog.findViewById(R.id.date_picker);
        calendarView = (CalendarView)dateDialog.findViewById(R.id.calendar);
        calendar = Calendar.getInstance();
        //this is needed because getDate method doesn't work on newer versions of android
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
        {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth)
            {
                calendar.set(year,month,dayOfMonth);
                Logger.log(TAG,"this is date in display calendar method "+UtilityClass.getDate(calendar.getTimeInMillis()));
                Log.d(TAG,"this is date in display calendar method"+UtilityClass.getDate(calendar.getTimeInMillis()));
            }
        });
        //datePicker.set
        // datePicker.setMinDate(System.currentTimeMillis() - 1000);

        //this handles what happens when the cancel button in the date picker dialog is
        //pressed.
        dateDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dateDialog.dismiss();
                calendarInterface = null;
            }
        });

        //this handles what happens when the done button in the date picker dialog is
        //pressed.
        dateDialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //CalendarView calendar = (CalendarView)dateDialog.findViewById(R.id.calendar);
                //System.out.println("this is date"+UtilityClass.getDate(calendar.getTimeInMillis()));
                Log.d(TAG,"this is date"+UtilityClass.getDate(calendar.getTimeInMillis()));
                Logger.log(TAG,"this is date"+UtilityClass.getDate(calendar.getTimeInMillis()));
                //UtilityClass.showToast("this is date"+UtilityClass.getDate(calendar.getTimeInMillis()));
                calendarInterface.onDatePicked(calendar.getTimeInMillis());
                dateDialog.dismiss();
                dateDialog = null;
                calendar = null;
            }
        });
    }
}
