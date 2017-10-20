package com.example.shopinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by USER on 08/04/2017.
 */

public class ShopDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shop.db";
    private static final int DATABASE_VERSION = 1;

    public ShopDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a String that contains the SQL statement to create the items table
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ShopContract.ItemEntry.TABLE_NAME + " ("
                + ShopContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ShopContract.ItemEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ShopContract.ItemEntry.COLUMN_PRICE + " REAL NOT NULL, "
                + ShopContract.ItemEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                + ShopContract.ItemEntry.COLUMN_IMAGE + " BLOB);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_ITEMS_TABLE);
        Log.e(ShopContract.ItemEntry.TABLE_NAME, "db created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //Db is on version one, so no upgrade is needed for now.
    }
}
