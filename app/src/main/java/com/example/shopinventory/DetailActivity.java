package com.example.shopinventory;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.shopinventory.data.ShopContract;

import java.io.ByteArrayOutputStream;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Private fields for input fields */
    private EditText productName;
    private EditText price;
    private EditText quantity;
    private Button increment;
    private Button decrement;
    private Button order;
    private Button delete;
    private ImageView imageView;
    private boolean mItemHasChanged = false;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 2;

    /** Content URI for the existing item (null if it's a new item) */
    private Uri mCurrentItemUri;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private byte[] imageByte; //Field to store converted bitmap images to byte array



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Requesting permissions on runtime.
        if (ContextCompat.checkSelfPermission(DetailActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(DetailActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(DetailActivity.this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(DetailActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                ActivityCompat.requestPermissions(DetailActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_CAMERA and MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE are an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            }
        }

        /*Find all the relevant views from the private fields declared above */
            productName = (EditText) findViewById(R.id.etProductName);
            price = (EditText) findViewById(R.id.etPrice);
            quantity = (EditText) findViewById(R.id.etQty);
            increment = (Button) findViewById(R.id.increase);
            decrement = (Button) findViewById(R.id.decrease);
            order = (Button) findViewById(R.id.btn_order);
            delete = (Button) findViewById(R.id.btn_delete);
            imageView = (ImageView) findViewById(R.id.image);

            //Set onTouch listener for the input fields to determine if changes are being made
            productName.setOnTouchListener(mTouchListener);
            price.setOnTouchListener(mTouchListener);
            quantity.setOnTouchListener(mTouchListener);
            increment.setOnTouchListener(mTouchListener);
            decrement.setOnTouchListener(mTouchListener);
            order.setOnTouchListener(mTouchListener);
            delete.setOnTouchListener(mTouchListener);
            imageView.setOnTouchListener(mTouchListener);


            //Get the intent from the previous activity and examine to determine if
            //we are creating a new item or editing an existing one.
            Intent i = getIntent();
            mCurrentItemUri = i.getData();

            //If null, that means we are creating a new item.
            if (mCurrentItemUri == null) {
                //This is a new item, so change the title at the app bar
                setTitle("Add Item");

                // Hide the Order button (It doesn't make sense to order more of an item that hasn't been created yet.)
                order.setVisibility(View.GONE);
                // Hide the Delete button(It doesn't make sense to delete an item that hasn't been created yet.)
                delete.setVisibility(View.GONE);
            } else {
                setTitle("Edit Item");
                order.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
            }

            if (mCurrentItemUri != null) {
                //Initialize loader
                getLoaderManager().initLoader(1, null, this);
            }


            increment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int qty;
                    if (TextUtils.isEmpty(quantity.getText())) {
                        qty = 0;
                        increment(qty);
                    } else {
                        qty = Integer.parseInt(quantity.getText().toString());
                        increment(qty);
                    }
                }
            });

            decrement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int qty;
                    if (TextUtils.isEmpty(quantity.getText())) {
                        qty = 0;
                        decrement(qty);
                    } else {
                        qty = Integer.parseInt(String.valueOf(quantity.getText().toString()));
                        decrement(qty);
                    }
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (i.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(i, 1);
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog();
                }
            });

            order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL,
                            new String[]{"oleohialli@gmail.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, "More of " + productName.getText().toString());
                    i.putExtra(Intent.EXTRA_TEXT, "Please supply us with:" + "\nName: " + productName.getText().toString()
                            + "\nQuantity: 10");

                    // Verify that the intent will resolve to an activity
                    if (i.resolveActivity(getPackageManager()) != null) {
                        startActivity(i);
                    }
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request

            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            imageView.setImageBitmap(imageBitmap);

        }
    }

    /* Helper methods to convert image */
    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Check for empty fields
                if (TextUtils.isEmpty(productName.getText().toString().trim())
                        || TextUtils.isEmpty(price.getText().toString().trim())
                        || TextUtils.isEmpty(quantity.getText().toString().trim())) {
                    Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                }else {
                    // Save item to database
                    saveItem();
                    finish();
                }
                return true;
            
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    /************************ Helper methods **********************/

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    // method to handle increment
    public void increment(int qty){
        qty++;
        quantity.setText(String.valueOf(qty));
    }

    // method to handle decrement
    public void decrement(int qty){
        if(qty == 0) {
            qty = 0;
            quantity.setText(String.valueOf(qty));
        }
        else {
            qty--;
            quantity.setText(String.valueOf(qty));
        }
    }

    /**
     * Get user input from editor and save new item into database.
     */
    private void saveItem() {
        //Read from input fields. Use trim() to eliminate trailing white spaces.
        String nameString = productName.getText().toString().trim();
        String priceString = price.getText().toString().trim();
        String quantityString = quantity.getText().toString().trim();

            // Convert priceString and quantityString to data types double and int respectively.
            double price = Double.parseDouble(priceString);
            int quantity = Integer.parseInt(quantityString);

            //First convert imageview to bitmap, before converting the bitmap to a byte array
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            imageByte = getBytes(bitmap);

            //If all the fields are default, then return early without inserting new data
            if (mCurrentItemUri == null && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)) {
                //Since no fields were modified, we can return early without creating a new item.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                finish();
                return;
            }

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(ShopContract.ItemEntry.COLUMN_NAME, nameString);
            values.put(ShopContract.ItemEntry.COLUMN_PRICE, price);
            values.put(ShopContract.ItemEntry.COLUMN_QUANTITY, quantity);
            values.put(ShopContract.ItemEntry.COLUMN_IMAGE, imageByte);


        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(ShopContract.ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error with saving item",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Item saved",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Error with updating item",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Item updated",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Error with deleting item",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Item deleted",
                        Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {ShopContract.ItemEntry._ID, ShopContract.ItemEntry.COLUMN_NAME, ShopContract.ItemEntry.COLUMN_PRICE,
                ShopContract.ItemEntry.COLUMN_QUANTITY, ShopContract.ItemEntry.COLUMN_IMAGE};

        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_PRICE);
            int qtyColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            double priceDouble = cursor.getDouble(priceColumnIndex);
            int qty = cursor.getInt(qtyColumnIndex);
            byte[] imageByte = cursor.getBlob(imageColumnIndex);

            // Update the views on the screen with the values from the database
            productName.setText(name);
            price.setText(String.valueOf(priceDouble));
            quantity.setText(String.valueOf(qty));

            //First convert image byte to bitmap before loading in the imageview.
             imageView.setImageBitmap(getImage(imageByte));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productName.setText("");
        price.setText("");
        quantity.setText("");
        imageView.setImageResource(R.drawable.blank_image);
    }
}
