package josemarq.inventoryudacityapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.facebook.stetho.Stetho;

import java.util.Random;

import josemarq.inventoryudacityapp.data.InventoryContract;
import josemarq.inventoryudacityapp.data.InventoryDbHelper;
import josemarq.inventoryudacityapp.data.Items;

public class MainActivity extends AppCompatActivity {

    InventoryDbHelper dbHelper;
    ItemCursorAdapter adapter;
    int lastVisibleItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Use Stetho for monitoring DB on Chrome
        Stetho.initializeWithDefaults(this);

        dbHelper = new InventoryDbHelper(this);

        //Add Floating Button
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        final ListView listView = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        //Read DB
        Cursor cursor = dbHelper.readItems();

        adapter = new ItemCursorAdapter(this, cursor);
        listView.setAdapter(adapter);
        //Close Cursor
        cursor.close();

        //Method for hide/show FAB on scroll
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == 0) return;
                final int currentFirstVisibleItem = view.getFirstVisiblePosition();
                if (currentFirstVisibleItem > lastVisibleItem) {
                    fab.hide();
                } else if (currentFirstVisibleItem < lastVisibleItem) {
                    fab.show();
                }
                lastVisibleItem = currentFirstVisibleItem;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.swapCursor(dbHelper.readItems());
    }

    //On click row open Details
    public void clickOnViewItem(long id) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("itemId", id);
        startActivity(intent);
    }

    //Click cart button decrease quantity by 1
    public void clickCart(long id, int quantity) {
        dbHelper.sellItem(id, quantity);
        adapter.swapCursor(dbHelper.readItems());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_dummy:
                // Insert dummy item to the DB
                addDummyData();
                adapter.swapCursor(dbHelper.readItems());
                return true;
            case R.id.action_delete_all_data:
                // Delete all items from DB showing an confirmation dialog
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Add Dummy random data to DB
    private void addDummyData() {
        Random random = new Random();
        int numberOfMethods = 5;

        switch(random.nextInt(numberOfMethods)) {
            case 0:
                Items dummy = new Items(
                        "Sweet Donuts",
                        "3",
                        10,
                        "Chez VJ Café",
                        "chezvj@yummy.com",
                        "android.resource://josemarq.inventoryudacityapp/drawable/donut");
                dbHelper.insertItem(dummy);
                break;
            case 1:
                Items dummy1 = new Items(
                        "Soda Fresh",
                        "7",
                        14,
                        "The Soda Cola Company",
                        "sodafresh@yummy.com",
                        "android.resource://josemarq.inventoryudacityapp/drawable/soda");
                dbHelper.insertItem(dummy1);
                break;
            case 2:
                Items dummy2 = new Items(
                        "Chocolate Chips Cookies",
                        "2",
                        40,
                        "The Old Julymar's Company",
                        "greatcookies@yummy.com",
                        "android.resource://josemarq.inventoryudacityapp/drawable/cookies");
                dbHelper.insertItem(dummy2);
                break;
            case 3:
                Items dummy3 = new Items(
                        "Potato Chips",
                        "1",
                        50,
                        "Mr. Potato Head Inc.",
                        "potatohead@yummy.com",
                        "android.resource://josemarq.inventoryudacityapp/drawable/chips");
                dbHelper.insertItem(dummy3);
                break;
            case 4:
                Items dummy4 = new Items(
                        "Salted Pretzel",
                        "6",
                        18,
                        "Koln Germany Inc.",
                        "pretzels@yummy.com",
                        "android.resource://josemarq.inventoryudacityapp/drawable/pretzel");
                dbHelper.insertItem(dummy4);
                break;

            default:
                Items dummy0 = new Items(
                        "Salted Pretzel",
                        "6",
                        18,
                        "Koln Germany Inc.",
                        "pretzels@yummy.com",
                        "android.resource://josemarq.inventoryudacityapp/drawable/pretzel");
                dbHelper.insertItem(dummy0);
        }


    }

    private int deleteAllRowsFromTable() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.delete(InventoryContract.ItemsEntry.TABLE_NAME, null, null);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllRowsFromTable();
                adapter.swapCursor(dbHelper.readItems());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
