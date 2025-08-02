package com.example.androidfinalproject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

public class AutoCategorizationFragment extends Fragment {

    private static final int REQ_OCR = 901;

    private ProgressBar listProgress;
    private EditText editVendor, editDate, editTotal;
    private Spinner categorySpinner;
    private Button btnRescan, btnSave;
    private ListView recentList;

    private ArrayAdapter<String> listAdapter;
    private final ArrayList<Row> rows = new ArrayList<>();

    private static class Row {
        int id; String vendor, date, category; double total;
        @Override public String toString() {
            return vendor + " • " + date + " • $" + total + " • " + category;
        }
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auto_categorization, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        listProgress    = v.findViewById(R.id.auto_list_progress);
        editVendor      = v.findViewById(R.id.editVendor);
        editDate        = v.findViewById(R.id.editDate);
        editTotal       = v.findViewById(R.id.editTotal);
        categorySpinner = v.findViewById(R.id.categorySpinner);
        btnRescan       = v.findViewById(R.id.btnRescan);
        btnSave         = v.findViewById(R.id.btnSave);
        recentList      = v.findViewById(R.id.recentList);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.receipt_categories, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(catAdapter);

        listAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        recentList.setAdapter(listAdapter);

        recentList.setOnItemClickListener((parent, view, position, id) -> {
            Row r = rows.get(position);
            Intent i = new Intent(requireContext(), EditReceiptActivity.class);
            i.putExtra("manual", false);
            i.putExtra("id", r.id);
            i.putExtra("vendor", r.vendor);
            i.putExtra("date", r.date);
            i.putExtra("total", r.total);
            i.putExtra("category", r.category);
            startActivity(i);
        });

        recentList.setOnItemLongClickListener((parent, view, position, id) -> {
            Row r = rows.get(position);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete receipt")
                    .setMessage("Delete " + r.vendor + " (" + r.date + ")?")
                    .setPositiveButton("Delete", (d, w) -> {
                        ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(requireContext());
                        db.deleteReceipt(r.id);
                        loadRecent();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        btnRescan.setOnClickListener(v1 -> startScan());
        btnSave.setOnClickListener(v12 -> saveNow());

        startScan();    // open camera immediately
        loadRecent();   // and load recent list
    }

    private void startScan() {
        setBusy(true);
        clearFields();

        Intent i = new Intent(requireContext(), CameraActivity.class);
        i.putExtra("mode", "OCR");
        i.putExtra("return_result", true); // <<< important
        startActivityForResult(i, REQ_OCR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setBusy(false);
        if (requestCode != REQ_OCR) return;

        if (resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(requireContext(), "Scan canceled", Toast.LENGTH_SHORT).show();
            return;
        }
        String ocrText = data.getStringExtra("ocr_text");
        if (TextUtils.isEmpty(ocrText)) {
            Toast.makeText(requireContext(), "No text detected", Toast.LENGTH_SHORT).show();
            return;
        }

        String vendor = ReceiptParser.extractVendor(ocrText);
        String date   = ReceiptParser.extractDate(ocrText);
        String totalS = ReceiptParser.extractTotal(ocrText);

        editVendor.setText(vendor);
        editDate.setText(date);
        editTotal.setText(totalS);

        String suggested = CategorySuggester.suggest(vendor, ocrText);
        if (TextUtils.isEmpty(suggested)) suggested = "Uncategorized";

        @SuppressWarnings("unchecked")
        ArrayAdapter<CharSequence> a = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
        int pos = a.getPosition(suggested);
        if (pos >= 0) categorySpinner.setSelection(pos);

        Snackbar.make(requireView(),
                        "Suggested category: " + suggested + ". Keep?",
                        Snackbar.LENGTH_LONG)
                .setAction("Change", v -> categorySpinner.performClick())
                .show();
    }

    private void saveNow() {
        String vendor = editVendor.getText().toString().trim();
        String date   = editDate.getText().toString().trim();
        String totalS = editTotal.getText().toString().trim();
        String cat    = categorySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(vendor) || TextUtils.isEmpty(totalS)) {
            Toast.makeText(requireContext(), "Please enter Vendor and Total", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(date)) {
            date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(new java.util.Date());
            editDate.setText(date);
        }

        double total;
        try {
            total = Double.parseDouble(totalS.replaceAll("[^0-9.\\-]", ""));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Total must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(requireContext());
        db.insertReceipt(vendor, date, total, cat); // triggers limit alert

        Toast.makeText(requireContext(), "Saved. Open Dashboard to see totals.", Toast.LENGTH_SHORT).show();
        clearFields();
        loadRecent();
    }

    private void loadRecent() {
        listProgress.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, ArrayList<Row>>() {
            @Override protected ArrayList<Row> doInBackground(Void... voids) {
                ArrayList<Row> out = new ArrayList<>();
                ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(requireContext());
                Cursor c = db.readAllReceipts();
                if (c != null) {
                    if (c.moveToLast()) {
                        int count = 0;
                        do {
                            Row r = new Row();
                            r.id = c.getInt(c.getColumnIndexOrThrow("_id"));
                            r.vendor = c.getString(c.getColumnIndexOrThrow("vendor"));
                            r.date = c.getString(c.getColumnIndexOrThrow("receipt_date"));
                            r.total = c.getDouble(c.getColumnIndexOrThrow("total"));
                            r.category = c.getString(c.getColumnIndexOrThrow("category"));
                            out.add(r);
                            count++;
                        } while (count < 5 && c.moveToPrevious());
                    }
                    c.close();
                }
                return out;
            }
            @Override protected void onPostExecute(ArrayList<Row> result) {
                rows.clear();
                rows.addAll(result);
                listAdapter.clear();
                for (Row r : rows) listAdapter.add(r.toString());
                listProgress.setVisibility(View.GONE);
            }
        }.execute();
    }

    private void clearFields() {
        editVendor.setText("");
        editDate.setText("");
        editTotal.setText("");
        categorySpinner.setSelection(0);
    }

    private void setBusy(boolean busy) {
        btnRescan.setEnabled(!busy);
        btnSave.setEnabled(!busy);
        editVendor.setEnabled(!busy);
        editDate.setEnabled(!busy);
        editTotal.setEnabled(!busy);
        categorySpinner.setEnabled(!busy);
    }
}
