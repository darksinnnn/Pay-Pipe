package com.paypipe.fraud_service.service;

import com.paypipe.fraud_service.entity.FraudRecord;
import com.paypipe.fraud_service.repository.FraudRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudDetectionService {

    @Autowired
    private FraudRecordRepository repository;

    // A mock blacklist of suspicious users
    private final List<String> BLACKLISTED_USERS = List.of("ashish", "vinut", "nancy");

    public FraudRecord evaluateTransaction(String transactionId, String userId, Double amount) {
        System.out.println("Fraud Engine scanning transaction: " + transactionId);

        String status = "APPROVED";
        String reason = "Passes all security checks.";

        //Blacklist Check
        if (BLACKLISTED_USERS.contains(userId.toLowerCase())) {
            status = "REJECTED";
            reason = "User is on the international watchlist.";
        }
        // High Value Check kind of money laundering
        else if (amount > 100000.00) {
            status = "FLAGGED";
            reason = "Transaction exceeds $100,000. Manual review required.";
        }

        // Save result to fraud db
        FraudRecord record = new FraudRecord(transactionId, userId, amount, status, reason);
        repository.save(record);

        System.out.println("Result: " + status + " | Reason: " + reason);
        return record;
    }

    // --- NEW: Added this method to handle manual admin approvals/rejections ---
    public void updateFraudStatus(String transactionId, String newStatus, String newReason) {
        // 1. Find the flagged transaction
        FraudRecord record = repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found!"));

        // 2. We only allow overrides if it is currently FLAGGED
        if (!"FLAGGED".equals(record.getStatus())) {
            throw new RuntimeException("Only FLAGGED transactions can be manually overridden!");
        }

        // 3. Update the status and reason
        record.setStatus(newStatus);
        record.setReason(newReason);
        repository.save(record);

        System.out.println("👨‍💻 Admin Override: Transaction " + transactionId + " is now " + newStatus);
    }
}