package com.example.androidfinalproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AutoCategorizationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_categorization_host);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.autoCatContainer, new AutoCategorizationFragment())
                    .commit();
        }
    }
}
