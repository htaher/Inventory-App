package com.example.android.phones.data;

/**
 * Created by hussain.taher on 16.06.2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.phones.data.PhoneContract.PhoneEntry;

public class PhoneDbHelper extends SQLiteOpenHelper {

    public static final String TAG = PhoneDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "phoneinventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link PhoneDbHelper}.
     *
     * @param context of the app
     */
    public PhoneDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase thedatabase) {
        // Create a String that contains the SQL statement to create the phones table
        String SQL_CREATE_INVENTORY = "CREATE TABLE " + PhoneEntry.TABLE_NAME + " ("
                + PhoneEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PhoneEntry.COLUMN_PHONE_IMAGE + " TEXT NOT NULL, "
                + PhoneEntry.COLUMN_PHONE_NAME + " TEXT NOT NULL, "
                + PhoneEntry.COLUMN_PHONE_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + PhoneEntry.COLUMN_QUANTITY_SOLD + " INTEGER NOT NULL, "
                + PhoneEntry.COLUMN_PHONE_PRICE + " REAL NOT NULL, "
                + PhoneEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                + PhoneEntry.COLUMN_PHONE_DESCRIPTION + " TEXT NOT NULL "
                + ");";

        thedatabase.execSQL(SQL_CREATE_INVENTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(PhoneEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
