package com.example.androidfinalproject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView receiptRecyclerView;
    ReceiptDatabaseHelper dbHelper;
    ArrayList<Receipt> receiptList;
    ReceiptAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        receiptRecyclerView = findViewById(R.id.receiptRecyclerView);
        dbHelper = new ReceiptDatabaseHelper(this);
        receiptList = new ArrayList<>();

        loadReceipts();

        adapter = new ReceiptAdapter(receiptList, new ReceiptAdapter.OnReceiptClickListener() {
            @Override
            public void onReceiptClick(Receipt receipt) {
                Intent intent = new Intent(HistoryActivity.this, EditReceiptActivity.class);
                intent.putExtra("id", receipt.id);
                intent.putExtra("vendor", receipt.vendor);
                intent.putExtra("date", receipt.date);
                intent.putExtra("total", receipt.total);
                startActivity(intent);
            }
        });

        receiptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        receiptRecyclerView.setAdapter(adapter);
    }

    private void loadReceipts() {
        Cursor cursor = dbHelper.readAllReceipts();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String vendor = cursor.getString(cursor.getColumnIndexOrThrow("vendor"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("receipt_date"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                receiptList.add(new Receipt(id, vendor, date, total));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        receiptList.clear();
        loadReceipts();
        adapter.notifyDataSetChanged();
    }
}
