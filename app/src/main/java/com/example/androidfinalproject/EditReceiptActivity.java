package com.example.androidfinalproject;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidfinalproject.R;
import com.example.androidfinalproject.ReceiptDatabaseHelper;

public class EditReceiptActivity extends AppCompatActivity {

    EditText editVendor, editDate, editTotal;
    Button updateButton, deleteButton;

    int id;
    ReceiptDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isManual = getIntent().getBooleanExtra("manual", false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_receipt);

        editVendor = findViewById(R.id.editVendor);
        editDate = findViewById(R.id.editDate);
        editTotal = findViewById(R.id.editTotal);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        dbHelper = new ReceiptDatabaseHelper(this);

        // Get receipt data from intent
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        String vendor = intent.getStringExtra("vendor");
        String date = intent.getStringExtra("date");
        double total = intent.getDoubleExtra("total", 0);

        editVendor.setText(vendor);
        editDate.setText(date);
        editTotal.setText(String.valueOf(total));

        updateButton.setOnClickListener(v -> {
            String newVendor = editVendor.getText().toString();
            String newDate = editDate.getText().toString();
            double newTotal = Double.parseDouble(editTotal.getText().toString());
            dbHelper.updateReceipt(id, newVendor, newDate, newTotal);
            finish();
        });

        deleteButton.setOnClickListener(v -> {
            dbHelper.deleteReceipt(id);
            finish();
        });

        if (isManual) {
            deleteButton.setVisibility(View.GONE);

            updateButton.setText("Add Receipt");

            updateButton.setOnClickListener(v -> {
                String newVendor = editVendor.getText().toString();
                String newDate = editDate.getText().toString();
                double newTotal = Double.parseDouble(editTotal.getText().toString());
                dbHelper.insertReceipt(newVendor, newDate, newTotal);
                finish();
            });
        } else {
            // Normal edit mode
            editVendor.setText(getIntent().getStringExtra("vendor"));
            editDate.setText(getIntent().getStringExtra("date"));
            editTotal.setText(String.valueOf(getIntent().getDoubleExtra("total", 0)));
            id = getIntent().getIntExtra("id", -1);

            updateButton.setOnClickListener(v -> {
                String newVendor = editVendor.getText().toString();
                String newDate = editDate.getText().toString();
                double newTotal = Double.parseDouble(editTotal.getText().toString());
                dbHelper.updateReceipt(id, newVendor, newDate, newTotal);
                finish();
            });

            deleteButton.setOnClickListener(v -> {
                dbHelper.deleteReceipt(id);
                finish();
            });
        }
    }
}
