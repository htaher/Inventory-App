package com.example.android.phones;

/**
 * Created by hussain.taher on 16.06.2017.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static com.example.android.phones.data.PhoneContract.PhoneEntry;

/**
 * {@link PhoneCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of phone data as its data source. This adapter knows
 * how to create list items for each row of phone data in the {@link Cursor}.
 */
public class PhoneCursorAdapter extends CursorAdapter {

    private static final String TAG = PhoneCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link PhoneCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    protected PhoneCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the phone data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current phone can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView image = (ImageView) view.findViewById(R.id.product_image);
        TextView phoneName = (TextView) view.findViewById(R.id.name);
        TextView phoneQuantity = (TextView) view.findViewById(R.id.quantity_number_text);
        TextView quantitySold = (TextView) view.findViewById(R.id.quantity_sold_number_text);
        TextView phonePrice = (TextView) view.findViewById(R.id.price_number_text);
        ImageView buyButton = (ImageView) view.findViewById(R.id.sale_button);

        // Find the columns of phone attributes that we're interested in
        int imageColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_QUANTITY);
        int quantitySoldColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_QUANTITY_SOLD);
        int priceColumnIndex = cursor.getColumnIndex(PhoneEntry.COLUMN_PHONE_PRICE);

        Uri imgUri = Uri.parse(cursor.getString(imageColumnIndex));
        int id = cursor.getInt(cursor.getColumnIndex(PhoneEntry._ID));
        final String Name = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int phonesSold = cursor.getInt(quantitySoldColumnIndex);
        String finalPhoneQuantity = "Quantity available: " + String.valueOf(quantity);
        String finalQuantitySold = "Quantity sold: " + String.valueOf(phonesSold);
        String Price = "Price: £" + cursor.getString(priceColumnIndex);
        final Uri currentPhoneUri = ContentUris.withAppendedId(PhoneEntry.CONTENT_URI, id);

        phoneName.setText(Name);
        phoneQuantity.setText(finalPhoneQuantity);
        phonePrice.setText(Price);
        quantitySold.setText(finalQuantitySold);

        // Glide will be used to import phone images
        Glide.with(context).load(imgUri)
                .placeholder(R.drawable.browse_image)
                .error(R.drawable.no_image_availabe)
                .crossFade()
                .centerCrop()
                .into(image);

        buyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    int minusQuantity = quantity;
                    int plusPhoneSold = phonesSold;
                    values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, --minusQuantity);
                    values.put(PhoneEntry.COLUMN_QUANTITY_SOLD, ++plusPhoneSold);
                    resolver.update(
                            currentPhoneUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentPhoneUri, null);
                } else {
                    Toast.makeText(context, "This phone is out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
