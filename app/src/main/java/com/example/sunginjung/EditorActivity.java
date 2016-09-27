package com.example.sunginjung;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.example.sunginjung.data.InventoryContract;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0;
    private Uri mCurrentInventoryUri;
    private String mProductName;


    private EditText mNameEditText;
    private EditText mPriceEditText;
    private TextView mQuantityText;
    private Button mOrderButton;
    private Button mDeleteButton = (Button) findViewById(R.id.delete_button);
    private Button mIncrementButton = (Button) findViewById(R.id.increment);
    private Button mDecrementButton = (Button) findViewById(R.id.decrement);

    private boolean mInventoryHasChanged = false;

    private int inventoryQuantity;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        if (mCurrentInventoryUri == null) {

            setTitle(getString(R.string.editor_activity_title_new_inventory));

            //Hide Delete Button
            mDeleteButton.setVisibility(View.GONE);

        } else {

            setTitle(getString(R.string.editor_activity_title_edit_inventory));

            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        mProductName = "";

        mNameEditText = (EditText) findViewById(R.id.edit_inventory_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_inventory_price);
        mQuantityText = (TextView) findViewById(R.id.quantity_view);
        mOrderButton = (Button) findViewById(R.id.order_button);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mIncrementButton.setOnTouchListener(mTouchListener);
        mDecrementButton.setOnTouchListener(mTouchListener);

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inventoryQuantity = inventoryQuantity + 1;
                mQuantityText.setText(Integer.toString(inventoryQuantity));
            }
        });

        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inventoryQuantity >= 0) {
                    inventoryQuantity = inventoryQuantity - 1;

                }
                else {
                    inventoryQuantity = 0;
                }
                mQuantityText.setText(Integer.toString(inventoryQuantity));
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, "I need more of " + mProductName);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

    }



    private void saveInventory() {

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        int quantity = inventoryQuantity;

        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)) {
          return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, Integer.valueOf(priceString));
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);

        if (mCurrentInventoryUri == null) {

            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
               Toast.makeText(this, "Insertion failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Insertion successful",
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, "Update failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Update successful",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveInventory();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

        @Override
        public void onBackPressed() {

            if (!mInventoryHasChanged) {
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


            showUnsavedChangesDialog(discardButtonClickListener);
        }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY};

        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);

            String name = cursor.getString(nameColumnIndex);
            mProductName = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            inventoryQuantity = cursor.getInt(quantityColumnIndex);

            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityText.setText(Integer.toString(quantity));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityText.setText("");
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteInventory();
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


    private void deleteInventory() {
        if (mCurrentInventoryUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            if (rowsDeleted == 0) {

                Toast.makeText(this, "Failed!",
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, "Success!",
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }
}