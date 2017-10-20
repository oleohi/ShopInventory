package com.example.shopinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shopinventory.data.ShopContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;
    private ShopCursorAdapter mCursorAdapter;
    private Uri mItemUri = ShopContract.ItemEntry.CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(i);
            }
        });

        // Find the ListView which will be populated with the item data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.emptyTextContainer);
        itemListView.setEmptyView(emptyView);

        //Set up an adapter to create a list item for each row of item data in the cursor
        //There is no item data yet until the loader finishes, so pass in null for the cursor
        mCursorAdapter = new ShopCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        //Set-up onitemclicklistener on the list items
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), DetailActivity.class);

                //Form the content URI that represents the specific item that was clicked on,
                //by appending the id (passed as input to this method) onto the content URI
                Uri currentPetUri = ContentUris.withAppendedId(ShopContract.ItemEntry.CONTENT_URI, id);

                i.setData(currentPetUri);

                startActivity(i);
            }
        });


        getLoaderManager().initLoader(ITEM_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {ShopContract.ItemEntry._ID,
                ShopContract.ItemEntry.COLUMN_NAME,
                ShopContract.ItemEntry.COLUMN_PRICE,
                ShopContract.ItemEntry.COLUMN_QUANTITY,
                ShopContract.ItemEntry.COLUMN_IMAGE};

        return new CursorLoader(this,
                ShopContract.ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                ShopContract.ItemEntry._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
