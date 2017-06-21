package com.example.android.phones.data;

/**
 * Created by hussain.taher on 16.06.2017.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the phone inventory app
 */

public class PhoneContract {


    public static final String CONTENT_AUTHORITY = "com.example.android.phones";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PHONES = "phones";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PhoneContract() {
    }

    /**
     * Inner class that defines constant values for the phones database table.
     * Each entry in the table represents a single phone item.
     */

    public static final class PhoneEntry implements BaseColumns {

        /**
         * The content URI to access the phone data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PHONES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of phones.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single phone.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;

        /**
         * Name of database table for phones
         */
        public final static String TABLE_NAME = "phones";

        /**
         * Unique ID number for the phone (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Image of the phone.
         */
        public final static String COLUMN_PHONE_IMAGE = "image";

        /**
         * Name of the phone.
         */
        public final static String COLUMN_PHONE_NAME = "name";

        /**
         * Quantity of  phones.
         */
        public final static String COLUMN_PHONE_QUANTITY = "quantity";

        /**
         * Quantity of  phones that were sold.
         */
        public final static String COLUMN_QUANTITY_SOLD = "quantity_sold";

        /**
         * Price of the phone.
         */
        public final static String COLUMN_PHONE_PRICE = "price";

        /**
         * Email of the phone supplier for restock.
         */
        public final static String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Description of the phone.
         */
        public final static String COLUMN_PHONE_DESCRIPTION = "description";
    }
}
