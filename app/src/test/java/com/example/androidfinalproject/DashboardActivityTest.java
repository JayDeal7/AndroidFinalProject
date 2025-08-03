package com.example.androidfinalproject;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.github.mikephil.charting.charts.PieChart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DashboardActivityTest {

    private Context context;
    private ReceiptDatabaseHelper dbHelper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new ReceiptDatabaseHelper(context);
        dbHelper.getWritableDatabase().delete("receipt_table", null, null);
        dbHelper.insertReceipt("Walmart", "2024-06-01", 50.0, "Food");
        dbHelper.insertReceipt("Apple", "2024-06-02", 100.0, "Electronics");
    }

    @Test
    public void testPieChartIsPopulated() {
        ActivityController<DashboardActivity> controller = Robolectric.buildActivity(DashboardActivity.class).create().start().resume();
        DashboardActivity activity = controller.get();
        PieChart pieChart = activity.findViewById(R.id.pieChart);

        assertNotNull(pieChart.getData());
        assertTrue(pieChart.getData().getEntryCount() > 0);
    }
}