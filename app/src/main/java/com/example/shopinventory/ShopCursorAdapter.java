package com.example.shopinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopinventory.data.ShopContract;

import static android.R.attr.id;

/**
 * Created by USER on 10/04/2017.
 */

public class ShopCursorAdapter extends CursorAdapter {
    public ShopCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);

    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        //Find individual views that we want to modify in our list_item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.productName);
        TextView quantityTextView = (TextView) view.findViewById(R.id.currentQty); //For the quantity label
        final TextView qtyTextView = (TextView) view.findViewById(R.id.qty); //For the actual quantity value
        TextView priceTextview = (TextView) view.findViewById(R.id.price);

        //find the columns of the item attributes that we are interested in
        int nameColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_NAME);
        int qtyColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_PRICE);

        //Read the item attributes from the cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        double itemPrice = cursor.getDouble(priceColumnIndex);
        final String itemQuantity = cursor.getString(qtyColumnIndex);

        //Update the textviews with the attributes of the current item
        nameTextView.setText(itemName);
        priceTextview.setText("N" + itemPrice);
        quantityTextView.setText("Remaining: ");
        qtyTextView.setText(itemQuantity);

        final int position = cursor.getPosition();

        //Controlling the sale button.
        Button sale = (Button) view.findViewById(R.id.sale);
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move cursor to current position
                cursor.moveToPosition(position);

                //get the Uri for the current item
                int itemIdColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry._ID);
                final long itemId = cursor.getLong(itemIdColumnIndex);
                Uri mCurrentItemUri = ContentUris.withAppendedId(ShopContract.ItemEntry.CONTENT_URI, itemId);

                //Get the column we are interested in from the database
                int quantityColumnIndex = cursor.getColumnIndex(ShopContract.ItemEntry.COLUMN_QUANTITY);

                //read the item attributes from the Cursor for the current phone
                String itemQuantity = cursor.getString(quantityColumnIndex);

                //convert the string back to an integer
                int updateQuantity = Integer.parseInt(itemQuantity);

                ContentValues values = new ContentValues();

                if (updateQuantity == 0){
                    updateQuantity = 0;
                    values.put(ShopContract.ItemEntry.COLUMN_QUANTITY, updateQuantity);
                    context.getContentResolver().update(mCurrentItemUri, values, null, null);
                    Toast.makeText(context.getApplicationContext(), "No more item to sell", Toast.LENGTH_SHORT).show();
                }else {
                    updateQuantity--; //reduce the quantity by 1.
                    values.put(ShopContract.ItemEntry.COLUMN_QUANTITY, updateQuantity);
                    context.getContentResolver().update(mCurrentItemUri, values, null, null);
                    Toast.makeText(context.getApplicationContext(), "Item sale successful", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
