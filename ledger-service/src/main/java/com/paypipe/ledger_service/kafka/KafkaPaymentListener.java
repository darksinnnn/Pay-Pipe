package com.paypipe.ledger_service.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypipe.ledger_service.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaPaymentListener {

    @Autowired
    private LedgerService ledgerService;

    // This method acts as a daemon. It is triggered AUTOMATICALLY the microsecond a message hits Kafka
    @KafkaListener(topics = "payment-success-topic", groupId = "ledger-group")
    public void consumePaymentEvent(String message) {
        System.out.println("📩 NEW KAFKA MESSAGE RECEIVED: " + message);

        try {
            // 1. Convert the JSON String from Kafka back into a Java Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});

            // 2. Extract the data exactly how we sent it from the Gateway
            String transactionId = payload.get("transactionId").toString();
            String userId = payload.get("userId").toString();
            Double amount = Double.valueOf(payload.get("amount").toString());
            String type = payload.get("type").toString();

            // 3. Save it to PostgreSQL using your existing Ledger Service!
            ledgerService.recordTransaction(transactionId, userId, amount, type);
            System.out.println("Successfully saved transaction to Vault: " + transactionId);

        } catch (Exception e) {
            System.err.println("Failed to process Kafka message: " + e.getMessage());
            // In an enterprise app, this is where we would send the failed message to a Dead Letter Queue (DLQ)
        }
    }
}