package com.example.sunginjung;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.sunginjung.data.InventoryContract;

/**
 * Created by Sungin Jung on 9/26/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {


    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
       return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);

        String inventoryName = cursor.getString(nameColumnIndex);
        Integer inventoryQuantity = cursor.getInt(quantityColumnIndex);
        Integer inventoryPrice = cursor.getInt(priceColumnIndex);

        nameTextView.setText(inventoryName);
        quantityTextView.setText(String.valueOf(inventoryQuantity));
        priceTextView.setText(String.valueOf(inventoryPrice));
    }
}
