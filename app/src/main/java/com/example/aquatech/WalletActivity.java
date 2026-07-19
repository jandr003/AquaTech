package com.example.aquatech;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity {

    private TextView tvBalance;
    private ProgressBar progressBar;
    private WalletAdapter adapter;
    private List<WalletTransaction> transactionList;
    
    private final String DB_URL = "https://aquatech-8da99c74-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        tvBalance = findViewById(R.id.tvBalance);
        RecyclerView rvTransactions = findViewById(R.id.rvTransactions);
        progressBar = findViewById(R.id.progressBar);
        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();

        if (uid == null) {
            finish();
            return;
        }

        DatabaseReference walletRef = FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("walletBalance");
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance(DB_URL).getReference("WalletTransactions").child(uid);

        transactionList = new ArrayList<>();
        adapter = new WalletAdapter(transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        fetchWalletData(walletRef, transactionsRef);
    }

    private void fetchWalletData(DatabaseReference walletRef, DatabaseReference transactionsRef) {
        progressBar.setVisibility(View.VISIBLE);

        // Fetch Balance
        walletRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                Double balance = snapshot.getValue(Double.class);
                if (balance == null) balance = 0.0;
                tvBalance.setText(String.format(Locale.getDefault(), "₱%.2f", balance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing()) {
                    Toast.makeText(WalletActivity.this, "Failed to load balance", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Fetch Transactions
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing()) return;
                transactionList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    WalletTransaction transaction = ds.getValue(WalletTransaction.class);
                    if (transaction != null) {
                        transactionList.add(transaction);
                    }
                }
                Collections.reverse(transactionList); // Show latest first
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WalletActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
