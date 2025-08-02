package com.example.androidfinalproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CalendarEntriesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_entries);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.calendarContainer, new CalendarEntriesFragment())
                    .commit();
        }
    }
}
