package josemarq.inventoryudacityapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import josemarq.inventoryudacityapp.data.InventoryContract;
import josemarq.inventoryudacityapp.data.InventoryDbHelper;
import josemarq.inventoryudacityapp.data.Items;

import static java.lang.Integer.parseInt;

public class DetailsActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private InventoryDbHelper dbHelper;
    EditText nameEdit;
    EditText priceEdit;
    EditText quantityEdit;
    EditText supplierNameEdit;
    EditText supplierEmailEdit;
    long currentItemId;
    Button decreaseQuantity;
    Button increaseQuantity;
    ImageButton imageBtn;
    ImageView imageView;
    Uri actualUri;
    private static final int PICK_IMAGE_REQUEST = 0;
    Boolean infoItemHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameEdit = (EditText) findViewById(R.id.product_name_edit);
        priceEdit = (EditText) findViewById(R.id.price_edit);
        quantityEdit = (EditText) findViewById(R.id.quantity_edit);
        supplierNameEdit = (EditText) findViewById(R.id.supplier_name_edit);
        supplierEmailEdit = (EditText) findViewById(R.id.supplier_email_edit);
        decreaseQuantity = (Button) findViewById(R.id.decrease_quantity);
        increaseQuantity = (Button) findViewById(R.id.increase_quantity);
        imageBtn = (ImageButton) findViewById(R.id.select_image);
        imageView = (ImageView) findViewById(R.id.image_view);

        dbHelper = new InventoryDbHelper(this);
        currentItemId = getIntent().getLongExtra("itemId", 0);

        //Set Action Bar title and Hide ImageView for add item
        if (currentItemId == 0) {
            setTitle(getString(R.string.new_item));
            imageView.setVisibility(View.GONE);
        } else {
            addValuesToEditItem(currentItemId);
            setTitle(getString(R.string.edit_item));
            imageBtn.setVisibility(View.GONE);
        }

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractQuantity();
                infoItemHasChanged = true;
            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQuantity();
                infoItemHasChanged = true;
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call ChekPermissionImages method wich check if whe have permission. If yes, open image picker
                checkPermissionImages();
                infoItemHasChanged = true;
            }
        });
    }


    private void showNotSavedDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.lost_changes);
        builder.setPositiveButton(R.string.yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.no_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void subtractQuantity() {
        String previousValueString = quantityEdit.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = parseInt(previousValueString);
            quantityEdit.setText(String.valueOf(previousValue - 1));
        }
    }

    private void addQuantity() {
        String previousValueString = quantityEdit.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = parseInt(previousValueString);
        }
        quantityEdit.setText(String.valueOf(previousValue + 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemId == 0) {
            MenuItem deleteOneItemMenuItem = menu.findItem(R.id.action_delete_item);
            MenuItem deleteAllMenuItem = menu.findItem(R.id.action_delete_all_data);
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            deleteOneItemMenuItem.setVisible(false);
            deleteAllMenuItem.setVisible(false);
            orderMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //Add item to DB
                if (!addItemToDb()) {
                    return true;
                }
                //Back to parent activity
                finish();
                return true;
            case android.R.id.home:
                if (!infoItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Dimiss go to parent activity
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };
                //Show a dialog for confirm quit without save
                showNotSavedDialog(discardButtonClickListener);
                return true;
            case R.id.action_order:
                //Send order to suppplier email
                orderSupplies();
                return true;
            case R.id.action_delete_item:
                //Delete the current item on the details
                showDeleteConfirmationDialog(currentItemId);
                return true;
            case R.id.action_delete_all_data:
                //Delete Database
                showDeleteConfirmationDialog(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean addItemToDb() {
        //Validation
        boolean isAllOk = true;
        if (!checkIfValueSet(nameEdit, getString(R.string.product_title_validation))) {
            isAllOk = false;
        }
        if (!checkIfValueSet(priceEdit, getString(R.string.product_price_validation))) {
            isAllOk = false;
        }
        if (!checkIfValueSet(quantityEdit, getString(R.string.product_quantity_validation))) {
            isAllOk = false;
        }
        if (!checkIfValueSet(supplierNameEdit, getString(R.string.product_suplier_name_validation))) {
            isAllOk = false;
        }
        if (!checkIfValueSet(supplierEmailEdit, getString(R.string.product_suplier_email_validation))) {
            isAllOk = false;
        }
        if (actualUri == null && currentItemId == 0) {
            Toast.makeText(this, getString(R.string.no_image), Toast.LENGTH_SHORT).show();
            isAllOk = false;
        }
        if (!isAllOk) {
            return false;
        }

        if (currentItemId == 0) {
            Items item = new Items(
                    nameEdit.getText().toString().trim(),
                    priceEdit.getText().toString().trim(),
                    parseInt(quantityEdit.getText().toString().trim()),
                    supplierNameEdit.getText().toString().trim(),
                    supplierEmailEdit.getText().toString().trim(),
                    actualUri.toString());
            dbHelper.insertItem(item);
        } else {
            int quantity = parseInt(quantityEdit.getText().toString().trim());
            int price = Integer.parseInt(priceEdit.getText().toString().trim());


            dbHelper.updateItem(currentItemId, quantity, price);
        }
        return true;
    }

    private boolean checkIfValueSet(EditText text, String description) {
        if (TextUtils.isEmpty(text.getText())) {
            text.setError(getString(R.string.you_must_fill) + " " + description);
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }

    private void addValuesToEditItem(long itemId) {
        Cursor cursor = dbHelper.readItem(itemId);
        cursor.moveToFirst();
        nameEdit.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.ItemsEntry.COLUMN_NAME)));
        priceEdit.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.ItemsEntry.COLUMN_PRICE)));
        quantityEdit.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.ItemsEntry.COLUMN_QUANTITY)));
        supplierNameEdit.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.ItemsEntry.COLUMN_SUPPLIER_NAME)));
        supplierEmailEdit.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.ItemsEntry.COLUMN_SUPPLIER_EMAIL)));
        imageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(InventoryContract.ItemsEntry.COLUMN_IMAGE))));
        nameEdit.setEnabled(false);
        priceEdit.setEnabled(true);
        supplierNameEdit.setEnabled(false);
        supplierEmailEdit.setEnabled(false);
        imageBtn.setEnabled(false);
        cursor.close();
    }

    private void orderSupplies() {
        //Intent to email
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + supplierEmailEdit.getText().toString().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order of " + nameEdit.getText().toString().trim());
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Dear " + supplierNameEdit.getText().toString().trim() + ":\n\n"
                + "We contact you for request re-Items the product " +
                nameEdit.getText().toString().trim() +
                " as soon as possible.\n\nThanks in advance.");
        startActivity(intent);
    }


    //Delete DB
    private int deleteAllRowsFromTable() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.delete(InventoryContract.ItemsEntry.TABLE_NAME, null, null);
    }

    //Delete the specific Item
    private int deleteOneItemFromTable(long itemId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String selection = InventoryContract.ItemsEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(itemId)};
        int rowsDeleted = database.delete(
                InventoryContract.ItemsEntry.TABLE_NAME, selection, selectionArgs);
        return rowsDeleted;
    }

    // Show delete confirmation dialog depending of wich button was pressed (All/One Item)
    private void showDeleteConfirmationDialog(final long itemId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (itemId == 0) {
            builder.setMessage(R.string.delete_all_message);
        } else {
            builder.setMessage(R.string.delete_one_message);
        }
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (itemId == 0) {
                    deleteAllRowsFromTable();
                } else {
                    deleteOneItemFromTable(itemId);
                }
                finish();
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

    public void checkPermissionImages() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Item Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onBackPressed() {
        if (!infoItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        //If there are not save changes, show Dialog
        showNotSavedDialog(discardButtonClickListener);
    }

    //Handle permission for read the image from the device
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                actualUri = resultData.getData();
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageURI(actualUri);
                imageView.invalidate();
            }
        }
    }
}
