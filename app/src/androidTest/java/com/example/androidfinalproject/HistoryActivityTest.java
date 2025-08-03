
package com.example.androidfinalproject;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import org.junit.*;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

@RunWith(AndroidJUnit4.class)
public class HistoryActivityTest {

    @Before
    public void setUp() {
        // Clear the database before each test
        ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(
                androidx.test.core.app.ApplicationProvider.getApplicationContext());
        db.getWritableDatabase().delete("receipt_table", null, null);
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        ActivityScenario.launch(HistoryActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.receiptRecyclerView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testAddManualButtonOpensEditReceiptActivity() {
        Intents.init();
        ActivityScenario.launch(HistoryActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.addManualButton))
                .perform(ViewActions.click());
        intended(hasComponent(EditReceiptActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void testReceiptAppearsInRecyclerView() {
        // Insert a test receipt
        ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(
                androidx.test.core.app.ApplicationProvider.getApplicationContext());
        db.insertReceipt("TestVendor", "2024-06-05", 12.34, "Food");

        ActivityScenario.launch(HistoryActivity.class);

        // Check if the vendor name appears in the list
        Espresso.onView(ViewMatchers.withText("TestVendor"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}