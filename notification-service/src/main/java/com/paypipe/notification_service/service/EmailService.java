package com.paypipe.notification_service.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendPaymentReceipt(String userId, String transactionId, Double amount) {
        System.out.println("\n========================================");
        System.out.println("SENDING EMAIL TO: " + userId + "@paypipe.com");
        System.out.println("SUBJECT: Payment Receipt - " + transactionId);
        System.out.println("BODY:");
        System.out.println(" Dear " + userId + ",");
        System.out.println("  We have successfully processed your payment of $" + amount + ".");
        System.out.println("  Thank you for using PayPipe!");
        System.out.println("========================================\n");
    }
}