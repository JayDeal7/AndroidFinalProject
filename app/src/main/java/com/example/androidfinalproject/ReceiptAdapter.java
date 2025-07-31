package com.example.androidfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {

    private List<Receipt> receiptList;

    public ReceiptAdapter(List<Receipt> receiptList) {
        this.receiptList = receiptList;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_row, parent, false);
        return new ReceiptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        Receipt receipt = receiptList.get(position);
        holder.vendorText.setText(receipt.vendor);
        holder.dateText.setText(receipt.date);
        holder.totalText.setText("$" + String.format("%.2f", receipt.total));
    }

    @Override
    public int getItemCount() {
        return receiptList.size();
    }

    public static class ReceiptViewHolder extends RecyclerView.ViewHolder {
        TextView vendorText, dateText, totalText;

        public ReceiptViewHolder(@NonNull View itemView) {
            super(itemView);
            vendorText = itemView.findViewById(R.id.vendorText);
            dateText = itemView.findViewById(R.id.dateText);
            totalText = itemView.findViewById(R.id.totalText);
        }
    }
}
