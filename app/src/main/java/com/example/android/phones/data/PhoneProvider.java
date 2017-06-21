package com.example.android.phones.data;

/**
 * Created by hussain.taher on 16.06.2017.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.android.phones.data.PhoneContract.PhoneEntry;

/**
 * {@link ContentProvider} for Phones Inventory app.
 */
public class PhoneProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String TAG = PhoneProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the phones table
     */
    private static final int PHONES = 100;

    /**
     * URI matcher code for the content URI for a single phone in the phones table
     */
    private static final int PHONES_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.phones/phones" will map to the
        // integer code {@link #PHONES}. This URI is used to provide access to MULTIPLE rows
        // of the phones table.
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES, PHONES);

        // The content URI of the form "content://com.example.android.phones/phones/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the phones table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.phones/phones/3" matches, but
        // "content://com.example.android.pets/phones" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES + "/#", PHONES_ID);
    }

    /**
     * Database helper object
     */
    private PhoneDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PhoneDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                // For the PHONES code, query the phones table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the phones table.
                cursor = database.query(PhoneEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PHONES_ID:
                // For the PHONES_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.phones/phones/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the phones table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PhoneEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return PhoneEntry.CONTENT_LIST_TYPE;
            case PHONES_ID:
                return PhoneEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return insertPhone(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri.toString());
        }
    }

    /**
     * Insert a phone into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    public Uri insertPhone(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(PhoneEntry.COLUMN_PHONE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Phone requires a name");
        }

        // Quantity
        Integer quantity = values.getAsInteger(PhoneEntry.COLUMN_PHONE_QUANTITY);

        // Check that the price is not null and have a value more than 0
        Float price = values.getAsFloat(PhoneEntry.COLUMN_PHONE_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Phone requires valid price");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        // Insert the new phone with the given values
        long id = database.insert(PhoneEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the phone content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PHONES:
                rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PHONES_ID:
                selection = PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PhoneEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if (contentValues == null) {
            throw new IllegalArgumentException("Cannot update empty values");
        }
        switch (match) {
            case PHONES:
                rowsUpdated = database.update(PhoneEntry.TABLE_NAME, contentValues, selection,
                        selectionArgs);
                break;
            case PHONES_ID:
                rowsUpdated = database.update(PhoneEntry.TABLE_NAME, contentValues,
                        PhoneEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsUpdated;
    }

}

