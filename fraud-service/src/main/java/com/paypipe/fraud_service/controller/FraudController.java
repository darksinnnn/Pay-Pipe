package com.paypipe.fraud_service.controller;

import com.paypipe.fraud_service.entity.FraudRecord;
import com.paypipe.fraud_service.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    @Autowired
    private FraudDetectionService fraudService;

    // Temporary endpoint to test logic before Kafka is connected
    @PostMapping("/check")
    public ResponseEntity<FraudRecord> checkFraud(@RequestBody Map<String, Object> payload) {
        String transactionId = payload.get("transactionId").toString();
        String userId = payload.get("userId").toString();
        Double amount = Double.valueOf(payload.get("amount").toString());

        FraudRecord result = fraudService.evaluateTransaction(transactionId, userId, amount);

        return ResponseEntity.ok(result);
    }

    //  Manual Approval Endpoint
    @PutMapping("/{transactionId}/approve")
    public ResponseEntity<String> manuallyApproveTransaction(@PathVariable String transactionId) {
        fraudService.updateFraudStatus(transactionId, "APPROVED", "Manually approved by Admin/Support");
        return ResponseEntity.ok("Transaction " + transactionId + " has been manually APPROVED.");
    }

    // Manual Rejection Endpoint ---
    @PutMapping("/{transactionId}/reject")
    public ResponseEntity<String> manuallyRejectTransaction(@PathVariable String transactionId) {
        fraudService.updateFraudStatus(transactionId, "REJECTED", "Manually rejected by Admin/Support");
        return ResponseEntity.ok("Transaction " + transactionId + " has been manually REJECTED.");
    }
}