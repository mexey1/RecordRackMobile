package com.geckosolutions.recordrack.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import com.geckosolutions.recordrack.exceptions.ThreadException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by anthony1 on 1/18/16.
 */
public class DatabaseManager extends SQLiteOpenHelper
{
    /*******************
     * Archived ==1 means deleted, 0 means not deleted
     * Suspended == 1 means suspended transaction, 0 means not suspended
     * All edits result in the item being deleted and then re-saved. Only exception is the unit table
     */
    private static DatabaseManager dbManager;
    private static int dbCount;
    private static String business_details = "CREATE TABLE IF NOT EXISTS business_details(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, address TEXT DEFAULT NULL, type TEXT DEFAULT NULL, establishment_year TEXT NOT NULL, phone_number TEXT DEFAULT NULL, business_logo BLOB, extra_details TEXT DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL) ";
    private static String user = "CREATE TABLE IF NOT EXISTS user(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, username TEXT UNIQUE, password TEXT, secret_question TEXT NOT NULL, answer TEXT NOT NULL) ";
    private static String category = "CREATE TABLE IF NOT EXISTS category(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category VARCHAR(100) UNIQUE NOT NULL, short_form VARCHAR(25) DEFAULT NULL, archived TINYINT(4) NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String client = "CREATE TABLE IF NOT EXISTS client(id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT DEFAULT NULL, last_name TEXT DEFAULT NULL, preferred_name TEXT NOT NULL, phone_number TEXT UNIQUE NOT NULL, alternate_phone_number TEXT NOT NULL, address TEXT DEFAULT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String credit_payment = "CREATE TABLE IF NOT EXISTS credit_payment(id INTEGER PRIMARY KEY AUTOINCREMENT, credit_transaction_id INTEGER NOT NULL, total_debt DECIMAL(19,2) NOT NULL, amount_paid DECIMAL(19,2) NOT NULL, balance DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, due_date DATETIME NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String credit_transaction = "CREATE TABLE IF NOT EXISTS credit_transaction(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, client_id INTEGER NOT NULL, transaction_table TEXT NOT NULL, transaction_id INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";

    private static String creditor = "CREATE TABLE IF NOT EXISTS creditor(id INTEGER PRIMARY KEY AUTOINCREMENT, client_id INTEGER NOT NULL, credit DECIMAL(19,2) NOT NULL, last_due_date DATETIME NOT NULL, currency VARCHAR(4) NOT NULL, archived INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    //quantity values in the current quantity table are in base unit
    private static String current_quantity = "CREATE TABLE IF NOT EXISTS current_quantity(id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER UNIQUE, quantity DOUBLE, created DATETIME, last_edited DATETIME, user_id INTEGER)";
    private static String customer = "CREATE TABLE IF NOT EXISTS customer(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, client_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String db_info = "CREATE TABLE IF NOT EXISTS db_info(version_number VARCHAR(15), rack_id VARCHAR(20), current_backup_filename VARCHAR(70), last_upload DATETIME, validation_key VARCHAR(150), expiry_date DATETIME, created DATETIME, last_edited DATETIME)";
    private static String debt_payment = "CREATE TABLE IF NOT EXISTS debt_payment(id INTEGER PRIMARY KEY AUTOINCREMENT, debt_transaction_id INTEGER NOT NULL, total_debt DECIMAL(19,2) NOT NULL, amount_paid DECIMAL(19,2) NOT NULL, balance DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, due_date DATETIME NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String debt_transaction = "CREATE TABLE IF NOT EXISTS debt_transaction(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, debtor_id INTEGER NOT NULL, transaction_table TEXT NOT NULL, transaction_id INTEGER UNIQUE NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";

    private static String debtor = "CREATE TABLE IF NOT EXISTS debtor(id INTEGER PRIMARY KEY AUTOINCREMENT, client_id INTEGER NOT NULL, debt DECIMAL(19,2) NOT NULL, last_due_date DATETIME NOT NULL, currency VARCHAR(4) NOT NULL, archived INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String expense_purpose = "CREATE TABLE IF NOT EXISTS expense_purpose(id INTEGER PRIMARY KEY AUTOINCREMENT, purpose VARCHAR(100) NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String expense = "CREATE TABLE IF NOT EXISTS expense(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT DEFAULT NULL, client_id INTEGER default 0, purpose VARCHAR(200) NOT NULL, amount DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, archived TINYINT NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String income = "CREATE TABLE IF NOT EXISTS income(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT DEFAULT NULL, client_id INTEGER default 0, purpose VARCHAR(200) NOT NULL, amount DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, archived TINYINT NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String income_purpose = "CREATE TABLE IF NOT EXISTS income_purpose(id INTEGER PRIMARY KEY AUTOINCREMENT, purpose VARCHAR(20), created DATETIME, last_edited DATETIME, user_id INTEGER)";
    private static String initial_quantity = "CREATE TABLE IF NOT EXISTS initial_quantity(id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER NOT NULL, quantity DOUBLE NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String item = "CREATE TABLE IF NOT EXISTS item(id INTEGER PRIMARY KEY AUTOINCREMENT, category_id INTEGER NOT NULL, item VARCHAR(200) NOT NULL COLLATE NOCASE, short_form VARCHAR(25) DEFAULT NULL, divisible INTEGER DEFAULT 1, archived TINYINT NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL, barcode VARCHAR(100) DEFAULT NULL)";
    private static String last_used_date_time ="CREATE TABLE IF NOT EXISTS last_used_date_time(date_time DATETIME, created DATETIME)";
    //private static String name = "CREATE TABLE IF NOT EXISTS name(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(100), created DATETIME, last_edited DATETIME, user_id INTEGER)";
    private static String notes = "CREATE TABLE IF NOT EXISTS notes(id INTEGER PRIMARY KEY AUTOINCREMENT, notes VARCHAR(300) NOT NULL, table_name VARCHAR(30) DEFAULT NULL, table_id INTEGER DEFAULT NULL, reminder_date DATETIME DEFAULT NULL, reminder_frequency INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String notifications = "CREATE TABLE IF NOT EXISTS notifications(id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(300) NOT NULL, short_description VARCHAR(50) DEFAULT NULL, long_description VARCHAR(300) DEFAULT NULL, table_name TEXT DEFAULT NULL, table_id INTEGER DEFAULT NULL, link VARCHAR(70) DEFAULT NULL, created DATETIME NOT NULL , last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String pending_user = "CREATE TABLE IF NOT EXISTS pending_user(id INTEGER PRIMARY KEY AUTOINCREMENT, surname VARCHAR(30), first_name VARCHAR(30), other_name VARCHAR(30), username VARCHAR(25), password VARCHAR(60), secret_question VARCHAR(60), secret_answer VARCHAR(60), gender VARCHAR(10), photo BLOB, phone_number VARCHAR(20), alternate_phone_number VARCHAR(20), email_address VARCHAR(100), active INTEGER, created DATETIME, last_edited DATETIME)";
    private static String purchase_item = "CREATE TABLE IF NOT EXISTS purchase_item(id INTEGER PRIMARY KEY AUTOINCREMENT, purchase_transaction_id INTEGER NOT NULL, item_id INTEGER NOT NULL, UNIT_ID INTEGER NOT NULL, unit_price DECIMAL(19,2) NOT NULL, quantity DOUBLE NOT NULL, _quantity DOUBLE NOT NULL, cost DOUBLE NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String purchase_transaction ="CREATE TABLE IF NOT EXISTS purchase_transaction(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(50) NOT NULL, client_id INTEGER DEFAULT NULL, suspended INTEGER NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    //private static String purchase = "CREATE TABLE IF NOT EXISTS purchase(id INTEGER PRIMARY KEY AUTOINCREMENT, transact_id INTEGER, name_id INTEGER, category_id INTEGER, item_id INTEGER, unit_price DECIMAL(19,2), quantity DOUBLE, unit_id INTEGER, total_cost DECIMAL(19,2), amount_paid DECIMAL(19,2), balance DECIMAL(19,2), currency VARCHAR(4), archived TINYINT, notes_id INTEGER, created VARCHAR(40), last_edited VARCHAR(40), user_id INTEGER)";
    //private static String sales = "CREATE TABLE IF NOT EXISTS sales(id INTEGER PRIMARY KEY AUTOINCREMENT, transact_id INTEGER, name_id INTEGER, category_id INTEGER, item_id INTEGER, unit_price DECIMAL(19,2), quantity DOUBLE, unit_id INTEGER, total_cost DECIMAL(19,2), amount_paid DECIMAL(19,2), balance DECIMAL(19,2), currency VARCHAR(4), archived TINYINT, notes_id INTEGER, created VARCHAR(40), last_edited VARCHAR(40), user_id INTEGER)";
    private static String sale_item = "CREATE TABLE IF NOT EXISTS sale_item(id INTEGER PRIMARY KEY AUTOINCREMENT, sale_transaction_id INTEGER NOT NULL, item_id INTEGER NOT NULL, unit_id INTEGER NOT NULL, unit_price DECIMAL(19,2) NOT NULL, quantity DOUBLE NOT NULL, _quantity DOUBLE NOT NULL, cost DOUBLE NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String sale_transaction ="CREATE TABLE IF NOT EXISTS sale_transaction(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(50) NOT NULL, amount_paid DOUBLE NOT NULL, client_id INTEGER DEFAULT NULL, suspended INTEGER NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String special_quantity ="CREATE TABLE IF NOT EXISTS special_quantity(id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER NOT NULL, formula VARCHAR(200) NOT NULL, notes_id INTEGER, archived TINYINT NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String unit = "CREATE TABLE IF NOT EXISTS unit(id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER NOT NULL, unit VARCHAR(100) NOT NULL, short_form VARCHAR(10) NOT NULL, base_unit_equivalent DOUBLE NOT NULL, cost_price DECIMAL(19,2) NOT NULL DEFAULT 0.00, retail_price DECIMAL(19,2) NOT NULL DEFAULT 0.00, is_default INTEGER NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT 0, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String unit_relation = "CREATE TABLE IF NOT EXISTS unit_relation(id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER NOT NULL, old_unit_quantity DOUBLE NOT NULL, old_unit_id INTEGER NOT NULL, new_unit_quantity DOUBLE NOT NULL, new_unit_id INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String suspend = "CREATE TABLE IF NOT EXISTS sale_item(id INTEGER PRIMARY KEY AUTOINCREMENT, sale_transaction_id INTEGER NOT NULL, item_id INTEGER NOT NULL, unit_id INTEGER NOT NULL, unit_price DECIMAL(19,2) NOT NULL, quantity DOUBLE NOT NULL, _quantity DOUBLE NOT NULL, cost DOUBLE NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String vendor = "CREATE TABLE IF NOT EXISTS vendor(id INTEGER PRIMARY KEY AUTOINCREMENT, client_id INTEGER UNIQUE NOT NULL, notes_id INTEGER DEFAULT NULL,archived INTEGER DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String settings ="CREATE TABLE IF NOT EXISTS settings(comp_nm TEXT, val TEXT)";
    //added in verion 2 of the db
    private static String queries ="CREATE TABLE IF NOT EXISTS queries(id INTEGER PRIMARY KEY AUTOINCREMENT, queryString TEXT, sent_to_server TINYINT, last_attempted_ts DATETIME)";

    //create indexes added in version 2 of the db
    private static String curr_quantity_index = "CREATE INDEX IF NOT EXISTS curr_qty_index ON current_quantity(item_id,created, last_edited)";
    private static String initial_qty_index ="CREATE INDEX IF NOT EXISTS init_qty_index ON initial_quantity(item_id,created,last_edited)";
    private static String item_index = "CREATE INDEX IF NOT EXISTS item_index on item (category_id)";
    private static String purchase_item_index ="CREATE INDEX IF NOT EXISTS purchase_item_index ON purchase_item(purchase_transaction_id,created, last_edited)";
    private static String purchase_trans_index ="CREATE INDEX IF NOT EXISTS purchase_trans_index ON purchase_transaction (created, last_edited)";
    private static String sale_item_index = "CREATE INDEX IF NOT EXISTS sale_item_index ON sale_item(sale_transaction_id,item_id,created,last_edited)";
    private static String sale_trans_index = "CREATE INDEX IF NOT EXISTS sale_trans_index ON sale_transaction(name,created,last_edited)";
    private static String unit_index = "CREATE INDEX IF NOT EXISTS unit_index ON unit(item_id, created, last_edited)";
    private static String queries_index = "CREATE INDEX IF NOT EXISTS queries_index ON queries(last_attempted_ts)";

    //purchase transaction modified in version 3 of database
    private static String alter_purchase_transaction_amount_pd = "ALTER TABLE purchase_transaction ADD COLUMN  amount_paid DOUBLE NOT NULL DEFAULT 0";
    private static String getAlter_purchase_transaction_ded_frm_rev ="ALTER TABLE purchase_transaction ADD COLUMN ded_frm_rev TINYINT DEFAULT 0";

    //SALE_TRANSACTION TABLE MODIFIED IN VERSION 4 OF DATABASE
    private static String alter_sale_transaction_total = "ALTER TABLE sale_transaction ADD COLUMN total DOUBLE NOT NULL DEFAULT 0";
    private static String alter_purchase_transaction_total = "ALTER TABLE purchase_transaction ADD COLUMN total DOUBLE NOT NULL DEFAULT 0";

    //drop indexes
    private static String drop_curr_qty_index = "DROP INDEX IF EXISTS curr_qty";
    private static String drop_init_qty_index = "DROP INDEX IF EXISTS init_qty";

    private static final String TAG = "DatabaseManager";



    //private static int user_id;
    private static final int DB_VERSION = 4;
    /**
     * LEFT OUT user_privileges and vendor tables.
     */
    public static final int INSERT = 1, UPDATE = 2, DELETE =3;
    private static String businessInfo[];
    public DatabaseManager(Context context)
    {
        super(context, "Record Rack", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(business_details);
        db.execSQL(user);
        db.execSQL(category);
        db.execSQL(client);
        db.execSQL(creditor);
        db.execSQL(credit_payment);
        db.execSQL(credit_transaction);
        db.execSQL(current_quantity);
        db.execSQL(customer);
        db.execSQL(db_info);
        db.execSQL(debtor);
        db.execSQL(debt_payment);
        db.execSQL(debt_transaction);
        db.execSQL(expense_purpose);
        db.execSQL(expense);
        db.execSQL(income);
        db.execSQL(income_purpose);
        db.execSQL(initial_quantity);
        db.execSQL(item);
        db.execSQL(last_used_date_time);
        //db.execSQL(name);
        db.execSQL(notes);
        db.execSQL(notifications);
        db.execSQL(pending_user);
        //db.execSQL(purchase);
        db.execSQL(purchase_item);
        db.execSQL(purchase_transaction);
        db.execSQL(sale_item);
        db.execSQL(sale_transaction);
        //db.execSQL(sales);
        db.execSQL(suspend);
        db.execSQL(special_quantity);
        db.execSQL(unit);
        db.execSQL(unit_relation);
        db.execSQL(vendor);

        updatesForDBVersion2(db);
        updatesForDBVersion3(db);
        updatesForDBVersion4(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        updatesForDBVersion4(db);

        /*if(oldVersion < 2)
        {
            //updatesForDBVersion2(db);
        }
        if(oldVersion < 3)
        {
            updatesForDBVersion3(db);
        }
        if(oldVersion < 4)
            updatesForDBVersion4(db);
        if(oldVersion == DB_VERSION)
        {
            updatesForDBVersion2(db);
            updatesForDBVersion3(db);
        }*/
    }

    private void updatesForDBVersion2(SQLiteDatabase db)
    {
        //updates in db version 2
        db.execSQL(queries);
        db.execSQL(curr_quantity_index);
        db.execSQL(initial_qty_index);
        db.execSQL(item_index);
        db.execSQL(purchase_item_index);
        db.execSQL(purchase_trans_index);
        db.execSQL(sale_item_index);
        db.execSQL(sale_trans_index);
        db.execSQL(unit_index);
        db.execSQL(queries_index);
        db.execSQL(settings);
        //db.execSQL(drop_curr_qty_index);
        //db.execSQL(drop_init_qty_index);
    }

    private void updatesForDBVersion3(SQLiteDatabase db)
    {
        db.execSQL(alter_purchase_transaction_amount_pd);
        db.execSQL(getAlter_purchase_transaction_ded_frm_rev);
    }

    private void updatesForDBVersion4(SQLiteDatabase db)
    {
        db.execSQL(alter_sale_transaction_total);
        db.execSQL(alter_purchase_transaction_total);
    }


    /**
     * private method that closes a database connection
     * @param database the connection to close
     */
    private static synchronized void closeDatabase(SQLiteDatabase database)
    {
        --dbCount;
        database.close();
    }

    /**
     * private method to get reference to a database connection
     * @return database connection
     */
    private static synchronized SQLiteDatabase openDatabase()
    {
        SQLiteDatabase database = null;
        try
        {
            String name = Thread.currentThread().getName();

            if(name.equalsIgnoreCase("DBThread"))
            {

                if(dbManager == null)
                {
                    //UtilityClass.showToast("Database not initialized");
                    Log.d("Database initialization","Database not initialized");
                    return null;
                }
                if(++dbCount == 1)
                {
                    database = dbManager.getWritableDatabase();

                }
                Log.d(TAG,"DB path is "+database.getPath());
                Logger.log(TAG,"DB path is "+database.getPath());
            }
            else
            {
                ThreadException exception = new ThreadException();
                exception.setErrorMessage("Calling database method from non database thread");
                throw exception;
            }
        }
        catch (ThreadException e)
        {
            e.printStackTrace();
        }
        return database;
    }

    /**
     * first method called when trying to interact with database
     */
    private static synchronized void initializeDatabase()
    {
        if(dbManager == null)
            dbManager = new DatabaseManager(UtilityClass.getContext());
    }

    /**
     * convenience method that inserts data into the database
     * @param details this jsonobject contains the data to be inserted into the database
     *                as well as essential information that helps in the storing process.
     * @return a positive integer value. -1 if an error occurred
     */
    public static long insertDataIntoDatabase(JSONObject details)
    {
        long result = -1;
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        ContentValues contentValues = new ContentValues();
        try
        {
            int count = 0;
            JSONArray entries = details.getJSONArray("entries");
            String tableName = details.getString("tableName");
            String names[] = null;
            if(tableName.equals("business_details"))
                names = getBusinessDetailsColumnNames();
            else if(tableName.equals("user"))
                names = getUserColumnNames();
            else
            {
                //UtilityClass.showToast("incorrect table name "+ tableName);
                Log.d("Debug RR","incorrect table name "+ tableName);
                return result;
            }

            while(count < entries.length())
            {
                JSONObject jsonObject = entries.getJSONObject(count);
                contentValues.put(names[count],jsonObject.getString(names[count]));

                jsonObject = null;
                count++;
            }
            result = database.insertOrThrow(tableName,null,contentValues);
            entries = null;
            contentValues = null;
            names = null;
            closeDatabase(database);
        }
        catch (JSONException exception)
        {
            exception.printStackTrace();
        }
        return result;
    }

    /**
     * method called to verify username/password credentials
     * @param username username entered
     * @param password password
     * @return a boolean indicating correctness of credentials
     */
    public static boolean verifyLoginDetails(String username, String password)
    {
        boolean result = false;
        initializeDatabase();
        if(!doesUsernameExist(username))
            return result;
        else
        {
            String pass = getPassword(username);
            if (password.equals(pass))
                result = true;
            else
                result = false;
        }
        //populateInitialQuantity();
        return result;
    }

    /**
     * convenience method to populate the initial quantity table when the item id, unit id and
     * quantity are known. This is basically used when a new item is added after initial quantity table
     * has been populated
     * @param itemID item id of the item to be added
     * @param unitID unit id of the item to be inserted
     * @param quantity the quantity to set as the current quantity.
     */
    public static void populateInitialQuantity(long itemID,long unitID, String quantity)
    {
        try
        {
            JSONObject object = new JSONObject();
            object = new JSONObject();
            object.put("tableName","initial_quantity");
            object.put("item_id",itemID);
            object.put("quantity",quantity);
            object.put("created",UtilityClass.getDateTime());
            object.put("last_edited",UtilityClass.getDateTime());
            //object.put("unit_id",unitID);
            object.put("user_id",UtilityClass.getCurrentUserID());
            insertData(object);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * private method to populate the initial quantity table. This method copies the values from the
     * current quantity table into the initial quantity. It is called once the user logs in
     */
    public static void populateInitialQuantity()
    {
        //first, check to see if initial quantity table has been updated for today
        try
        {
            JSONObject object = new JSONObject(),loopItem;
            JSONArray items = null, unit = null, quantity = null;
            String [] columns = {"quantity"};
            String whereArgs = "last_edited >='"+UtilityClass.getDateTime(0,0,0,0)+"'";
            object.put("tableName","initial_quantity");
            object.put("columns",columns);
            object.put("whereArgs",whereArgs);
            JSONArray array = fetchData(object);
            if(array.length() == 0) //initial quantity table hasn't been updated
            {
                //get all items
                object = null;
                columns = null;
                columns = new String[]{"id"};
                object = new JSONObject();
                object.put("tableName","item");
                object.put("columns",columns);
                items = fetchData(object);
                object = null;
                for (int count = 0; count<items.length(); count++)
                {
                    //get unit_id for each item
                    loopItem = items.getJSONObject(count);
                    /*columns = null;
                    columns = new String[]{"id"};
                    object = new JSONObject();
                    object.put("tableName","unit");
                    object.put("columns",columns);
                    object.put("whereArgs"," item_id='"+loopItem.getString("id")+"'");
                    unit = fetchData(object);*/

                    //get current quantity
                    object = null;
                    columns = null;
                    columns = new String[]{"quantity"};
                    object = new JSONObject();
                    object.put("tableName","current_quantity");
                    object.put("columns",columns);
                    object.put("whereArgs"," item_id='"+loopItem.getString("id")+"'");
                    quantity = fetchData(object);

                    //insert data into intial_quantity
                    object = null;
                    object = new JSONObject();
                    object.put("tableName","initial_quantity");
                    object.put("item_id",loopItem.getString("id"));
                    object.put("quantity",quantity.getJSONObject(0).getString("quantity"));
                    object.put("created",UtilityClass.getDateTime());
                    object.put("last_edited",UtilityClass.getDateTime());
                    //object.put("unit_id",unit.getJSONObject(0).getString("id"));
                    object.put("user_id",UtilityClass.getCurrentUserID());
                    insertData(object);
                }
            }
            /*else
            {
                JSONObject object1 = new JSONObject();
                object1.put("tableName","initial_quantity");
                object1.put("whereArgs","created >='"+UtilityClass.getDateTime(0,0,0,0)+"'");
                deleteData(object1);
                System.out.println("data deleted");
            }*/

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * method to check if the username specified is already in the database
     * @param username the username to look up
     * @return true if found, else false
     */
    public static boolean doesUsernameExist(String username)
    {
        boolean result = false;
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        String query = "SELECT id,username FROM user where username = '"+username+"'";
        Cursor cursor = database.rawQuery(query,null);
        if(cursor != null && cursor.moveToFirst())
        {
            UtilityClass.setCurrentUserName(username);
            UtilityClass.setCurrentUserID(cursor.getInt(0));
            //user_id = cursor.getInt(0);
            result = true;
        }
        else
            result= false;
        cursor.close();
        closeDatabase(database);
        return result;
    }

    /**
     * method to retrieve password from the database
     * @param username the username to look up
     * @return true if found, else false
     */
    private static String getPassword(String username)
    {
        String password  = null;
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        String query = "SELECT password FROM user where username = '"+username+"'";
        Cursor cursor = database.rawQuery(query,null);
        if(cursor.moveToFirst())
        {
            password = cursor.getString(0);
        }
        else
        {
            Log.d("Debug RR","Could not advance to cursor object");
            //UtilityClass.showToast("Could not advance to cursor object");
        }

        cursor.close();
        closeDatabase(database);
        return password;
    }

    /**
     * this method is called to retrieve and return the business info, usually for printing purposes
     * The info returned are the business name, address and phone number
     * @return
     */
    public static String[] getBusinessDetails()
    {
        if(businessInfo == null)
        {
            JSONArray result = null;
            try
            {
                JSONObject object = new JSONObject();
                object.put("tableName","business_details");
                object.put("columns",new String[]{"name","address","phone_number"});
                result = fetchData(object);
                businessInfo = new String[3];
                businessInfo[0] = result.getJSONObject(0).getString("name");
                businessInfo[1] = result.getJSONObject(0).getString("address");
                businessInfo[2] = result.getJSONObject(0).getString("phone_number");
                object = null;

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        return businessInfo;
    }

    /**
     * method that returns the column names for business details table
     * @return column names in a string array
     */
    public static String [] getBusinessDetailsColumnNames()
    {
        String [] names = new String[5];
        names[0] = "business_name";
        names[1] = "business_address";
        names[2] = "business_type";
        names[3] = "establishment_year";
        names[4] = "phone_number";
        return names;
    }

    /**
     * method that returns the column names for user table
     * @return column names in a string array
     */
    public static String [] getUserColumnNames()
    {
        String [] names = new String[5];
        names[0] = "name";
        names[1] = "username";
        names[2] = "password";
        names[3] = "secret_question";
        names[4] = "answer";
        return names;
    }



    /**
     * Insert the quantity into the current quantity table for the given item_id
     * @param itemID the item_id of the item to be inserted
     * @param quantity the quantity to be inserted into the current quantity table
     */
    public static void insertCurrentQuantity(int itemID, double quantity)
    {
        /**
         * First check to see if the itemID exists in the database, if it does we'd update instead
         * If it doesn't we'd insert
         */

        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        String stmt = "SELECT id FROM current_quantity WHERE item_id="+itemID+"";
        Cursor cursor = database.rawQuery(stmt, null);
        if(cursor != null && cursor.moveToFirst())//we have an entry for this itemID
        {
            stmt = null;
            stmt = "UPDATE current_quantity SET quantity="+quantity+" WHERE item_id="+itemID+"";
            database.execSQL(stmt);
        }
        else
        {
            stmt = null;
            stmt = "INSERT INTO current_quantity(item_id, quantity) VALUES("+itemID+", "+quantity+")";
            database.execSQL(stmt);
        }

        closeDatabase(database);
        database = null;
        stmt = null;
        cursor.close();
    }

    public static void insertUnit(int item_id,String unit)
    {

    }

    /**
     * this method handles all insertions into the database
     * @param data the JSON encoded data
     * @return the row number as a long.
     */
    public static long insertData(JSONObject data) throws JSONException
    {
        //String [] columnArgs = null;
        //System.out.println("Method insertData "+data);
        int length = 0;
        long id = -1;
        String tableName = data.getString("tableName");
        String key=null,value=null;
        JSONObject data1 = new JSONObject(data.toString());
        data.remove("tableName");
        JSONArray keys = data.names();
        data.put("tableName", tableName);
        if(tableName.equals("name"))
        {
            Logger.log(TAG,"data "+data+" keys "+keys);
            Log.d(TAG,"data "+data+" keys "+keys);
        }
        //this checks to see if the category or item or unit already exists
        switch (tableName)
        {
            case "category":
            {
                JSONObject object = new JSONObject();
                String [] columnArgs = {"category","id"};
                object.put("tableName",tableName);
                object.put("columnArgs",columnArgs);
                object.put("whereArgs","category = '"+data.getString("category")+"'");
                JSONArray category = fetchData(object);
                //if the category exists set the first tuple to true, else set it to false
                if(category.length() >0 && category.getJSONObject(0).length()>0)
                {
                    //I am assuming a single jsonobject is contained in the array
                    //if the category exists set the first tuple to true, else set it to falsetuples.setValues(true,Long.parseLong(category.getJSONObject(0).getString("id")));
                    return Long.parseLong(category.getJSONObject(0).getString("id"));
                }
                else
                    break;
            }
            case"item":
            {
                JSONObject object = new JSONObject();
                String [] columnArgs = {"id"};
                object.put("tableName",tableName);
                object.put("columnArgs",columnArgs);
                //add collate nocase here
                object.put("whereArgs","category_id = '"+data.getString("category_id")+"' AND item='"+data.getString("item")+"' AND ARCHIVED = 0");
                JSONArray item = fetchData(object);
                if(item.length() >0 && item.getJSONObject(0).length()>0)
                {
                    //If item is already in the database, return -1
                    return -1; //Long.parseLong(item.getJSONObject(0).getString("id"));
                }
                else
                    break;
            }
            case "unit":
            {
                JSONObject object = new JSONObject();
                String [] columnArgs = {"id"};
                object.put("tableName",tableName);
                object.put("columnArgs",columnArgs);
                object.put("whereArgs","item_id = '"+data.getString("item_id")+"' AND "+"unit='"+data.getString("unit")+"'");
                JSONArray unit = fetchData(object);
                if(unit.length() >0 && unit.getJSONObject(0).length()>0)
                {
                    //I am assuming a single jsonobject is contained in the array
                    return Long.parseLong(unit.getJSONObject(0).getString("id"));
                }
                else
                    break;
            }
            case "current_quantity":
            {
                JSONObject object = new JSONObject();
                String [] columnArgs = {"id"};
                object.put("tableName",tableName);
                object.put("columnArgs",columnArgs);
                object.put("whereArgs","item_id = '"+data.getString("item_id")+"'"); // AND "+"unit_id='"+data.getString("unit_id")+"'");
                JSONArray quantity = fetchData(object);
                if(quantity.length() > 0)
                {
                    //I am assuming a single jsonobject is contained in the array
                    //if the entry exists in the current_quentity table, it should return -1
                    //else it should be inserted into the database
                    return -1;
                }
                else
                    break;
            }
        }
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        ContentValues cv = new ContentValues();
        while(length < keys.length())
        {
            key = keys.getString(length);
            value = data.getString(keys.getString(length));
            if(value.length()>0)
                cv.put(key,value);
            length++;
        }
        //System.out.println(data);
        Set<String> ks = cv.keySet();
        Log.d(TAG,"About inserting data into "+tableName);
        Logger.log(TAG,"About inserting data into "+tableName);
        for(String s:ks)
            Log.d(TAG,"key "+s+":value "+cv.getAsString(s));
        id = database.insertOrThrow(tableName,null, cv);
        Log.d(TAG,"ID returned is "+id);
        Logger.log(TAG,"ID returned is "+id);
        //System.out.println("out "+id);

        closeDatabase(database);
        keys = null;
        cv = null;

        //attempt to send data to server except if data is being inserted into queries table
        //put the action type as insert
        if(!tableName.equals("queries"))
        {
            data1.put("action","insert");
            data1.put("id",id);
            uploadDataToServer(data1);
        }
        return id;
    }



    private static void uploadDataToServer(final JSONObject data)
    {
        NetworkThread.getNetworkThread().postNetworkTask(new Runnable()
        {
            @Override
            public void run()
            {
                DataUploadClass.uploadToServer(data);
            }
        });
    }

    /**
     * this method updates data in the database
     * @param data the data to be updated. The data includes the table name and column values
     * @param whereClause the where statement that determines which row gets updated. This excludes
     *                    the where keyword.
     * @return a boolean true if one or more tables were updated, false if nothing was updated
     * @throws JSONException
     */
    public static boolean updateData(JSONObject data,String whereClause) throws JSONException
    {
        Logger.log(TAG,"In the update data method");
        Log.d(TAG,"In the update data method");
        //System.out.println("json object "+data);
        String tableName = data.getString("tableName");
        data.remove("tableName");
        JSONArray keys = data.names();
        //System.out.println("json object "+data);
        //System.out.println("keys " + keys);
        int length = 0;
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        ContentValues cv = new ContentValues();

        while(length < keys.length())
        {
            Logger.log(TAG,"key "+keys.getString(length)+" value "+data.getString(keys.getString(length)));
            Log.d(TAG,"key "+keys.getString(length)+" value "+data.getString(keys.getString(length)));
            cv.put(keys.getString(length),data.getString(keys.getString(length)));
            length++;
        }

        boolean response = database.update(tableName,cv,whereClause,null) >0;

        closeDatabase(database);
        keys = null;
        cv = null;
        data.put("tableName",tableName);
        data.put("whereArgs",whereClause);
        //attempt to send data to server except if data is being inserted into queries table
        //put the action type as insert
        if(!tableName.equals("queries"))
        {
            data.put("action","update");
            uploadDataToServer(data);
        }
        return response;
    }

    /**
     * this method performs a query and returns the response as a JSON string
     * @param data data contains the following key/value pairs
     *             tableName: the name of the table to select from. Multiple tables can be specified
     *             by separating them by commas
     *             columnArgs: a string array containing columns to select from. NB, if this key is
     *             ommitted, it'd be replaced by * i.e all columns are selected
     *             whereArgs: the where condition, without the WHERE statement. If no whereArgs is
     *             specified, the WHERE clause is excluded
     *             join: the join statement. this can be created by using the static method
     *             getJoinQuery, available in the UtilityClass
     *             extra: this basically contains statements such as group by, order by etc
     * @return the result of the query as a json object
     * @throws JSONException
     */
    public static JSONArray fetchData(JSONObject data) throws JSONException
    {
        Log.d(TAG,"In the fetch data method");
        Logger.log(TAG,"In the fetch data method");
        JSONArray response = new JSONArray();
        JSONObject object = null;
        String[] columnArgs = data.has("columnArgs")?(String [])data.get("columnArgs"):new String[]{"*"};
        //for (String val : columnArgs)
           // System.out.println(val);
        String whereArgs = data.has("whereArgs")?data.getString("whereArgs"):"";
        String join = data.has("join")?data.getString("join"):"";
        String extra = data.has("extra")?data.getString("extra"):"";
        //JSONObject whereArgs = data.getJSONObject("whereArgs");
        //JSONArray keys = whereArgs.names();
        String tableName = data.getString("tableName");
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        int count = 0;
        String stmt = "SELECT ";//id FROM current_quantity WHERE item_id="+itemID+"";
        while(count < columnArgs.length)
        {
            stmt+= columnArgs[count];
            count++;
            if(count < columnArgs.length)
                stmt+=", ";
        }

        if(whereArgs.length() > 0)
            stmt+=" FROM "+ tableName+" "+join+" WHERE "+whereArgs+" "+extra;
        else
            stmt+=" FROM "+ tableName +" "+join+" "+extra;


        Log.d(TAG,"lol here "+stmt +" db null? "+Boolean.toString(database==null));
        Logger.log(TAG,"lol here "+stmt +" db null? "+Boolean.toString(database==null));
        //DataUploadClass.uploadToServer(data);
        Cursor cursor = database.rawQuery(stmt, null);

        while(cursor != null && cursor.moveToNext())
        {
            object = new JSONObject();
            columnArgs = cursor.getColumnNames();
            count = 0;
            if(columnArgs.length==1 && columnArgs[0].equals("*"))
            {
                while(count < cursor.getColumnCount())
                {
                    object.put(cursor.getColumnName(count), cursor.getString(cursor.getColumnIndex(cursor.getColumnName(count))));
                    //System.out.println(cursor.getString(cursor.getColumnIndex(columnArgs[count])));
                    count++;
                }
            }
            else
            {
                while(count < columnArgs.length)
                {
                    object.put(columnArgs[count], cursor.getString(cursor.getColumnIndex(columnArgs[count])));
                    //System.out.println(cursor.getString(cursor.getColumnIndex(columnArgs[count])));
                    count++;
                }
            }
            response.put(object);
            object = null;
        }
        Log.d(TAG,"Result of query: "+response);
        Logger.log(TAG,"Result of query: "+response);

        cursor.close();
        closeDatabase(database);
        database = null;
        columnArgs = null;
        whereArgs = null;
        tableName = null;
        return response;
    }

    /**
     * method to delete entries from the database. The JSONObject contains the table to delete from
     * and the whereArgs that constraints the rows to be deleted
     * @param data
     * @throws JSONException
     */
    public static void deleteData(JSONObject data) throws JSONException
    {
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        String tableName = data.getString("tableName");
        String whereArgs = data.getString("whereArgs");
        String query = "DELETE FROM "+ data.getString("tableName")+ (whereArgs.length()==0?"":" WHERE "+whereArgs);
        database.execSQL(query);
        if(!tableName.equals("queries"))
        {
            data.put("action","delete");
            uploadDataToServer(data);
        }
        closeDatabase(database);
    }

    /**
     * this method deletes all entries from a given database
     * @param d table whose data is to be deleted
     */
    public static void deleteData(String d)
    {
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        String query = "DELETE FROM "+ d;
        database.execSQL(query);
        if(!d.equals("queries"))
        {
            try
            {
                JSONObject data = new JSONObject();
                data.put("tableName",d);
                //data.put("dbName",UtilityClass.getRackID());
                data.put("action","delete");
                uploadDataToServer(data);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
        closeDatabase(database);
    }

    /**
     * convenience method that retrieves and returns the current quantity from the database
     * @return the current quantity in base unit
     */
    //public static double retrieveCurrentQuantity(Long itemID, Long unitID)
    public static double retrieveCurrentQuantity(Long itemID)
    {
        Log.d(TAG,"In the retrieve current quantity method");
        Logger.log(TAG,"In the retrieve current quantity method");
        double quantity = -1;
        try
        {
            JSONObject object = new JSONObject();
            String columnArgs[] = {"quantity"};
            String whereArgs = null;
            whereArgs = " item_id = '"+itemID+"'";
            /*if(unitID != -1)
                whereArgs = " item_id = '"+itemID+"'";
                //whereArgs = " item_id = '"+itemID+"' AND unit_id = '"+unitID+"'";
            else
                whereArgs = " item_id = '"+itemID+"'";*/
            object.put("columnArgs", columnArgs);
            object.put("tableName", "current_quantity");
            object.put("whereArgs", whereArgs);
            JSONArray result = DatabaseManager.fetchData(object);
            quantity = result.getJSONObject(0).getDouble("quantity");
            Log.d(TAG,"Result for quantity: "+quantity);
            Logger.log(TAG,"Result for quantity: "+quantity);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return quantity;
    }


    /**
     * this method retrieves the last transact id from the a table. The table is passed as an arg
     * @param table the table to obtain the last id
     * @return the last transact id is returned as a long
     */
    public static long getLastTransactID(String table)
    {
        long lastID = -1;
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        Cursor cursor = database.rawQuery("SELECT MAX(transact_id) AS max FROM "+table+"", null);
        if(cursor != null && cursor.moveToFirst())
            lastID = cursor.getInt(0);

        closeDatabase(database);

        return lastID;
    }

    /**
     * this method sums up a set of rows and returns the sum as a double. A simple where clause
     * is used to constrain the search and summation
     * @param tableName the name of the table whose row would be summed. In the case of a JOIN,
     *                  multiple tables are specified by separating them with commas.
     * @param columnName the name of the column to be summed
     * @param whereClause where clause that limits how many rows that would be summed. this excludes
     *                    the where keyword.
     * @return the sum as a double. -1 is returned to indicate an error.
     */
    public static double sumUpRowsWithWhere(String tableName, String columnName,String whereClause)
    {
        Log.d(TAG,"Summing up with where clause");
        Logger.log(TAG,"Summing up with where clause");
        double sum = -1;
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        Log.d(TAG,"where clause:: "+whereClause);
        Logger.log(TAG,"where clause:: "+whereClause);
        String query="SELECT SUM(" + columnName + ") AS sum FROM " + tableName + " WHERE " + whereClause;
        Logger.log(TAG,query);
        Log.d(TAG,query);
        Cursor cursor = database.rawQuery(query, null);
        //Cursor cursor = database.rawQuery(query,null);
        if (cursor != null && cursor.moveToFirst())
            sum = cursor.getDouble(cursor.getColumnIndex("sum"));

        closeDatabase(database);
        return sum;
    }

    /**
     * this method sums up a set of rows and returns the sum as a double. The JOIN statement is
     * formatted by calling UtilityClass.getJoinQuery.
     * @param tableName the name of the table whose row would be summed. In the case of a JOIN,
     *                  multiple tables are specified by separating them with commas.
     * @param columnName the name of the column to be summed
     * @param join join statement that describes how tables should be interleaved and required column
     *             summed
     * @return the sum as a double. -1 is returned to indicate an error.
     */
    public static double sumUpRowsWithJoin(String tableName, String columnName,String join)
    {
        double sum = 0;
        initializeDatabase();
        SQLiteDatabase database = openDatabase();
        Log.d(TAG,"Sum up rows with join");
        Logger.log(TAG,"Sum up rows with join");
        Log.d(TAG,"SELECT SUM(" + columnName + ") AS sum FROM " + tableName  + join);
        Logger.log(TAG,"SELECT SUM(" + columnName + ") AS sum FROM " + tableName  + join);
        Cursor cursor = database.rawQuery("SELECT SUM(" + columnName + ") AS sum FROM " + tableName  + join, null);
        //Cursor cursor = database.rawQuery(query,null);
        if (cursor != null && cursor.moveToFirst())
            sum = cursor.getDouble(cursor.getColumnIndex("sum"));

        Log.d("This is sum for "+join,Double.toString(sum));
        Logger.log("This is sum for "+join,Double.toString(sum));

        closeDatabase(database);
        return sum;
    }


    /**
     * call this method to subtract a given quantity from the current quantity. This current
     * implementation doesn't take into consideration different units for a given item
     * @param itemID item id of the item to compute a new quantity for
     * @param quantity the quantity to subtract
     * @return the new quantity
     */
    public static double subtractQuantity(long itemID, double quantity)
    {
        Log.d(TAG,"Subtracting quantity::"+retrieveCurrentQuantity(itemID)+" subtract "+quantity);
        Logger.log(TAG,"Subtracting quantity::"+retrieveCurrentQuantity(itemID)+" subtract "+quantity);

        return (retrieveCurrentQuantity(itemID) - quantity);
        //return (retrieveCurrentQuantity(itemID,unitID) - quantity);
    }

    /**
     * call this method to add a given quantity to the current quantity. This current
     * implementation doesn't take into consideration different units for a given item
     * @param itemID item id of the item to compute a new quantity for
     * @param quantity the quantity to subtract
     * @return the new quantity
     */
    public static double addQuantity(long itemID, double quantity)
    {
        return (retrieveCurrentQuantity(itemID) + quantity);

        //return (retrieveCurrentQuantity(itemID,unitID) + quantity);
    }

    /**
     * this method first re-adds the quantities of the various items sold to the current quantity.
     * Then it moves to flag the transaction as archived
     * @param transactionID
     */
    public static void deleteSaleTransaction(String transactionID)
    {
        try
        {
            JSONObject object = new JSONObject();
            //retrieve all items that belong to this transaction
            object = new JSONObject();
            object.put("tableName","sale_item");
            object.put("whereArgs"," sale_transaction_id = '"+transactionID+"'");
            object.put("columnArgs",new String[]{"id","_quantity","item_id"});
            JSONArray array = fetchData(object);
            int loop = 0;
            for (;loop<array.length();loop++)
            {
                String item_id = array.getJSONObject(loop).getString("item_id");
                String quantity = array.getJSONObject(loop).getString("_quantity");
                String saleItemID=array.getJSONObject(loop).getString("id");
                //add the quantity back to what we currently have in stock
                updateCurrentQuantity(item_id,quantity,0);

                //flag the sale item as archived
                object = null;
                object = new JSONObject();
                object.put("tableName","sale_item");
                object.put("archived","1");
                String where = "id = '"+saleItemID+"'";
                updateData(object,where);

                item_id = null;
                quantity = null;
                object = null;
                //unit_id = null;
            }

            //flag transaction as archived
            object = null;
            object = new JSONObject();
            object.put("tableName","sale_transaction");
            object.put("archived","1");
            String where = "id = '"+transactionID+"'";
            updateData(object,where);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * this method deletes purchase transactions
     * @param transactionID the purchase transaction id whose items are to be deleted.
     * @return returns an array containing items that couldn't be deleted because the quantity to be
     *          deleted is greater than the current quantity. This would be because from the purchased
     *          items have been sold.
     */
    public static JSONArray deletePurchaseTransaction(String transactionID)
    {
        Log.d(TAG,"Deleting purchase transaction");
        Logger.log(TAG,"Deleting purchase transaction");
        JSONArray result =  new JSONArray();
        JSONObject object = new JSONObject();
        try
        {
            object = new JSONObject();
            //retrieve all items that belong to this transaction
            object = new JSONObject();
            object.put("tableName","purchase_item");
            object.put("whereArgs"," purchase_transaction_id = '"+transactionID+"' AND archived=0");
            object.put("columnArgs",new String[]{"id","_quantity","item_id"});
            JSONArray array = fetchData(object);
            int loop = 0;
            for (;loop<array.length();loop++)
            {
                String item_id = array.getJSONObject(loop).getString("item_id");
                String quantity = array.getJSONObject(loop).getString("_quantity");
                String purchaseItemID=array.getJSONObject(loop).getString("id");
                double current_quantity = retrieveCurrentQuantity(Long.parseLong(item_id));
                if(Double.parseDouble(quantity) > current_quantity)
                {
                    Logger.log(TAG,"in here about to update");
                    Log.d(TAG,"in here about to update");
                    JSONObject resultItem = new JSONObject();
                    object.put("tableName","item");
                    object.put("columnArgs",new String[]{"item","category"});
                    object.put("join",UtilityClass.getJoinQuery("category","category_id=category.id"));
                    JSONArray res = fetchData(object);
                    resultItem.put("item",res.getJSONObject(0).getString("item"));
                    resultItem.put("category",res.getJSONObject(0).getString("category"));
                    resultItem.put("quantity_to_delete",StockFragmentLogic.breakDown(Double.parseDouble(quantity),item_id));
                    resultItem.put("current_quantity",StockFragmentLogic.breakDown(current_quantity,item_id));
                    result.put(resultItem);
                    continue;
                }

                //delete the quantity purchased
                updateCurrentQuantity(item_id,quantity,-1);

                //flag the purchased item as archived
                object = null;
                object = new JSONObject();
                object.put("tableName","purchase_item");
                object.put("archived","1");
                String where = "id = '"+purchaseItemID+"'";
                updateData(object,where);

                item_id = null;
                quantity = null;
                object = null;
                //unit_id = null;
            }
            //flag the item as archived
            object = null;
            object = new JSONObject();
            object.put("tableName","purchase_transaction");
            object.put("archived","1");
            String where = "id = '"+transactionID+"'";
            updateData(object,where);
            object = null;

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * call this method to modify the value of the current quantity of a given item
     * @param item_id id of the item being modified
     * @param quan quantity to add or subtract from the current quantity
     * @param ops operation to perform, either add (0) or subtract (-1)
     */
    public static void updateCurrentQuantity(String item_id, String quan,int ops)
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName", "current_quantity");
            object.put("item_id",item_id);
            //object.put("unit_id", unit_id);

            double quantity = Double.parseDouble(quan);
            long itemID = Long.parseLong(item_id);
            //long unitID = Long.parseLong(unit_id);
            //System.out.println("sum is "+addQuantity(itemID,unitID,quantity));
            if(ops == 0)
                object.put("quantity",addQuantity(itemID,quantity));
            else if(ops == -1)
                object.put("quantity",subtractQuantity(itemID,quantity));
            object.put("created",UtilityClass.getDateTime());
            object.put("last_edited",UtilityClass.getDateTime());
            object.put("user_id", UtilityClass.getCurrentUserID());

            long id = DatabaseManager.insertData(object);
            if(id == -1)
            {
                String where = "item_id = '"+item_id+"'";
                DatabaseManager.updateData(object, where);
            }
            object = null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * method to save a new debt transaction
     * @param clientID id of the client who's owing
     * @param amountDue amount originally to be paid
     * @param amountPaid amount actually paid
     * @param dueDate date when this debt is due
     * @param transactionTable what table was the transaction saved to e. sale, purchase etc
     * @param transactionID id from whatever transaction table the transaction is from
     */
    public static void saveDebtTransaction(long clientID, double amountDue, double amountPaid, String dueDate, String transactionTable, long transactionID)
    {
        try
        {
            //first, we check to see if the given client has an existing debt.
            //if we find an entry, we add it to the new debt and update, else we just write the new debt
            Log.d(TAG,"saving debt");
            double debt = amountDue - amountPaid;
            long debtorID = 0;
            JSONArray result = null;
            JSONObject object = new JSONObject();
            object.put("tableName","debtor");
            object.put("columnArgs",new String[]{"id","debt"});
            object.put("whereArgs","client_id ='"+clientID+"' AND archived='0'");
            result = fetchData(object);
            if(result.length() == 0)//no entry was found, insert the new debt
            {
                Log.d(TAG,"debt is:"+debt);
                Log.d(TAG,"client id is:"+clientID);
                object = null;
                object = new JSONObject();
                object.put("tableName","debtor");
                object.put("client_id",clientID);
                object.put("debt",debt);
                object.put("last_due_date",dueDate);
                object.put("currency","NGN");
                object.put("archived","0");
                object.put("created",UtilityClass.getDateTime());
                object.put("last_edited",UtilityClass.getDateTime());
                object.put("user_id",UtilityClass.getCurrentUserID());
                debtorID = DatabaseManager.insertData(object);
            }
            else //an entry was found, add the new debt to the old debt
            {
                Log.d(TAG,"An old debt was found");
                double oldDebt = result.getJSONObject(0).getDouble("debt");
                debtorID = result.getJSONObject(0).getLong("id");
                Log.d(TAG,"Old debt value:"+oldDebt);
                Log.d(TAG,"debt is this:"+debt);
                double newDebt = oldDebt + debt;
                Log.d(TAG,"new debt is:"+newDebt);

                object = null;
                object = new JSONObject();
                object.put("tableName","debtor");
                object.put("debt",newDebt);
                object.put("last_due_date",dueDate);
                object.put("archived","0");
                object.put("created",UtilityClass.getDateTime());
                object.put("last_edited",UtilityClass.getDateTime());
                object.put("user_id",UtilityClass.getCurrentUserID());
                DatabaseManager.updateData(object,"client_id = "+clientID+"");
            }

            //create an entry in debt_transaction
            object = null;
            object = new JSONObject();
            object.put("tableName","debt_transaction");
            object.put("debtor_id",debtorID);
            object.put("transaction_table",transactionTable);
            object.put("transaction_id",transactionID);
            object.put("created",UtilityClass.getDateTime());
            object.put("last_edited",UtilityClass.getDateTime());
            object.put("user_id",UtilityClass.getCurrentUserID());
            long debtTransactionID = DatabaseManager.insertData(object);

            //update the debt_payment table
            makeDebtPayment(debtorID,debtTransactionID,amountDue,amountPaid,dueDate,true);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * this method is called whenever payment for a debt has been made
     * @param debtTransactionID id for the debt transaction.
     * @param totalDebt total money owed or 0 if the balance from a previous payment is to be used
     * @param amountPaid amount the client is paying
     * @param dueDate date the debt is due.
     * @param isNewDebt boolean indicator for a new debt that's inserted or payment made for old debt.
     *                  True if this is a new debt, false if otherwise
     */
    public static void makeDebtPayment(long debtorID, long debtTransactionID , double totalDebt , double amountPaid, String dueDate, boolean isNewDebt)
    {
        try
        {
            Log.d(TAG,"Making debt payment");
            //finally, create an entry in the debt_payment table. first, we have to check for the
            //most recent payment associated with a given transaction_id, if we find an entry,
            //balance becomes the new total_amount, new balance becomes total_amount - amount_paid

            JSONObject object = new JSONObject();
            JSONArray result = null;
            //get the most recent debt payment
            object.put("tableName","debt_payment");
            object.put("columnArgs",new String[]{"balance"});
            object.put("whereArgs","debt_transaction_id='"+debtTransactionID+"' ORDER BY last_edited DESC LIMIT 1");
            result = fetchData(object);
            if(result.length() > 0)
                totalDebt = result.getJSONObject(0).getDouble("balance");

            double balance = totalDebt - amountPaid;

            Log.d(TAG,"Inserting data into the Debt_Payment table");
            object = null;
            object = new JSONObject();
            object.put("tableName","debt_payment");
            object.put("debt_transaction_id",debtTransactionID);
            object.put("total_debt",totalDebt);
            object.put("amount_paid",amountPaid);
            object.put("balance",balance);
            object.put("currency","NGN");
            object.put("archived","0");
            object.put("due_date",dueDate);
            object.put("created",UtilityClass.getDateTime());
            object.put("last_edited",UtilityClass.getDateTime());
            object.put("user_id",UtilityClass.getCurrentUserID());
            DatabaseManager.insertData(object);
            Log.d(TAG,"Data inserted: "+object.toString());

            if(!isNewDebt)
            {
                //retrieve the current total debt from the debtor's table. Subtract the amount paid from
                //this debt value.
                //update the debtor table to have the new debt balance
                Log.d(TAG,"Not new debt, updating Debor table");
                object = null;
                object = new JSONObject();
                object.put("tableName","debtor");
                object.put("columnArgs",new String[]{"debt"});
                object.put("whereArgs","id="+debtorID);
                result = fetchData(object);
                totalDebt = result.getJSONObject(0).getDouble("debt");
                balance = totalDebt - amountPaid;

                //update the debtor table
                object = null;
                object = new JSONObject();
                object.put("tableName","debtor");
                object.put("debt",balance);
                object.put("last_due_date",dueDate);
                updateData(object,"id="+debtorID);
                Log.d(TAG,"Data updated: "+object.toString());
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * method called to retrieve the base unit equivalent for a given unit
     * @param item_id the id referring to the particular item
     * @param unit_id this refers to the id of the unit in the unit table
     * @return the base unit equivalent
     */
    public static int getBaseUnitEquivalent(long item_id, long unit_id)
    {
        int r = -1;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName","unit");
            object.put("columnArgs",new String[]{"base_unit_equivalent"});
            object.put("whereArgs","item_id="+item_id+" AND id="+unit_id);
            JSONArray result = fetchData(object);
            r = result.getJSONObject(0).getInt("base_unit_equivalent");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return r;
    }

    /**
     * this method is called to retrieve the base unit equivalents for all sub-units for a given
     * item. For example, by passing the id for a particular item, it returns a Tuple array for all
     * the sub-units related to the given item.e to be retrieved
     * @param item_id the id for the item whose base units ar
     * @return an array of Tuples containing the unit(String) and the base unit equvalent(Integer)
     */
    public static Tuples[] getBaseUnitEquivalents(long item_id)
    {
        Tuples<String, Integer> results[] = null;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName","unit");
            object.put("columnArgs", new String[]{"base_unit_equivalent","unit"});
            object.put("whereArgs","item_id="+item_id);
            object.put("extra","ORDER BY base_unit_equivalent DESC");
            JSONArray array = fetchData(object);
            results= new Tuples[array.length()];
            Tuples<String,Integer> res = null;
            for (int i=0; i< array.length(); i++)
            {
                object = array.getJSONObject(i);
                res = new Tuples<>();
                res.setValues(object.getString("unit"),object.getInt("base_unit_equivalent"));
                results[i] = res;
            }


        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return results;
    }

    private void isCalledFromUIThread()
    {
        System.out.println(Thread.currentThread().getName());
    }


    /**
     * method to check if a given client already exists in the database
     * @param phoneNumber of client to search for
     * @return client's ID or 0 if nothing was found
     */
    public static long getClientID(String phoneNumber)
    {
        long result = 0;
        try
        {
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            object.put("tableName","client");
            object.put("columnArgs",new String[]{"id"});
            object.put("whereArgs","phone_number='"+phoneNumber+"'");
            array = fetchData(object);
            if(array.length()>0)
            {
                object = array.getJSONObject(0);
                result = object.getLong("id");
            }

        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
