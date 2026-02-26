package com.paypipe.payment_gateway.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate; // Added this to make HTTP calls to Ledger

    @Value("${stripe.secret.key}")//fetching the key
    private String stripeSecretKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey=stripeSecretKey;
    }

    public String processPayment(String idempotencyKey,String userId,Double amount, String currency){
        String redisKey = "payment:idempotency:" + idempotencyKey;

        //Idempotency Check here
        Boolean isNewRequest = redisTemplate.opsForValue()
                .setIfAbsent(redisKey,"Processing",10, TimeUnit.MINUTES);

        if(Boolean.FALSE.equals(isNewRequest)){
            return "DUPLICATE_REQUEST - Transaction already in progress.";
        }

        try{
            System.out.println("Initiating real Transaction with Stripe...");

            long amountInCents = (long) (amount*100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .putMetadata("userId",userId)
                    .build();

            //make http call to stripe
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            String stripeId = paymentIntent.getId(); // Get the ID to send to Ledger

            // --- NEW: Send data to the Ledger Service ---
            System.out.println("Stripe Success! Sending data to Ledger Vault...");
            String ledgerUrl = "http://localhost:8081/api/ledger/record";

            Map<String, Object> ledgerPayload = new HashMap<>();
            ledgerPayload.put("transactionId", stripeId);
            ledgerPayload.put("userId", userId);
            ledgerPayload.put("amount", amount);
            ledgerPayload.put("type", "CREDIT");

            // Make POST request to Port 8081
            ResponseEntity<String> ledgerResponse = restTemplate.postForEntity(ledgerUrl, ledgerPayload, String.class);

            if (!ledgerResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Ledger Service failed to save data!");
            }
            // --------------------------------------------

            //update redis for success
            redisTemplate.opsForValue().set(redisKey,"COMPLETED_INTENT_"+ paymentIntent.getId(),24,TimeUnit.SECONDS);

            return "SUCCESS!! Stripe Payment Intent Created & Saved in Ledger. ID: " + paymentIntent.getId();

        }
        catch (Exception e){ // Changed to generic Exception so it catches Stripe AND Ledger errors
            //if stripe fails delete the redis lock
            //user can try clicking the button again.
            redisTemplate.delete(redisKey);
            return "FAILED - Error: " + e.getMessage();
        }
    }
}