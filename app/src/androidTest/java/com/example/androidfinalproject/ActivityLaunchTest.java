// File: app/src/test/java/com/example/androidfinalproject/ActivityLaunchTest.java
package com.example.androidfinalproject;

import android.content.Intent;
import android.widget.EditText;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActivityLaunchTest {

    @Test
    public void dashboardActivity_Launches() {
        try (ActivityScenario<DashboardActivity> scenario = ActivityScenario.launch(DashboardActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity.findViewById(R.id.pieChart)));
        }
    }

    @Test
    public void editReceiptActivity_Launches() {
        Intent intent = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                EditReceiptActivity.class
        );
        intent.putExtra("manual", true);
        try (ActivityScenario<EditReceiptActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> assertNotNull(activity.findViewById(R.id.editVendor)));
        }
    }

    @Test
    public void autoCategorizationActivity_Launches() {
        try (ActivityScenario<AutoCategorizationActivity> scenario = ActivityScenario.launch(AutoCategorizationActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void calendarEntriesActivity_Launches() {
        try (ActivityScenario<CalendarEntriesActivity> scenario = ActivityScenario.launch(CalendarEntriesActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }
}