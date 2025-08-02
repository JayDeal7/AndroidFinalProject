package com.example.androidfinalproject;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarEntriesFragment extends Fragment {

    private ProgressBar progress;
    private ListView list;
    private TextView empty;
    private ArrayAdapter<String> adapter;
    private final ArrayList<ReceiptRow> rows = new ArrayList<>();

    private static class ReceiptRow {
        int id; String vendor; String date; double total; String category;
        @Override public String toString() {
            return vendor + "  •  " + date + "  •  $" + total + "  •  " + category;
        }
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_entries, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        progress = view.findViewById(R.id.calendarProgress);
        list     = view.findViewById(R.id.receiptsList);
        empty    = view.findViewById(R.id.emptyView);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);
        list.setEmptyView(empty);

        list.setOnItemClickListener((parent, v, position, id) -> showReminderDialog(rows.get(position)));

        list.setOnItemLongClickListener((parent, v, position, id) -> {
            ReceiptRow r = rows.get(position);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete receipt")
                    .setMessage("Delete " + r.vendor + " (" + r.date + ")?")
                    .setPositiveButton("Delete", (d, w) -> {
                        ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(requireContext());
                        db.deleteReceipt(r.id);
                        Snackbar.make(requireView(), "Deleted", Snackbar.LENGTH_SHORT).show();
                        loadReceipts();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        loadReceipts();
    }

    private void loadReceipts() {
        progress.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, ArrayList<ReceiptRow>>() {
            @Override protected ArrayList<ReceiptRow> doInBackground(Void... voids) {
                ArrayList<ReceiptRow> out = new ArrayList<>();
                ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(requireContext());
                Cursor c = db.readAllReceipts();
                if (c != null) {
                    while (c.moveToNext()) {
                        ReceiptRow r = new ReceiptRow();
                        r.id       = c.getInt(c.getColumnIndexOrThrow("_id"));
                        r.vendor   = c.getString(c.getColumnIndexOrThrow("vendor"));
                        r.date     = c.getString(c.getColumnIndexOrThrow("receipt_date"));
                        r.total    = c.getDouble(c.getColumnIndexOrThrow("total"));
                        r.category = c.getString(c.getColumnIndexOrThrow("category"));
                        out.add(r);
                    }
                    c.close();
                }
                return out;
            }
            @Override protected void onPostExecute(ArrayList<ReceiptRow> result) {
                rows.clear(); rows.addAll(result);
                adapter.clear();
                for (ReceiptRow r : rows) adapter.add(r.toString());
                progress.setVisibility(View.GONE);
                if (rows.isEmpty()) empty.setText("No receipts saved yet.\nScan and save to add calendar entries.");
            }
        }.execute();
    }

    private void showReminderDialog(ReceiptRow r) {
        View dlg = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_calendar_options, null, false);
        Spinner reminderSpinner = dlg.findViewById(R.id.spinnerReminder);
        EditText editDueDays    = dlg.findViewById(R.id.editDueDays);

        ArrayAdapter<CharSequence> remAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.reminder_minutes, android.R.layout.simple_spinner_item);
        remAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderSpinner.setAdapter(remAdapter);
        reminderSpinner.setSelection(2); // 60 default

        new AlertDialog.Builder(requireContext())
                .setTitle("Add to Calendar")
                .setMessage("Vendor: " + safe(r.vendor) + "\nDate: " + safe(r.date) +
                        "\nTotal: $" + r.total + "\nCategory: " + safe(r.category))
                .setView(dlg)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (d, which) -> {
                    int minutes = parseReminderMinutes(reminderSpinner.getSelectedItem().toString());
                    int dueDays = parseIntSafe(editDueDays.getText().toString(), 0);
                    addToCalendar(r, minutes, dueDays);
                })
                .show();
    }

    private void addToCalendar(ReceiptRow r, int reminderMinutes, int dueDays) {
        String title = "Purchase: " + safe(r.vendor) + (r.total > 0 ? " ($" + r.total + ")" : "");
        String desc  = "Category: " + safe(r.category);

        long startMillis = parseDateToMillis(r.date, 9, 0);
        if (dueDays > 0) {
            startMillis += dueDays * 24L * 60L * 60L * 1000L;
            title = "Payment Due: " + safe(r.vendor) + (r.total > 0 ? " ($" + r.total + ")" : "");
        }
        long endMillis = startMillis + 30 * 60 * 1000;

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, desc)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.HAS_ALARM, true)
                .putExtra(CalendarContract.Reminders.MINUTES, reminderMinutes)
                .putExtra(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

        try {
            startActivity(intent);
            Snackbar.make(requireView(), "Event prepared — remember to Save in Calendar", Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No calendar app found", Toast.LENGTH_SHORT).show();
        }
    }

    // Helpers
    private static String safe(String s) { return TextUtils.isEmpty(s) ? "" : s.trim(); }

    private static long parseDateToMillis(String dateStr, int hour, int minute) {
        String[] fmts = {"yyyy-MM-dd","yyyy/MM/dd","MM/dd/yyyy","dd/MM/yyyy","MM-dd-yyyy","dd-MM-yyyy","MMM d, yyyy","d MMM yyyy"};
        for (String f : fmts) {
            try {
                Date d = new SimpleDateFormat(f).parse(dateStr);
                if (d != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    return cal.getTimeInMillis();
                }
            } catch (Exception ignored) {}
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static int parseReminderMinutes(String s) {
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 60; }
    }
    private static int parseIntSafe(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } }
}
