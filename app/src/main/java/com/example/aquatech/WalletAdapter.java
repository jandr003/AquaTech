package com.example.aquatech;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.TransactionViewHolder> {

    private List<WalletTransaction> transactionList;

    public WalletAdapter(List<WalletTransaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        WalletTransaction transaction = transactionList.get(position);
        holder.tvDescription.setText(transaction.getDescription());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(transaction.getTimestamp())));

        String amountPrefix = transaction.getType().equals("Credit") ? "+" : "-";
        holder.tvAmount.setText(amountPrefix + "₱" + String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));
        
        if (transaction.getType().equals("Credit")) {
            holder.tvAmount.setTextColor(Color.parseColor("#00796B")); // Teal 700
        } else {
            holder.tvAmount.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
