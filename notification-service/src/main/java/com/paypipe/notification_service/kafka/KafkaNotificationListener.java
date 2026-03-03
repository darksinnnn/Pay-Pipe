package com.paypipe.notification_service.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypipe.notification_service.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaNotificationListener {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "payment-success-topic", groupId = "notification-group")
    public void consumePaymentEvent(String message) {
        try {
            // Convert Kafka JSON back to a Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});

            String transactionId = payload.get("transactionId").toString();
            String userId = payload.get("userId").toString();
            Double amount = Double.valueOf(payload.get("amount").toString());

            // Trigger the email!
            emailService.sendPaymentReceipt(userId, transactionId, amount);

        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}