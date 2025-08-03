package com.example.androidfinalproject;
import android.content.Context;
import android.database.Cursor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReceiptDatabaseHelperTest {

    private ReceiptDatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new ReceiptDatabaseHelper(context);
        dbHelper.getWritableDatabase().delete("receipt_table", null, null); // clear table
    }

    @Test
    public void testInsertAndReadReceipt() {
        dbHelper.insertReceipt("Walmart", "2024-06-01", 42.50, "Food");

        Cursor cursor = dbHelper.readAllReceipts();
        assertTrue(cursor.moveToFirst());
        assertEquals("Walmart", cursor.getString(cursor.getColumnIndexOrThrow("vendor")));
        assertEquals("2024-06-01", cursor.getString(cursor.getColumnIndexOrThrow("receipt_date")));
        assertEquals(42.50, cursor.getDouble(cursor.getColumnIndexOrThrow("total")), 0.01);
        assertEquals("Food", cursor.getString(cursor.getColumnIndexOrThrow("category")));
        cursor.close();
    }

    @Test
    public void testUpdateReceipt() {
        dbHelper.insertReceipt("FreshCo", "2024-06-02", 10.00, "Food");

        Cursor cursor = dbHelper.readAllReceipts();
        assertTrue(cursor.moveToFirst());
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        cursor.close();

        dbHelper.updateReceipt(id, "FreshCo", "2024-06-03", 12.00, "Groceries");

        cursor = dbHelper.readAllReceipts();
        assertTrue(cursor.moveToFirst());
        assertEquals("2024-06-03", cursor.getString(cursor.getColumnIndexOrThrow("receipt_date")));
        assertEquals(12.00, cursor.getDouble(cursor.getColumnIndexOrThrow("total")), 0.01);
        assertEquals("Groceries", cursor.getString(cursor.getColumnIndexOrThrow("category")));
        cursor.close();
    }

    @Test
    public void testDeleteReceipt() {
        dbHelper.insertReceipt("Test", "2024-06-04", 5.00, "Misc");

        Cursor cursor = dbHelper.readAllReceipts();
        assertTrue(cursor.moveToFirst());
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        cursor.close();

        dbHelper.deleteReceipt(id);

        cursor = dbHelper.readAllReceipts();
        assertFalse(cursor.moveToFirst());
        cursor.close();
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }
}