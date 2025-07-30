package com.example.androidfinalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;
    FloatingActionButton captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = findViewById(R.id.captureButton);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
    }

    // onActivityResult must be outside any listener
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Log.d("MainActivity", "Received image URI: " + imageUri);
            Toast.makeText(this, "Processing receipt...", Toast.LENGTH_SHORT).show();

            OcrProcessor.extractTextFromImage(this, imageUri, new OcrProcessor.OcrCallback() {
                @Override
                public void onTextExtracted(String result) {
                    Log.d("OCR Result", result);

                    String vendor = ReceiptParser.extractVendor(result);
                    String date = ReceiptParser.extractDate(result);
                    String total = ReceiptParser.extractTotal(result);

                    Log.d("Parsed Info", "Vendor: " + vendor);
                    Log.d("Parsed Info", "Date: " + date);
                    Log.d("Parsed Info", "Total: " + total);

                    Toast.makeText(MainActivity.this, "Vendor: " + vendor + "\nDate: " + date + "\nTotal: $" + total, Toast.LENGTH_LONG).show();

                    // TODO: Step 6 — Show this in a Review screen for editing/saving
                }

                @Override
                public void onError(Exception e) {
                    Log.e("OCR Error", "Failed to extract text", e);
                    Toast.makeText(MainActivity.this, "Failed to process receipt.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
