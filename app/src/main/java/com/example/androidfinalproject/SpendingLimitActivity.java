package com.example.androidfinalproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class SpendingLimitActivity extends AppCompatActivity {

    private EditText editLimit;
    private Button btnSave, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spending_limit);

        editLimit = findViewById(R.id.editMonthlyLimit);
        btnSave   = findViewById(R.id.btnSaveLimit);
        btnReset  = findViewById(R.id.btnResetLimit);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        float limit = prefs.getFloat("monthly_limit", 0f);
        if (limit > 0f) editLimit.setText(String.valueOf(limit));

        btnSave.setOnClickListener(v -> {
            String s = editLimit.getText().toString().trim();
            try {
                float val = Float.parseFloat(s);
                float old = prefs.getFloat("monthly_limit", 0f);
                prefs.edit().putFloat("monthly_limit", val).apply();
                Snackbar.make(findViewById(android.R.id.content),
                                "Limit saved: $" + val, Snackbar.LENGTH_LONG)
                        .setAction("Undo", a -> prefs.edit().putFloat("monthly_limit", old).apply())
                        .show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        btnReset.setOnClickListener(v -> {
            float old = prefs.getFloat("monthly_limit", 0f);
            prefs.edit().remove("monthly_limit").apply();
            editLimit.setText("");
            Snackbar.make(findViewById(android.R.id.content),
                            "Limit cleared", Snackbar.LENGTH_LONG)
                    .setAction("Undo", a -> {
                        prefs.edit().putFloat("monthly_limit", old).apply();
                        editLimit.setText(String.valueOf(old));
                    }).show();
        });
    }
}
