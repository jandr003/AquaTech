package com.example.aquatech;

public class WalletTransaction {
    private String transactionId;
    private String type; // e.g., "Credit" or "Debit"
    private double amount;
    private String description;
    private long timestamp;

    public WalletTransaction() {
        // Required for Firebase
    }

    public WalletTransaction(String transactionId, String type, double amount, String description, long timestamp) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getTransactionId() { return transactionId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
}
