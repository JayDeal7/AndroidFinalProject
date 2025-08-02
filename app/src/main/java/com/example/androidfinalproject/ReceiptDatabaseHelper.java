package com.example.androidfinalproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReceiptDatabaseHelper extends SQLiteOpenHelper {

    private final Context context;

    private static final String DATABASE_NAME = "ScanExpense.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "receipt_table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_VENDOR = "vendor";
    private static final String COLUMN_DATE = "receipt_date";
    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_CATEGORY = "category";

    private static final String CHANNEL_ID = "spending_alerts";
    private static final int NOTIF_ID = 1001;

    public ReceiptDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext(); // use app context for notifications
        ensureChannel();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_VENDOR + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TOTAL + " REAL, " +
                COLUMN_CATEGORY + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ---- CRUD ----
    public void addReceipt(String vendor, String date, double total, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_VENDOR, vendor);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TOTAL, total);
        cv.put(COLUMN_CATEGORY, category);
        db.insert(TABLE_NAME, null, cv);
        // keep UI toasts to caller screens if they want; not required here
    }

    /** Call this everywhere so we always evaluate the limit after saves. */
    public void insertReceipt(String vendor, String date, double total, String category) {
        addReceipt(vendor, date, total, category);
        checkSpendingLimit(); // may notify
    }

    public Cursor readAllReceipts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public void updateReceipt(int id, String vendor, String date, double total, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_VENDOR, vendor);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TOTAL, total);
        cv.put(COLUMN_CATEGORY, category);
        db.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        checkSpendingLimit();
    }

    public void deleteReceipt(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ---- Spending limit logic ----
    private void checkSpendingLimit() {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        float limit = prefs.getFloat("monthly_limit", 0f);
        if (limit <= 0f) return;

        double monthlyTotal = getCurrentMonthTotalRobust();
        if (monthlyTotal > limit) {
            String title = "Spending Limit Exceeded";
            String message = "Spent $" + round2(monthlyTotal) + " this month (limit $" + round2(limit) + ").";

            // Show a system notification so it appears even if the caller Activity finishes
            postNotification(title, message);

            // Optional: small toast as a backup (on main thread)
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            );
        }
    }

    /** Exposed for debugging if needed. */
    public double debugGetCurrentMonthTotal() { return getCurrentMonthTotalRobust(); }

    private double getCurrentMonthTotalRobust() {
        double total = 0.0;

        Calendar now = Calendar.getInstance();
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH); // 0-based

        SimpleDateFormat[] fmts = new SimpleDateFormat[] {
                new SimpleDateFormat("yyyy-MM-dd", Locale.US),
                new SimpleDateFormat("yyyy/MM/dd", Locale.US),
                new SimpleDateFormat("MM/dd/yyyy", Locale.US),
                new SimpleDateFormat("dd/MM/yyyy", Locale.US),
                new SimpleDateFormat("MM-dd-yyyy", Locale.US),
                new SimpleDateFormat("dd-MM-yyyy", Locale.US),
                new SimpleDateFormat("MMM d, yyyy", Locale.US),
                new SimpleDateFormat("d MMM yyyy", Locale.US)
        };
        for (SimpleDateFormat f : fmts) f.setLenient(true);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COLUMN_DATE + ", " + COLUMN_TOTAL + " FROM " + TABLE_NAME, null);
        if (c != null) {
            while (c.moveToNext()) {
                String dStr = c.getString(0);
                double amt = c.getDouble(1);

                Date parsed = tryParseDateFlexible(dStr, fmts);
                if (parsed == null) continue;

                Calendar cal = Calendar.getInstance();
                cal.setTime(parsed);
                if (cal.get(Calendar.YEAR) == curYear && cal.get(Calendar.MONTH) == curMonth) {
                    total += amt;
                }
            }
            c.close();
        }
        return total;
    }

    private static Date tryParseDateFlexible(String s, SimpleDateFormat[] fmts) {
        if (s == null) return null;
        s = s.trim();
        for (SimpleDateFormat f : fmts) {
            try { Date d = f.parse(s); if (d != null) return d; } catch (ParseException ignored) {}
        }
        String s2 = s.replace('-', '/');
        if (!s2.equals(s)) {
            for (SimpleDateFormat f : fmts) {
                try { Date d = f.parse(s2); if (d != null) return d; } catch (ParseException ignored) {}
            }
        }
        return null;
    }

    private static String round2(double v) { return String.format(Locale.US, "%.2f", v); }

    // ---- Notification helpers ----
    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Spending Alerts", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Notifications when your monthly spending exceeds the limit.");
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }

    private void postNotification(String title, String message) {
        // Tap notification opens Dashboard (change to MainActivity if you prefer)
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= 31
                        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // or R.mipmap.ic_launcher
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(NOTIF_ID, b.build());
    }
}
