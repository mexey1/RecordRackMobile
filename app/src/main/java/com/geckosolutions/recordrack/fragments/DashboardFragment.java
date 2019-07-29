package com.geckosolutions.recordrack.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.custom.CustomTextView;
import com.geckosolutions.recordrack.interfaces.CalendarInterface;
import com.geckosolutions.recordrack.logic.CalendarClass;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.DatabaseThread;
import com.geckosolutions.recordrack.logic.DebtorPaymentHistoryLogic;
import com.geckosolutions.recordrack.logic.ExpenseFragmentLogic;
import com.geckosolutions.recordrack.logic.IncomeFragmentLogic;
import com.geckosolutions.recordrack.logic.Logger;
import com.geckosolutions.recordrack.logic.PurchaseFragmentLogic;
import com.geckosolutions.recordrack.logic.SalesInfoFragmentLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by anthony1 on 5/26/16.
 * this class handles what is shown when dash board menu is clicked.
 */
public class DashboardFragment extends Fragment implements CalendarInterface
{
    private double total[];
    private LinearLayout barChartLayout,revenueSummary;

    private ScrollView primaryView;
    private LayoutInflater inflater;
    private Calendar calendar;
    private WeakReference<DashboardFragment> reference;
    private double max = 0;
    private String daysOfTheWeek [] = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
    private String months[] = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    int []colors = new int[]{R.color.pomegranate,R.color.midnight_blue,R.color.orange1,R.color.blue1,
                        R.color.emerald,R.color.applegreen,R.color.battleship_grey,R.color.wisteria,
                        R.color.peterriver,R.color.wet_asphalt,R.color.green_sea,R.color.grey};
    private int period = 1;//1 means days of week, 2 means months of year
    private int position =0;
    private ProgressDialog dialog;
    private CustomTextView title;
    private Button periodButton;
    private final String TAG="DashboardFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstance)
    {
        this.inflater = inflater;
        calendar = Calendar.getInstance();
        //this is the base layout for the entire dashboard
        primaryView = (ScrollView)inflater.inflate(R.layout.dashboard_layout,group,false);
        barChartLayout = (LinearLayout) inflater.inflate(R.layout.bar_chart_container,group,false);
        ((LinearLayout)primaryView.findViewById(R.id.dashboard_parent)).addView(barChartLayout);
        periodButton = (Button)barChartLayout.findViewById(R.id.period_button);
        periodButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                initPeriodButton();
            }
        });
        initPeriodButton();
        addRevenueLayout();
        return primaryView;
    }

    /**
     * this method is responsible for inflating and inserting the revenue layout
     */
    private void addRevenueLayout()
    {
        revenueSummary = (LinearLayout) inflater.inflate(R.layout.revenue_summary,primaryView,false);
        reference = new WeakReference<DashboardFragment>(this);

        revenueSummary.findViewById(R.id.date).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //UtilityClass.showToast("hello there");
                new CalendarClass(reference.get(),"Choose date to view revenue");
            }
        });
        title = (CustomTextView)revenueSummary.findViewById(R.id.title);
        title.setText("Revenue summary for Today");
        fetchRevenue();
        ((LinearLayout)primaryView.findViewById(R.id.dashboard_parent)).addView(revenueSummary);
    }

    private void fetchRevenue()
    {
        DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
        {
            @Override
            public void run()
            {
                //for now, only show revenues for today, in the future, we'd allow user to choose dates
                final double saleValue = SalesInfoFragmentLogic.getSum(position);
                final double incomeVal = IncomeFragmentLogic.getTotalIncome(position);
                final double expenseVal = ExpenseFragmentLogic.getTotalExpense(position);
                final double purchaseVal = PurchaseFragmentLogic.getTotalExpense(position);
                final double debtPaymentVal= DebtorPaymentHistoryLogic.getTotalDebtPayment(position);
                //final double balanceVal = 0;

                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        TextView sale = (TextView)revenueSummary.findViewById(R.id.sale);
                        TextView income = (TextView)revenueSummary.findViewById(R.id.income);
                        TextView expense = (TextView)revenueSummary.findViewById(R.id.expense);
                        TextView purchase = (TextView)revenueSummary.findViewById(R.id.purchase);
                        TextView balance = (TextView)revenueSummary.findViewById(R.id.balance);
                        TextView debtPayment = (TextView)revenueSummary.findViewById(R.id.debt_payment);

                        sale.setText(UtilityClass.formatMoney(saleValue));
                        income.setText(UtilityClass.formatMoney(incomeVal));
                        expense.setText(UtilityClass.formatMoney(expenseVal));
                        purchase.setText(UtilityClass.formatMoney(purchaseVal));
                        debtPayment.setText(UtilityClass.formatMoney(debtPaymentVal));
                        double balanceVal = saleValue+incomeVal+debtPaymentVal-expenseVal - purchaseVal;
                        if(balanceVal<0)
                        {
                            balanceVal = balanceVal * -1;
                            balance.setText(UtilityClass.formatMoney(balanceVal));
                            balance.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.pomegranate));
                        }
                        else
                        {
                            balance.setText(UtilityClass.formatMoney(balanceVal));
                            balance.setTextColor(UtilityClass.getContext().getResources().getColor(R.color.green_sea));
                        }

                        sale = null;
                        income = null;
                        expense = null;
                        balance = null;
                    }
                });
            }
        });
    }

    /**
     * this method initializes the period button. When this button is pressed, the chart displayed
     * is toggled from days of the week to months of year
     */
    private void initPeriodButton()
    {
        if(period == 1)
        {
            dialog = UtilityClass.getProgressDialog(new WeakReference<Activity>((AppCompatActivity)getActivity()));
            dialog.show();
            periodButton.setText("Days");
            ((LinearLayout)barChartLayout.findViewById(R.id.bar_chart)).removeAllViews();
            //all database queries are moved off the UI thread
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                @Override
                public void run()
                {
                    getValuesForThisWeek();
                    //getValuesForThisYear();
                    period =2;
                }
            });
        }
        else if(period == 2)
        {
            dialog = UtilityClass.getProgressDialog(new WeakReference<Activity>((AppCompatActivity)getActivity()));
            dialog.show();
            periodButton.setText("Months");
            ((LinearLayout)barChartLayout.findViewById(R.id.bar_chart)).removeAllViews();
            //all database queries are moved off the UI thread
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable() {
                @Override
                public void run()
                {
                    //getValuesForThisWeek();
                    getValuesForThisYear();
                    period =1;
                }
            });
        }
    }

    /**
     * this method is called to compute and display the sales for the week using
     * a bar chart.
     */
    private void getValuesForThisWeek()
    {
        int day = calendar.get(Calendar.DAY_OF_MONTH);//date in the month e.g 23
        int month = calendar.get(Calendar.MONTH)+1;//counting is zero based i.e January is 0
        int year = calendar.get(Calendar.YEAR);
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);//sunday,monday ..... sunday is 1
        int count = 0;
        int loop = day_of_week -1;
        boolean useModulo = false;//this indicates if modulo should be used
        int modulo = -1;
        day-=loop;
        total = new double[7];
        double debtPayment,sale;
        Logger.log(TAG,"This is value of day: "+day);
        Log.d(TAG,"This is value of day: "+day);
        max = 0;
        //this is the condition where we are in the first week and subtracting the DAY_OF_WEEK from
        //the current date would give a negative number. For example, say the date is 2nd and we are
        //on thursday (5), subtract 5 from 2, we land at a negative number. Furthermore, we have to
        //check if the month is January, if it is we'd have to go back one year.
        if(day < 0)
        {
            useModulo = true;
            //if the current month is january, we'd have to go back to December, else we subtract one
            //because we added one before.
            month = calendar.get(Calendar.MONTH)==0 ? 11: --month;
            //if the current month is january, we'd like to go back one year.
            year = calendar.get(Calendar.MONTH) ==0 ? --year:year;
            int prevMonth = month-1;
            if(prevMonth == 3 || prevMonth == 5 || prevMonth == 10 || prevMonth == 8)
                modulo = 30;
            else if (prevMonth == 1)
            {
                if(year%4 == 0) //leap year
                    modulo = 29;
                else
                    modulo = 28;
            }
            else
                modulo = 31;

            day = modulo + day;
            //month = prevMonth;
            Logger.log(TAG,"day < 0: "+year + " " + month + " " + loop + " " + modulo);
            Log.d(TAG,"day < 0: "+year + " " + month + " " + loop + " " + modulo);
        }

        while(count < 7)
        {
            loop = day+count;
            if(useModulo && loop>modulo)
            {
                loop = loop%modulo;
                //if loop is 1, then we want to increase the month.
                month = loop ==1 ? ++month :month;
                //if current month is "0", we'd increase year by 1.
                year = (loop==1)&&(calendar.get(Calendar.MONTH) == 0)? ++year:year;
            }
            Logger.log(TAG,"In while loop: "+year + " " + month + " " + loop + " " + modulo);
            Log.d(TAG,"In while loop: "+year + " " + month + " " + loop + " " + modulo);
            //fetch value for sales
            String tableName = "sale_transaction";
            String rowName = "amount_paid";//for now leave this as total_cost. It should be amount_paid
            String minDate = UtilityClass.getDateTime(year, month, loop, 0, 0, 0);
            String maxDate = UtilityClass.getDateTime(year,month,loop,23,59,59);
            String whereArgs = "sale_transaction.suspended = '0' AND " +
                    " sale_transaction.archived = '0' AND sale_transaction.last_edited >= '"+
                    minDate+"' AND sale_transaction.last_edited <= '"+maxDate+"'";
            //String join = UtilityClass.getJoinQuery("sale_transaction",whereArgs,"");
            sale = DatabaseManager.sumUpRowsWithWhere(tableName,rowName,whereArgs);
            //fetch value for debt payment
            tableName = "debt_payment";
            whereArgs =" debt_payment.archived = '0' AND debt_payment.last_edited >= '"+
                    minDate+"' AND debt_payment.last_edited <= '"+maxDate+"'";
            debtPayment = DatabaseManager.sumUpRowsWithWhere(tableName,rowName,whereArgs);
            total[count]=sale+debtPayment;
            if(total[count] > max)
                max = total[count];
            count+=1;
        }

        //Views must be updated from the UI thread, hence the need for this handler.
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                drawBarChart(total, daysOfTheWeek);
            }
        });
    }

    /**
     * this method is called to compute and display the sales for the week using
     * a bar chart.
     */
    private void getValuesForThisYear()
    {
        int day = calendar.get(Calendar.DAY_OF_MONTH);//date in the month e.g 23
        int month = 0;//counting is zero based i.e January is 0
        int year = calendar.get(Calendar.YEAR);
        double sale,debt=0;
        total = new double[12];
        max = 0;
        while(month < 12)
        {
            //System.out.println(year + " " + month + " " + loop + " " + modulo);
            String tableName = "sale_transaction";
            String rowName = "amount_paid";//for now leave this as total_cost. It should be amount_paid

            String minDate = UtilityClass.getDateTime(year, month+1, 1, 0, 0, 0);
            String maxDate = UtilityClass.getDateTime(year,month+1,31,23,59,59);
            String whereArgs = "sale_transaction.suspended = '0' AND " +
                                 " sale_transaction.archived = '0' AND sale_transaction.last_edited >= '"+
                                    minDate+"' AND sale_transaction.last_edited <= '"+maxDate+"'";
            //String join = UtilityClass.getJoinQuery("sale_transaction",whereArgs,"");
            //get total for sales
            sale = DatabaseManager.sumUpRowsWithWhere(tableName,rowName,whereArgs);
            //fetch value for debt payment
            tableName = "debt_payment";
            whereArgs =" debt_payment.archived = '0' AND debt_payment.last_edited >= '"+
                    minDate+"' AND debt_payment.last_edited <= '"+maxDate+"'";
            debt = DatabaseManager.sumUpRowsWithWhere(tableName,rowName,whereArgs);
            total[month]=sale+debt;

            if(total[month] > max)
                max = total[month];
            month+=1;
        }

        //Views must be updated from the UI thread, hence the need for this handler.
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                drawBarChart(total, months);
            }
        });
    }

    /**
     * this method draws the bar chart. It has to be run on the UI thread
     * @param values values to be resented with the bar chart.
     */
    private void drawBarChart(double[] values, String[] label)
    {
        int loop = 0;
        while (loop <values.length)
        {
            LinearLayout chart = (LinearLayout)inflater.inflate(R.layout.bar_chart_element,primaryView,false);
            ((TextView)chart.findViewById(R.id.label)).setText(label[loop]);//set the label

            //format the money value appending K, M, B
            String value = null;
            if(values[loop] < 999)
                value = UtilityClass.getNairaSign()+values[loop];
            else if(values[loop] < 999999)
                value = UtilityClass.getNairaSign()+String.format("%.1f",values[loop]/1000)+"K";
            else if(values[loop] < 999999999)
                value = UtilityClass.getNairaSign()+String.format("%.1f", values[loop]/1000000)+"M";
            else
                value = UtilityClass.getNairaSign()+String.format("%.1f", values[loop]/1000000000)+"B";

            ((TextView) chart.findViewById(R.id.value)).setText(value);

            int width = UtilityClass.getScreenWidth()/7;
            int pseudoHeight = UtilityClass.convertToPixels(10);
            int height = UtilityClass.convertToPixels((int)(values[loop]/max*100));
            height = height < pseudoHeight ? pseudoHeight+height:height;
            //format the bar. set the height, width and background color
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
            params.setMargins(10,10,10,10);
            chart.findViewById(R.id.bar).setLayoutParams(params);
            chart.findViewById(R.id.bar).setMinimumHeight(UtilityClass.convertToPixels(10));
            chart.findViewById(R.id.bar).setBackgroundColor(getResources().getColor(colors[loop]));

            ((LinearLayout)barChartLayout.findViewById(R.id.bar_chart)).addView(chart);
            chart = null;
            params = null;
            loop++;
        }

        dialog.dismiss();
        dialog = null;
    }

    @Override
    public void onDatePicked(long datePicked)
    {
        position = UtilityClass.getDateDifferenceFromToday(datePicked);
        String date = (position == 0)?"Today":UtilityClass.getDate(datePicked);
        title.setText("Revenue summary for "+date);
        fetchRevenue();
    }

    @Override
    public WeakReference<Activity> getActivityWeakReference()
    {
        return new WeakReference<Activity>(getActivity());
    }
}
