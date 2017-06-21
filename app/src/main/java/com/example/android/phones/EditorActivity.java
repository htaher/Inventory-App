package com.example.android.phones;

/**
 * Created by hussain.taher on 16.06.2017.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.phones.data.PhoneContract.PhoneEntry;

import java.io.File;

/**
 * Allows user to create a new phone or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = EditorActivity.class.getSimpleName();

    public static final int SELECT_IMAGE_REQUEST = 20;
    public static final int STORAGE_PERMISSION_CODE = 21;

    /**
     * Identifier for the phone data loader
     */
    private static final int EXISTING_PHONE_LOADER = 0;

    /**
     * Phone query projection
     */
    public final String[] PHONES_COLUMNS = {
            PhoneEntry._ID,
            PhoneEntry.COLUMN_PHONE_IMAGE,
            PhoneEntry.COLUMN_PHONE_NAME,
            PhoneEntry.COLUMN_PHONE_QUANTITY,
            PhoneEntry.COLUMN_QUANTITY_SOLD,
            PhoneEntry.COLUMN_PHONE_PRICE,
            PhoneEntry.COLUMN_SUPPLIER_EMAIL,
            PhoneEntry.COLUMN_PHONE_DESCRIPTION
    };

    /**
     * Content URI for the existing phone (null if it's a new phone)
     */
    private Uri mCurrentPhoneUri;

    /**
     * ImageView for the phone's image
     */
    private ImageView mPhoneImage;

    /**
     * EditText field to enter the phone's name
     */
    private EditText mPhoneName;

    /**
     * EditText field to enter the phone's quantity
     */
    private EditText mPhoneQuantity;

    /**
     * EditText field to enter the phone's quantity sold
     */
    private EditText mQuantitySold;

    /**
     * EditText field to enter the phone's price
     */
    private EditText mPhonePrice;

    /**
     * EditText field to enter the phone's supplier name
     */
    private EditText mSupplierEmail;

    /**
     * EditText field to enter the phone's description
     */
    private EditText mPhoneDescription;

    /**
     * Calculated quantity when + & - is clicked
     */
    private int mCalculatedQuantity;

    /**
     * For the image browser
     */
    private Button mBrowseImage;

    /**
     * Let us know if an image was selected or not
     */
    private int mImageSelected = 0;

    /**
     * Action elements
     */
    private ImageView mMinusButton;
    private ImageView mPlusButton;
    private Button mRestockBtn;
    private Button mDeleteBtn;
    private Button mSaveBtn;

    private String mCurrentImageUri = "no images";
    private String mRestockEmail;
    private String mRestockPhone;

    /**
     * Boolean flag that keeps track of whether the phone has been edited (true) or not (false)
     */
    private boolean mPhoneHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPhoneHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mPhoneImage = (ImageView) findViewById(R.id.phone_image);
        mPhoneName = (EditText) findViewById(R.id.edit_phone_name);
        mPhoneQuantity = (EditText) findViewById(R.id.quantity_editer);
        mQuantitySold = (EditText) findViewById(R.id.edit_quantity_sold);
        mPhonePrice = (EditText) findViewById(R.id.edit_phone_price);
        mSupplierEmail = (EditText) findViewById(R.id.edit_supplier_email);
        mPhoneDescription = (EditText) findViewById(R.id.edit_phone_description);
        mMinusButton = (ImageView) findViewById(R.id.less_quantity);
        mPlusButton = (ImageView) findViewById(R.id.more_quantity);
        mBrowseImage = (Button) findViewById(R.id.browse_image);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mPhoneImage.setOnTouchListener(mTouchListener);
        mPhoneName.setOnTouchListener(mTouchListener);
        mPhoneQuantity.setOnTouchListener(mTouchListener);
        mQuantitySold.setOnTouchListener(mTouchListener);
        mPhonePrice.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);
        mPhoneDescription.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);
        mBrowseImage.setOnTouchListener(mTouchListener);

        // Find all relevant views for the action elements
        mRestockBtn = (Button) findViewById(R.id.restock_phone_btn);
        mDeleteBtn = (Button) findViewById(R.id.delete_phone_btn);
        mSaveBtn = (Button) findViewById(R.id.save_phone_btn);

        // Decrease the quantity of 1
        mMinusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mCalculatedQuantity > 0) {
                    int calculatedQuantity = mCalculatedQuantity - 1;
                    mPhoneQuantity.setText(String.valueOf(calculatedQuantity));
                    mCalculatedQuantity = calculatedQuantity;
                }
            }
        });

        // Increase the quantity of 1
        mPlusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int calculatedQuantity = mCalculatedQuantity + 1;
                mPhoneQuantity.setText(String.valueOf(calculatedQuantity));
                mCalculatedQuantity = calculatedQuantity;
            }
        });


        // Set an OnClickListener to update the image
        mBrowseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onImagePhoneUpdate(view);
            }
        });

        // Set an OnClickListener to save
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePhone();
            }
        });

        // Set an OnClickListener to delete
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        // Set an OnClickListener to restock
        mRestockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restockSupplier();
            }
        });


        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();

        // Check if this is supposed to be a new phone or editing phone
        if (mCurrentPhoneUri == null) {
            // Set create new phone title
            setTitle(getString(R.string.editor_activity_title_new_phone));

            // Hide restock and delete button
            mRestockBtn.setVisibility(View.GONE);
            mDeleteBtn.setVisibility(View.GONE);

        } else {
            // Set edit phone title
            setTitle(getString(R.string.editor_activity_title_edit_phone));

            // Confirm than an image was selected
            mImageSelected = 1;

            // Show restock and delete button
            mRestockBtn.setVisibility(View.VISIBLE);
            mDeleteBtn.setVisibility(View.VISIBLE);

            //Read database for selected Product
            getLoaderManager().initLoader(EXISTING_PHONE_LOADER, null, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // If user didn't make any change
                if (!mPhoneHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // User has made some change
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they haven't save changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onImagePhoneUpdate(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // We are on M or above so there is a need to ask for permission
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getImage();
            } else {
                // Here if we don't already have permission
                String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissionRequest, STORAGE_PERMISSION_CODE);
            }
        } else {
            // We are on an older device so no permission needed
            getImage();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission OK
            getImage();
        } else {
            Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();
        }
    }

    private void getImage() {
        // Access the image gallery using an implicit intent.
        Intent photoSelectorIntent = new Intent(Intent.ACTION_PICK);

        // Find data
        File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String imageDirectoryPath = imageDirectory.getPath();

        // Get the URI
        Uri data = Uri.parse(imageDirectoryPath);

        // Set the data and type
        photoSelectorIntent.setDataAndType(data, "image/*");

        startActivityForResult(photoSelectorIntent, SELECT_IMAGE_REQUEST);
    }

    @Override
    public void onBackPressed() {
        // Go back if there is no change
        if (!mPhoneHasChanged) {
            super.onBackPressed();
            return;
        }

        // Protect user from loosing info
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are not saved change
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                // If we are here, everything processed successfully and we have an Uri data
                Uri mPhoneImageUri = data.getData();
                mCurrentImageUri = mPhoneImageUri.toString();
                mImageSelected = 1;
                Log.d(TAG, "Selected image " + mPhoneImageUri);

                // We use Glide to import image
                Glide.with(this).load(mPhoneImageUri)
                        .placeholder(R.drawable.browse_image)
                        .crossFade()
                        .fitCenter()
                        .into(mPhoneImage);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                mCurrentPhoneUri,
                PHONES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of phone attributes that we're interested in
            int i_ID = 0;
            int imageColumnIndex = 1;
            int nameColumnIndex = 2;
            int quantityColumnIndex = 3;
            int quantitySoldColumnIndex = 4;
            int priceColumnIndex = 5;
            int supplierEmailColumnIndex = 6;
            int descriptionColumnIndex = 7;

            // Extract values from current cursor
            mCurrentImageUri = cursor.getString(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int itemSold = cursor.getInt(quantitySoldColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);

            mRestockEmail = supplierEmail;
            mRestockPhone = name;
            mCalculatedQuantity = quantity;

            // We updates fields to values on the database
            mPhoneName.setText(name);
            mPhoneQuantity.setText(String.valueOf(quantity));
            mQuantitySold.setText(String.valueOf(itemSold));
            mPhonePrice.setText(String.valueOf(price));
            mSupplierEmail.setText(supplierEmail);
            mPhoneDescription.setText(description);

            // Update the image using Glide
            Glide.with(this).load(mCurrentImageUri)
                    .placeholder(R.drawable.browse_image)
                    .error(R.drawable.no_image_availabe)
                    .crossFade()
                    .fitCenter()
                    .into(mPhoneImage);
        }
    }

    /**
     * Get user input from editor and save/update product into database.
     */
    private void savePhone() {
        //Read Values from text field
        // Use trim to eliminate leading or trailing white space
        String nameString = mPhoneName.getText().toString().trim();
        String quantityString = mPhoneQuantity.getText().toString().toString();
        String quantitySoldString = mQuantitySold.getText().toString().trim();
        String priceString = mPhonePrice.getText().toString().trim();
        String supplierEmailString = mSupplierEmail.getText().toString().trim();
        String descriptionString = mPhoneDescription.getText().toString().trim();

        // Check if this is supposed to be a new phone
        if (mImageSelected != 1 || TextUtils.isEmpty(nameString)
                || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(quantitySoldString)
                || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierEmailString)
                || TextUtils.isEmpty(descriptionString)) {

            Toast.makeText(this, R.string.all_fields_requested, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and phone attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PhoneEntry.COLUMN_PHONE_IMAGE, mCurrentImageUri);
        values.put(PhoneEntry.COLUMN_PHONE_NAME, nameString);
        values.put(PhoneEntry.COLUMN_PHONE_QUANTITY, quantityString);
        values.put(PhoneEntry.COLUMN_QUANTITY_SOLD, quantitySoldString);
        values.put(PhoneEntry.COLUMN_PHONE_PRICE, priceString);
        values.put(PhoneEntry.COLUMN_PHONE_DESCRIPTION, descriptionString);
        values.put(PhoneEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);

        if (mCurrentPhoneUri == null) {
            Uri insertedRow = getContentResolver().insert(PhoneEntry.CONTENT_URI, values);
            if (insertedRow == null) {
                Toast.makeText(this, R.string.editor_insert_phone_failed, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.editor_insert_phone_successful, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, CatalogActivity.class);
                startActivity(intent);
            }
        } else {
            // It's a phone that has been updating
            int rowUpdated = getContentResolver().update(mCurrentPhoneUri, values, null, null);
            if (rowUpdated == 0) {
                Toast.makeText(this, R.string.editor_update_phone_failed, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.editor_update_phone_successful, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, CatalogActivity.class);
                startActivity(intent);
            }
        }
        // Reset the browser selection
        mImageSelected = 0;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mPhoneName.setText("");
        mPhoneQuantity.setText("");
        mQuantitySold.setText("");
        mPhonePrice.setText("");
        mSupplierEmail.setText("");
        mPhoneDescription.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the phone.
                deletePhone();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePhone() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentPhoneUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPhoneUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    // Order from supplier
    private void restockSupplier() {
        String[] TO = {mRestockEmail};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Restocking " + mRestockPhone);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Our stock of " + mRestockPhone +
                " is finished. Please send us 20 more.\nKind Regards");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}

