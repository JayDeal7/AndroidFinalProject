package com.example.androidfinalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;
    FloatingActionButton captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        captureButton = findViewById(R.id.captureButton);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            // Launch the HistoryActivity
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

                    //Log.d("Parsed Info", "Vendor: " + vendor);
                    //Log.d("Parsed Info", "Date: " + date);
                    //Log.d("Parsed Info", "Total: " + total);

                    //Toast.makeText(MainActivity.this, "Vendor: " + vendor + "\nDate: " + date + "\nTotal: $" + total, Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(MainActivity.this, ReviewReceiptActivity.class);
                    intent.putExtra("vendor", vendor);
                    intent.putExtra("date", date);
                    intent.putExtra("total", total);
                    startActivity(intent);

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
