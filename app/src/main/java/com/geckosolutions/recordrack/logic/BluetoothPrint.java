package com.geckosolutions.recordrack.logic;

/**
 * Created by anthony1 on 9/21/16.
 */

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import com.geckosolutions.recordrack.R;
import com.geckosolutions.recordrack.adapters.BluetoothDevicesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothPrint implements ActivityCompat.OnRequestPermissionsResultCallback
{
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    // needed for communication to bluetooth device / network
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private String customerName,transactID,date, time,user;
    private ArrayList<JSONObject> data;
    private JSONArray data1;
    private double amountPaid, total, balance;
    private WeakReference<Activity> activityWeakReference;
    private boolean isPrintingEnabled;
    private String bluetoothPrinter;
    private int[][] imageData;
    private Dialog dialog;
    private BluetoothDevicesListAdapter adapter;
    private static BluetoothPrint bluetoothPrint;
    private BluetoothDevice bondingDevice;
    private String storeInfo[];
    private String spaces[] = new String[]{""," ","  ","   ","    ","     ","      ","       ","        ",
            "         ","          ","           ","            ","             ","              ","               ",
            "                ","                 ","                  ","                    ",
            "                    "};
    private int wid,hei;
    private int pixels[];
    private static final  String TAG="BluetoothPrint";

    /**
     * Constructor for the bluetooth print class
     * @param reference a reference to the activity from which this object was created
     * @param customerName the name of the customer
     * @param data data to be printed
     * @param total this represents the sum total for the specified transaction
     * @param amountPaid this represents the amount paid
     * @param dateTime when the transaction was performed
     * @param transactionID transact id for the transaction
     */
    public BluetoothPrint(WeakReference<Activity> reference, String customerName, String user, ArrayList<JSONObject> data, double total, double amountPaid,String[] dateTime,String transactionID)
    {
        activityWeakReference = reference;
        this.customerName = customerName;
        this.user = user;
        this.data = data;
        this.total = total;
        this.amountPaid = amountPaid;
        this.date = dateTime[0];
        this.time = dateTime[1];
        this.transactID = transactionID;
        bluetoothPrint = this;
        //bluetoothPrint = this;
    }


    public BluetoothPrint(WeakReference<Activity> reference,String transactID,String customerName,String user,double total,double amountPaid,String []dateTime,JSONArray array)
    {
        activityWeakReference = reference;
        this.transactID = transactID;
        this.customerName = customerName;
        this.user = user;
        this.total = total;
        this.amountPaid = amountPaid;
        this.date = dateTime[0];
        this.time =dateTime[1];
        data1 = array;
        bluetoothPrint = this;
    }

    public BluetoothPrint(JSONArray array) throws JSONException
    {
        data = null;
        data1 = array;
        String dateTime[] = UtilityClass.getDateAndTime(array.getJSONObject(0).getString("last_edited"));
        this.date = dateTime[0];
        this.time = dateTime[1];
        this.customerName = array.getJSONObject(0).getString("name");
        bluetoothPrint = this;
    }

    // this will find a bluetooth printer device

    /**
     * this method is usually called after the bluetooth object is created. It is called on the
     * bluetooth print thread
     */
    public  void beginPrintingReceipt()
    {
        //if printing is enabled and printer has been set, go ahead and print
        isPrintingEnabled = Settings.isPrintingEnabled();
        bluetoothPrinter = Settings.getPrinterName();
        storeInfo = DatabaseManager.getBusinessDetails();
        Logger.log(TAG,"Bluetooth enabled "+isPrintingEnabled+" printer name:"+bluetoothPrinter);
        Log.d(TAG,"Bluetooth enabled "+isPrintingEnabled+" printer name:"+bluetoothPrinter);
        if( isPrintingEnabled && bluetoothPrinter !=null)
            turnBluetoothOn();
        //else, let the user tell us if they'd like to print the receipt.
        else
        {
            //this has to be moved to the UI thread
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    dialog = UtilityClass.showCustomDialog(R.layout.print_receipt_layout,activityWeakReference,150);
                    dialog.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            //if yes, we'd like to start scanning for bluetooth devices
                            dialog.dismiss();
                            dialog = null;
                            displayBluetoothDevicesDialog();
                            //turn on bluetooth
                            turnBluetoothOn();
                        }
                    });

                    dialog.findViewById(R.id.no).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                            dialog = null;
                        }
                    });
                }
            });
        }
    }

    /**
     * this method is responsible for displaying the bluetooth devices dialog. It's the dialog that
     * populates all found bluetooth devices
     */
    private void displayBluetoothDevicesDialog()
    {
        dialog = UtilityClass.showCustomDialog(R.layout.bluetooth_discovery_layout,activityWeakReference);
        dialog.setCancelable(false);
        adapter = new BluetoothDevicesListAdapter();
        ((ListView)dialog.findViewById(R.id.list_view)).setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                ReceiptPrintingThread.getReceiptPrintingThread().postPrintingTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        pairWithDevice(adapter.getBluetoothDevice(position));
                        dialog.dismiss();
                    }
                });
            }
        });
        ((ListView)dialog.findViewById(R.id.list_view)).setAdapter(adapter);
        //define what happens when user want's to scan for devices again
        dialog.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UtilityClass.getBluetoothDevicesListAdapter().clearItems();
                //notify user of discovery in progress
                dialog.findViewById(R.id.discovery_complete).setVisibility(View.GONE);
                dialog.findViewById(R.id.discovery_progress).setVisibility(View.VISIBLE);
                bluetoothPrinter = null;
                turnBluetoothOn();
            }
        });
        //dismiss dialog when user selects close
        dialog.findViewById(R.id.close).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                cancelBluetoothDiscovery();
            }
        });
    }

    private void cancelBluetoothDiscovery()
    {
        ReceiptPrintingThread.getReceiptPrintingThread().postPrintingTask(new Runnable()
        {
            @Override
            public void run()
            {
                boolean result = mBluetoothAdapter.cancelDiscovery();
                if(result)
                    UtilityClass.showToast("Discovery cancelled ");
                else
                    UtilityClass.showToast("Discovery wasn't cancelled successfully");
            }
        });
    }

    /**
     * This method is called to turn the bluetooth adapter on and start network discovery process.
     * This is run on the receipt printing thread.
     */
    private void turnBluetoothOn()
    {
        ReceiptPrintingThread.getReceiptPrintingThread().postPrintingTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    //if no bluetooth adapter available
                    if(mBluetoothAdapter == null)
                    {
                        UtilityClass.showToast("No bluetooth adapter available");
                        return;
                        //myLabel.setText("No bluetooth adapter available");
                    }

                    //if bluetooth is off,we'd like to enable it
                    if(!mBluetoothAdapter.isEnabled())
                    {
                        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        activityWeakReference.get().startActivityForResult(enableBluetooth, 0);
                    }

                    //if no bluetooth printer has been saved, we'd like to discover it
                    //connectToBTDevice();
                    if(bluetoothPrinter == null)
                        mBluetoothAdapter.startDiscovery();
                    else
                        connectToBTDevice();
                }
                catch(Exception e)
                {
                    Logger.writeException(e);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * this method is called when the user selects what device to pair with. this is only called
     * when the user is trying to pair with a bluetooth printer for the first time.
     * @param device the device to be paired with.
     */
    private void pairWithDevice(final BluetoothDevice device)
    {
        try
        {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            //UUID uuid = UUID.fromString("DC:0D:30:02:23:B2");
            mmSocket = device .createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            if (bondingDevice!=null && bondingDevice.getName().equalsIgnoreCase(device.getName()))
            {
                //dismiss dialog on the UI thread
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        dialog.dismiss();
                    }
                });
            }
            bluetoothPrinter = device.getName();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            sendData();
            //once printing is successful the first time, we should go ahead and store the
            //printer name and enable printing
            DatabaseThread.getDatabaseThread().postDatabaseTask(new Runnable()
            {
                @Override
                public void run()
                {
                    Logger.log(TAG,"Enabling printing in the db");
                    Log.d(TAG,"Enabling printing in the db");
                    Settings.setPrintingEnabled();
                    Settings.setPrinterName(device.getName());
                }
            });
            //Method method = device.getClass().getMethod("createBond", (Class[]) null);
            //method.invoke(device, (Object[]) null);
        }
        catch (IOException e)
        {
            Logger.writeException(e);
            notifyUserOfPrintingException("There was an issue printing to the printer. Please make sure it is on and try again.");
            e.printStackTrace();
        }
    }

    /**
     * this method is called to notify user of exceptions that occurred during printing. Move this
     * to the UI thread
     */
    private void notifyUserOfPrintingException(final String text)
    {
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run()
            {
                final Dialog dialog = UtilityClass.showCustomDialog(R.layout.notification_layout,activityWeakReference);
                ((TextView)dialog.findViewById(R.id.text)).setText(text);
                dialog.findViewById(R.id.got_it).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    /**
     * this method is called from the turnbluetoothon method. It attempts to look for an already bonded
     * printer and connects with it.
     */
    private void connectToBTDevice()
    {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                Logger.log(TAG,"Bluetooth device : "+device.getName()+"printer: "+bluetoothPrinter);
                Log.d(TAG,"Bluetooth device : "+device.getName()+"printer: "+bluetoothPrinter);
                // RPP300 is the name of the bluetooth printer device
                // we got this name from the list of paired devices
                if ((device.getName()).equals(bluetoothPrinter))
                //if (device.getName().equals("Printer001"))
                //if (device.getName().equals("BlueTooth Printer"))
                {
                    mmDevice = device;
                    try
                    {
                        openBT();
                        UtilityClass.showToast("Bluetooth device found.");
                    }
                    catch (IOException e)
                    {
                        Logger.writeException(e);
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    /**
     * called from connectToBTDevice.  It just generates the connection streams to the printer.
     * @throws IOException
     */
    private void openBT() throws IOException
    {
        try
        {
            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            //UUID uuid = UUID.fromString("DC:0D:30:02:23:B2");

            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            sendData();
            //beginListenForData();

            //myLabel.setText("Bluetooth Opened");

        } catch (Exception e)
        {
            Logger.writeException(e);
            notifyUserOfPrintingException("An error occurred while connecting to the printer with name "
                    +bluetoothPrinter+". One way of fixing this is to ensure printer is on or re-pair with it.");
            e.printStackTrace();
        }
    }

    private void sendData() throws IOException
    {
        try
        {
            StringBuilder builder = new StringBuilder();
            String category = null,item=null,qty=null,price=null;
            PrintStream printStream = new PrintStream(mmOutputStream);
            JSONObject object = null;


            printStream.write(initPrinter());
            printStream.write(setStartPrintPosition());
            printStream.write(setAlignment((byte)49));//center align
            printStream.write(setEmphasizedMode((byte)1));//turn on emphasized mode
            printStream.write(storeInfo[0].getBytes());//write store name
            printStream.write(setEmphasizedMode((byte)0));//turn off emphasized mode
            printStream.write(newLine());//LF new line
            printStream.write(storeInfo[1].getBytes());//write address
            printStream.write(newLine());
            printStream.write(storeInfo[2].getBytes());//write phone number
            printStream.write(newLine());//LF new line
            printStream.write(setAlignment((byte)48));//ESC,a value (align left
            printStream.write(newLine());//LF new line



            /*list.add(new byte[] { 27, 68, 16, 0 });//ESC,D,DLE,NULL  (set horizontal tab)
            list.add(new byte[] { 27, 29, 97, 49 });//ESC,GS,a,1     ()

            list.add(new byte[] { 27, 105, 2, 0 });//ESC,i,Start of Text,NULL ()
            list.add(new byte[] { 27, 69 });//ESC,E      (set emphasized mode)*/
            //mmOutputStream.write(new byte[]{10});//print and line feed
            builder.append("Cashier:       "+user+"\n");
            builder.append("Customer Name: "+customerName+"\n");
            builder.append("Receipt No:    "+transactID+"\n");
            builder.append("Date:          "+date+" / "+time+"\n\n");

            printStream.print(builder.toString());
            printStream.write(setEmphasizedMode((byte)1));//turn on emphasize mode

            //write Category, Item Qty and Price in bold
            printStream.print("Category   Item                Qty    Price ");
            printStream.write(setLineSpacing((byte)50));
            printStream.print("\n");
            printStream.write(setLineSpacing((byte)25));

            printStream.write(setEmphasizedMode((byte)0));//turn off emphasized mode
            builder = new StringBuilder();//create a new string builder
//            System.out.println("this is size of data "+data.size());
            if(data != null)//if receipt is being generated immediately after sales
            {
                for (int i=0; i< data.size(); i++)
                {
                    object = data.get(i);
                    category = object.getString("category");
                    item = object.getString("item");
                    qty = object.getString("quantity")+" "+object.getString("unit");
                    price = "N "+UtilityClass.formatMoneyWithoutNairaSign(object.getDouble("cost"));

                    //add category
                    builder.append(category);
                    if(category.length()<11)
                    {
                        builder.append(spaces[11-category.length()]);
                        /*for (int j = 0;j<11 -category.length();j++)
                            builder.append(" ");*/
                    }

                    //add item
                    builder.append(item);
                    if(item.length()<20)
                    {
                        builder.append(spaces[20-item.length()]);
                        /*for (int j = 0;j<20 -item.length();j++)
                            builder.append(" ");*/
                    }

                    //add qty
                    builder.append(qty);
                    if(qty.length()<7)
                    {
                        builder.append(spaces[7-qty.length()]);
                        /*for (int j = 0;j<7 -qty.length();j++)
                            builder.append(" ");*/
                    }

                    //add price
                    builder.append(price);

                    //builder.append("N"+object.getString("cost"));
                    builder.append("\n\n");//new line
                    object = null;
                }
            }
            else //receipt is being generated after sales
            {
                for (int i=0; i< data1.length(); i++)
                {
                    try
                    {
                        object = data1.getJSONObject(i);
                        category = object.getString("category");
                        item = object.getString("item");
                        qty = object.getString("quantity")+" "+object.getString("unit");
                        price = "N "+UtilityClass.formatMoneyWithoutNairaSign(object.getDouble("cost"));

                        //add category
                        builder.append(category);
                        //builder.append(category);
                        if(category.length()<11)
                        {
                            builder.append(spaces[11-category.length()]);
                        /*for (int j = 0;j<11 -category.length();j++)
                            builder.append(" ");*/
                        }

                        //add item
                        builder.append(item);
                        if(item.length()<20)
                        {
                            builder.append(spaces[20-item.length()]);
                        /*for (int j = 0;j<20 -item.length();j++)
                            builder.append(" ");*/
                        }

                        //add qty
                        builder.append(qty);
                        if(qty.length()<7)
                        {
                            builder.append(spaces[7-qty.length()]);
                        /*for (int j = 0;j<7 -qty.length();j++)
                            builder.append(" ");*/
                        }

                        //add price
                        builder.append(price);

                        //builder.append("N"+object.getString("cost"));
                        builder.append("\n\n");//new line
                        object = null;
                    }
                    catch (JSONException e)
                    {
                        Logger.writeException(e);
                        e.printStackTrace();
                    }
                }
            }

            //builder.append("Total: "+"\t\t N"+msg+"");
            //builder.append(new byte[]{27,105});
            //builder.append((char)27+(char)26);
            printStream.print(builder.toString());//write data to the printer
            printStream.flush();
            printStream.print("\n\n");//create line feed to insert total & amount paid
            //format total to be printed printed


            printStream.println(prepareAmountsForPrinting("Total: N "+UtilityClass.formatMoneyWithoutNairaSign(total)));
            printStream.println(prepareAmountsForPrinting("Amount Paid: N "+UtilityClass.formatMoneyWithoutNairaSign(amountPaid)));
            printStream.println(prepareAmountsForPrinting("Balance: N "+UtilityClass.formatMoneyWithoutNairaSign(total-amountPaid)));
            //printStream.print("                                 Total: N "+UtilityClass.formatMoneyWithoutNairaSign(total));
            //printStream.print("                           Amount Paid: N "+UtilityClass.formatMoneyWithoutNairaSign(amountPaid));
            //printStream.print("                               Balance: N "+UtilityClass.formatMoneyWithoutNairaSign(total-amountPaid));


            printStream.flush();
            //mmOutputStream.write(newLine());
            //mmOutputStream.write(newLine());
            //mmOutputStream.write(getLineSeparator().getBytes());
            //mmOutputStream.write(newLine());
            //mmOutputStream.write(setAlignment((byte)49));//center alignment
            //defineNVImageBits();
            //mmOutputStream.flush();
            //mmOutputStream.write(newLine());
            //mmOutputStream.write("This receipt was generated using the Record Rack app. You too can begin managing your business today for FREE. Visit www.googleplay.com to get started".getBytes());
            //mmOutputStream.write(newLine());
            //mmOutputStream.write(newLine());

            //mmOutputStream.write(setAlignment((byte)0));//left alignment
            printStream.print("\n\n");
            printStream.flush();
            printStream.write(setAlignment((byte)49));//center align
            printStream.print("Powered by Record Rack");
            //mmOutputStream.write(newLine());
            //mmOutputStream.write(setHorizontalTab());

            //mmOutputStream.write(getLineSeparator().getBytes());

            printStream.flush();
            //printStream.write(preCut());
            printStream.print("\n\n\n\n\n\n\n");
            printStream.write(finishAndCutPaper());
            //printStream.flush();
            //mmOutputStream.write(new byte[]{27,105});//cause printer to cut paper
            //mmOutputStream.write(new byte[]{0x1B,0x69});
            printStream.flush();

            // tell the user data were sent
            UtilityClass.showToast("Data sent for printing");

            if(data != null)
            {
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        data.clear();
                    }
                });
            }
            closeBT();
            //myLabel.setText("Data sent.");

        } catch (Exception e)
        {
            Logger.writeException(e);
            notifyUserOfPrintingException("An error occurred while printing to the printer with name "
                    +bluetoothPrinter+". Please make sure printer is on and discoverable");
            e.printStackTrace();
        }
    }

    /**
     * this method is called to format the money for printing.
     * @param tot the text to be printed
     * @return the formatted text
     */
    private String prepareAmountsForPrinting(String tot)
    {
        int c = (45-tot.length())/20;
        int d = (45-tot.length())%20;
        Logger.log(TAG,"Amount being prepared for printing: "+tot+" :"+c+" :"+d);
        Log.d(TAG,"Amount being prepared for printing: "+tot+" :"+c+" :"+d);
        String temp = null;
        if(c >0 | d>0)
        {
            for (int i=0;i<c;i++)
            {
                temp = spaces[20] + tot;
                tot = temp;
            }
            tot= spaces[d]+tot;
        }

        return tot;
    }

    // close the connection to bluetooth printer.
    private void closeBT() throws IOException
    {
        try
        {
            //stopWorker = true;
            mmOutputStream.flush();
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            //myLabel.setText("Bluetooth Closed");
        } catch (Exception e)
        {
            Logger.writeException(e);
            e.printStackTrace();
        }
    }

    //returns the ESC new line character
    private byte[] newLine()
    {
        return new byte[]{10};//LF new line
    }

    //return bytes to initialize the printer
    private byte[] initPrinter()
    {
        return new byte[]{27,64};//initialize printer
    }

    //return bytes to mark the beginning of printing
    private byte[] setStartPrintPosition()
    {
        return new byte[] { 27, 36,1,-100 };//ESC,$,1 (set print start position)
    }

    /**
     * return bytes to center align text.
     * @param n byte specifying if its left,center or right alignment
     *          48 left align, 49 center
     */
    private byte[] setAlignment(byte n)
    {
        return new byte[]{27,97,n};//ESC,a value (align center)
    }

    //turn off or on the emphasized mode. 0 for off, 1 for on
    private byte[] setEmphasizedMode(byte n)
    {
        return new byte[] { 27, 69, n };//ESC,E   (set emphasized mode 1 ON,0 OFF)
    }

    /**
     * method to return bytes that feeds line and cuts the paper
     * @return bytes to feed and cut paper
     */
    private byte[] finishAndCutPaper()
    {
        //return new byte[]{27,105};
        String GS = Character.toString((char)29);
        String ESC = Character.toString((char)27);
        String COMMAND = "";
        COMMAND = ESC + "@";
        COMMAND += GS + "V" + (char)1;
        //return COMMAND.getBytes();
        //return (GS+"V"+"66"+"0").getBytes();
        return new byte[]{29,'V',65,0};
    }

    private byte[] preCut()
    {
        String ESC = Character.toString((char)27);
        String J = "J";
        String X = Integer.toString(200);
        return (ESC+J+X).getBytes();
    }

    private byte[] setHorizontalTab()
    {
        return new byte[]{27,68,5,1};//ESC,D 5 columns, 1 tab position
    }

    private String getLineSeparator()
    {
        return "******************************************";
    }

    private void defineNVImageBits() throws IOException
    {
        Bitmap bmp = BitmapFactory.decodeResource(UtilityClass.getContext().getResources(),R.drawable.record_rack_small);
        imageData = new int[bmp.getHeight()][bmp.getWidth()];
        for (int row=0;row<bmp.getHeight();row++)
        {
            for (int col = 0; col<bmp.getWidth();col++)
            {
                imageData[row][col] = bmp.getPixel(col,row);
            }
        }
        printImage(imageData);
    }


    private void printImage(int[][] pixels) throws IOException
    {
        // Set the line spacing at 24 (we'll print 24 dots high)
        mmOutputStream.write(setLineSpacing((byte)24));
        for (int y = 0; y < pixels.length; y += 24) {
            // Like I said before, when done sending data,
            // the printer will resume to normal text printing
            mmOutputStream.write(selectBitImageMode());
            // Set nL and nH based on the width of the image
            mmOutputStream.write(new byte[]{(byte)(0x00ff & pixels[y].length)
                    , (byte)((0xff00 & pixels[y].length) >> 8)});
            for (int x = 0; x < pixels[y].length; x++) {
                // for each stripe, recollect 3 bytes (3 bytes = 24 bits)
                mmOutputStream.write(recollectSlice(y, x, pixels));
            }

            // Do a line feed, if not the printing will resume on the same line
            mmOutputStream.write(newLine());

        }

       // mmOutputStream.write("Hello there".getBytes());
        //mmOutputStream.write(setLineSpacing((byte)60));
    }

    private byte[] recollectSlice(int y, int x, int[][] img) {
        byte[] slices = new byte[] {0, 0, 0};
        for (int yy = y, i = 0; yy < y + 24 && i < 3; yy += 8, i++) {
            byte slice = 0;
            for (int b = 0; b < 8; b++) {
                int yyy = yy + b;
                if (yyy >= img.length) {
                    continue;
                }
                int col = img[yyy][x];
                boolean v = shouldPrintColor(col);
                slice |= (byte) ((v ? 1 : 0) << (7 - b));
            }
            slices[i] = slice;
        }

        return slices;
    }

    private boolean shouldPrintColor(int col) {
        final int threshold = 127;
        int a, r, g, b, luminance;
        a = (col >> 24) & 0xff;
        if (a != 0xff) {// Ignore transparencies
            return false;
        }
        r = (col >> 16) & 0xff;
        g = (col >> 8) & 0xff;
        b = col & 0xff;

        luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);

        return luminance < threshold;
    }

    private byte[] selectBitImageMode()
    {
        return new byte[]{27,42,33};//ESC,* 33 represents 24 dot double density
    }

    private byte[] setLineSpacing(byte n)
    {
        return new byte[]{27,51,n};
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

    }

    /**
     * this is called from the bluetooth receiver class.
     * @param btName
     */
    public static void dismissDialog(String btName)
    {
        if(bluetoothPrint.dialog !=null && bluetoothPrint.dialog.isShowing())
        {
            bluetoothPrint.dialog.dismiss();
            bluetoothPrint.bluetoothPrinter = btName;
            bluetoothPrint.connectToBTDevice();
        }

    }

    /**
     * this is called from the BluetoothReceiver class to set a device that's being bonded with.
     * @param device the device the bluetooth adapter is trying to bond with
     */
    public static void setBondingDevice(BluetoothDevice device)
    {
        bluetoothPrint.bondingDevice=device;
    }

    /**
     * called from the bluetooth discovery class to show the user that bluetooth discovery is complete
     */
    public static void setDiscoveryFinished()
    {
        if(bluetoothPrint.dialog !=null && bluetoothPrint.dialog.isShowing())
        {
            View discoveryProgress = bluetoothPrint.dialog.findViewById(R.id.discovery_progress);
            View discoveryComplete = bluetoothPrint.dialog.findViewById(R.id.discovery_complete);
            if(discoveryProgress!=null && discoveryComplete!=null)
            {
                discoveryProgress.setVisibility(View.GONE);
                discoveryComplete.setVisibility(View.VISIBLE);
            }
        }
    }
}

