package josemarq.inventoryudacityapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import josemarq.inventoryudacityapp.MainActivity;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public final static String DB_NAME = "inventory.db";
    public final static int DB_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(InventoryContract.ItemsEntry.CREATE_TABLE_Items);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //Get all items
    public Cursor readItems() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                InventoryContract.ItemsEntry._ID,
                InventoryContract.ItemsEntry.COLUMN_NAME,
                InventoryContract.ItemsEntry.COLUMN_PRICE,
                InventoryContract.ItemsEntry.COLUMN_QUANTITY,
                InventoryContract.ItemsEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.ItemsEntry.COLUMN_SUPPLIER_EMAIL,
                InventoryContract.ItemsEntry.COLUMN_IMAGE
        };
        Cursor cursor = db.query(
                InventoryContract.ItemsEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;
    }

    //Get single item
    public Cursor readItem(long itemId) {
        SQLiteDatabase db = getReadableDatabase();

        //Projection
        String[] projection = {
                InventoryContract.ItemsEntry._ID,
                InventoryContract.ItemsEntry.COLUMN_NAME,
                InventoryContract.ItemsEntry.COLUMN_PRICE,
                InventoryContract.ItemsEntry.COLUMN_QUANTITY,
                InventoryContract.ItemsEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.ItemsEntry.COLUMN_SUPPLIER_EMAIL,
                InventoryContract.ItemsEntry.COLUMN_IMAGE
        };

        //Selection
        String selection = InventoryContract.ItemsEntry._ID + "=?";

        //Arguments
        String[] selectionArgs = new String[] { String.valueOf(itemId) };

        //Cursor
        Cursor cursor = db.query(
                InventoryContract.ItemsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        return cursor;
    }

    //Inserting to DB
    public void insertItem(Items item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemsEntry.COLUMN_NAME, item.getProductName());
        values.put(InventoryContract.ItemsEntry.COLUMN_PRICE, item.getPrice());
        values.put(InventoryContract.ItemsEntry.COLUMN_QUANTITY, item.getQuantity());
        values.put(InventoryContract.ItemsEntry.COLUMN_SUPPLIER_NAME, item.getSupplierName());
        values.put(InventoryContract.ItemsEntry.COLUMN_SUPPLIER_EMAIL, item.getSupplierEmail());
        values.put(InventoryContract.ItemsEntry.COLUMN_IMAGE, item.getImage());
        long newRowId = db.insert(InventoryContract.ItemsEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Log.i("Error with saving in DB", " -1");
        } else {
            // Otherwise, the insertion was successful and we can display a info with the row ID.
            Log.i("Saving ok with ID: ", String.valueOf(newRowId));
        }
    }

    //Update DB for now only price and quantity
    public void updateItem(long currentItemId, int quantity, int price) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemsEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryContract.ItemsEntry.COLUMN_PRICE, price);
        String selection = InventoryContract.ItemsEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(currentItemId) };
        db.update(InventoryContract.ItemsEntry.TABLE_NAME,
                values, selection, selectionArgs);
    }

    //On sellItem decrease qty in 1 unit
    public void sellItem(long itemId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        int newQuantity = 0;
        if (quantity > 0) {
            newQuantity = quantity -1;
        } else {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ItemsEntry.COLUMN_QUANTITY, newQuantity);
        String selection = InventoryContract.ItemsEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(itemId) };
        db.update(InventoryContract.ItemsEntry.TABLE_NAME,
                values, selection, selectionArgs);
    }


}
