package josemarq.inventoryudacityapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    public InventoryContract() { }

    public static final class ItemsEntry implements BaseColumns {

        public static final String TABLE_NAME = "stock";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";
        public static final String COLUMN_IMAGE = "image";

        public static final String CREATE_TABLE_Items = "CREATE TABLE " +
                InventoryContract.ItemsEntry.TABLE_NAME + "(" +
                InventoryContract.ItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                InventoryContract.ItemsEntry.COLUMN_NAME + " TEXT NOT NULL," +
                InventoryContract.ItemsEntry.COLUMN_PRICE + " TEXT NOT NULL," +
                InventoryContract.ItemsEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                InventoryContract.ItemsEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL," +
                InventoryContract.ItemsEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL," +
                ItemsEntry.COLUMN_IMAGE + " TEXT NOT NULL" + ");";
    }
}
