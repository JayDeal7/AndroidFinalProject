package com.example.androidfinalproject;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReviewReceiptActivity extends AppCompatActivity {

    private EditText editVendor, editDate, editTotal;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_receipt);

        editVendor = findViewById(R.id.editVendor);
        editDate   = findViewById(R.id.editDate);
        editTotal  = findViewById(R.id.editTotal);
        categorySpinner = findViewById(R.id.categorySpinner);
        Button saveButton = findViewById(R.id.saveButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.receipt_categories, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        String vendor = getIntent().getStringExtra("vendor");
        String date   = getIntent().getStringExtra("date");
        String total  = getIntent().getStringExtra("total");
        if (!TextUtils.isEmpty(vendor)) editVendor.setText(vendor);
        if (!TextUtils.isEmpty(date))   editDate.setText(date);
        if (!TextUtils.isEmpty(total))  editTotal.setText(total);

        String uriStr = getIntent().getStringExtra("image_uri");
        if (!TextUtils.isEmpty(uriStr)) {
            Uri imgUri = Uri.parse(uriStr);
            OcrProcessor.extractTextFromImage(this, imgUri, new OcrProcessor.OcrCallback() {
                @Override public void onTextExtracted(String result) {
                    String v = ReceiptParser.extractVendor(result);
                    String d = ReceiptParser.extractDate(result);
                    String t = ReceiptParser.extractTotal(result);
                    if (!TextUtils.isEmpty(v)) editVendor.setText(v);
                    if (!TextUtils.isEmpty(d)) editDate.setText(d);
                    if (!TextUtils.isEmpty(t)) editTotal.setText(t);
                }
                @Override public void onError(Exception e) {
                    Toast.makeText(ReviewReceiptActivity.this, "OCR failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        saveButton.setOnClickListener(view -> {
            String v = editVendor.getText().toString().trim();
            String d = editDate.getText().toString().trim();
            String t = editTotal.getText().toString().trim();
            String c = categorySpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(d)) {
                d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                editDate.setText(d);
            }
            if (TextUtils.isEmpty(v) || TextUtils.isEmpty(t)) {
                Toast.makeText(this, "Please enter Vendor and Total", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalValue;
            try {
                t = t.replaceAll("[^0-9.\\-]", "");  // strip $ and commas
                totalValue = Double.parseDouble(t);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid total amount", Toast.LENGTH_SHORT).show();
                return;
            }

            ReceiptDatabaseHelper dbHelper = new ReceiptDatabaseHelper(this);
            dbHelper.insertReceipt(v, d, totalValue, c);  // triggers limit check

            // ---- DEBUG TOAST: show what DB/prefs see right now ----
            double monthTotal = dbHelper.debugGetCurrentMonthTotal();
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            float limit = prefs.getFloat("monthly_limit", 0f);
            Toast.makeText(
                    this,
                    "Saved. Month total = $" + String.format(Locale.US, "%.2f", monthTotal) +
                            " | Limit = $" + String.format(Locale.US, "%.2f", limit),
                    Toast.LENGTH_LONG
            ).show();
            // -------------------------------------------------------

            finish();
        });
    }
}
