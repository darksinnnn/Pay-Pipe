package com.paypipe.fraud_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="fraud_records")
public class FraudRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String userId;
    private Double amount;
    private String status;
    private String reason;
    private LocalDateTime checkedAt;

    public FraudRecord() {}

    public FraudRecord(String transactionId, String userId, Double amount, String status, String reason) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.checkedAt = LocalDateTime.now();
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public String getUserId() { return userId; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public LocalDateTime getCheckedAt() { return checkedAt; }

    // --- NEW: Setters (Required for Admin Override) ---
    public void setStatus(String status) {
        this.status = status;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}