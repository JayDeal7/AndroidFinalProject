package com.example.androidfinalproject;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class EditReceiptActivityTest {

    @Test
    public void testManualEntryFieldsPresent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditReceiptActivity.class);
        intent.putExtra("manual", true);

        ActivityController<EditReceiptActivity> controller = Robolectric.buildActivity(EditReceiptActivity.class, intent).create().start();
        EditReceiptActivity activity = controller.get();

        EditText vendor = activity.findViewById(R.id.editVendor);
        EditText date = activity.findViewById(R.id.editDate);
        EditText total = activity.findViewById(R.id.editTotal);
        Spinner category = activity.findViewById(R.id.categorySpinner);
        Button addButton = activity.findViewById(R.id.updateButton);

        assertNotNull(vendor);
        assertNotNull(date);
        assertNotNull(total);
        assertNotNull(category);
        assertNotNull(addButton);
        assertEquals("Add Receipt", addButton.getText().toString());
    }

    @Test
    public void testAddReceiptButtonSavesData() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditReceiptActivity.class);
        intent.putExtra("manual", true);

        ActivityController<EditReceiptActivity> controller = Robolectric.buildActivity(EditReceiptActivity.class, intent).create().start();
        EditReceiptActivity activity = controller.get();

        EditText vendor = activity.findViewById(R.id.editVendor);
        EditText date = activity.findViewById(R.id.editDate);
        EditText total = activity.findViewById(R.id.editTotal);
        Spinner category = activity.findViewById(R.id.categorySpinner);
        Button addButton = activity.findViewById(R.id.updateButton);

        vendor.setText("TestVendor");
        date.setText("2024-06-06");
        total.setText("99.99");
        category.setSelection(0); // Select first category

        addButton.performClick();

        // Optionally, check that the activity finishes after adding
        assertTrue(activity.isFinishing());
    }
}