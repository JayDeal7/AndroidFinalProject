package com.example.androidfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReviewReceiptActivity extends AppCompatActivity {

    private EditText editVendor, editDate, editTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_receipt);

        editVendor = findViewById(R.id.editVendor);
        editDate = findViewById(R.id.editDate);
        editTotal = findViewById(R.id.editTotal);
        Button saveButton = findViewById(R.id.saveButton);

        // Get data from intent
        String vendor = getIntent().getStringExtra("vendor");
        String date = getIntent().getStringExtra("date");
        String total = getIntent().getStringExtra("total");

        editVendor.setText(vendor);
        editDate.setText(date);
        editTotal.setText(total);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String v = editVendor.getText().toString().trim();
                String d = editDate.getText().toString().trim();
                String t = editTotal.getText().toString().trim();

                if (v.isEmpty() || d.isEmpty() || t.isEmpty()) {
                    Toast.makeText(ReviewReceiptActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    double totalValue;
                    try {
                        totalValue = Double.parseDouble(t);
                    } catch (NumberFormatException e) {
                        Toast.makeText(ReviewReceiptActivity.this, "Invalid total amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ReceiptDatabaseHelper dbHelper = new ReceiptDatabaseHelper(ReviewReceiptActivity.this);
                    dbHelper.addReceipt(v, d, totalValue);

                    finish(); // go back to MainActivity or previous screen
                }
            }
        });
    }
}
