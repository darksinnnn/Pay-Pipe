package com.paypipe.payment_gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

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

            // KAFKA LOGIC
            System.out.println("Stripe Success! Publishing Event to Kafka...");

            Map<String, Object> ledgerPayload = new HashMap<>();
            ledgerPayload.put("transactionId", stripeId);
            ledgerPayload.put("userId", userId);
            ledgerPayload.put("amount", amount);
            ledgerPayload.put("type", "CREDIT");

            // Convert the Java Map into a JSON String for Kafka
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(ledgerPayload);

            // Publish message to Kafka Topic (NO DIRECT HTTP CALL TO LEDGER!)
            kafkaTemplate.send("payment-success-topic", jsonMessage);

            //update redis for success
            redisTemplate.opsForValue().set(redisKey,"COMPLETED_INTENT_"+ paymentIntent.getId(),24,TimeUnit.SECONDS);

            return "SUCCESS!! Stripe Payment Intent Created & Sent to Kafka. ID: " + paymentIntent.getId();

        }
        catch (Exception e){
            //if stripe fails delete the redis lock
            //user can try clicking the button again.
            redisTemplate.delete(redisKey);
            return "FAILED - Error: " + e.getMessage();
        }
    }
}