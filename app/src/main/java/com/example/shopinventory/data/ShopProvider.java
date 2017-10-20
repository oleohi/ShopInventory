package com.example.shopinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by USER on 08/04/2017.
 */

public class ShopProvider extends ContentProvider {

    /** URI matcher code for the content URI for the items table */
    private static final int ITEMS = 100;

    /** URI matcher code for the content URI for a single item in the items table */
    private static final int ITEM_ID = 101;

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

        // TODO: Add 2 content URIs to URI matcher
        sUriMatcher.addURI(ShopContract.CONTENT_AUTHORITY, ShopContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(ShopContract.CONTENT_AUTHORITY, ShopContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    ShopDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ShopDbHelper(getContext());
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
            case ITEMS:
                cursor = database.query(ShopContract.ItemEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case ITEM_ID:
                selection = ShopContract.ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ShopContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //Set notification URI on the cursor, so we know what content URI the cursor was created for.
        //If the data at this URI changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri){
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ShopContract.ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ShopContract.ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        // Track the number of rows that were deleted
        int rowsDeleted;
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ShopContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = ShopContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ShopContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // check that the name value is not null.

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);

            case ITEM_ID:
                selection = ShopContract.ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /************************************************
     ****************Helper Methods******************
     ************************************************/

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // TODO: Insert a new pet into the pets database table with the given ContentValues
        // Check that the name and other required fields are not null
        String name = values.getAsString(ShopContract.ItemEntry.COLUMN_NAME);
        Double price = values.getAsDouble(ShopContract.ItemEntry.COLUMN_PRICE);
        Integer quantity = values.getAsInteger(ShopContract.ItemEntry.COLUMN_PRICE);
        if (name == null && price == null && quantity == null) {
            throw new IllegalArgumentException("Item requires a name, quantity and price");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = database.insert(ShopContract.ItemEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e("App", "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the pet content URI.
        //uri: content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // TODO: Update the selected pets in the items table with the given ContentValues
        // check that the required fields values are not null.
        if (values.containsKey(ShopContract.ItemEntry.COLUMN_NAME)
                && values.containsKey(ShopContract.ItemEntry.COLUMN_PRICE)
                && values.containsKey(ShopContract.ItemEntry.COLUMN_QUANTITY)) {
                    String name = values.getAsString(ShopContract.ItemEntry.COLUMN_NAME);
                    Double price = values.getAsDouble(ShopContract.ItemEntry.COLUMN_PRICE);
                    Integer quantity = values.getAsInteger(ShopContract.ItemEntry.COLUMN_PRICE);
                    if (name == null && price == null && quantity == null) {
                        throw new IllegalArgumentException("Item requires a name, quantity and price");
                    }
        }

        // TODO: Return the number of rows that were affected
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ShopContract.ItemEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
