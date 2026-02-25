package com.paypipe.payment_gateway.service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {
    @Autowired
    private StringRedisTemplate redisTemplate;

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

            //update redis for success
            redisTemplate.opsForValue().set(redisKey,"COMPLETED_INTENT_"+ paymentIntent.getId(),24,TimeUnit.SECONDS);

            return "SUCCESS!! Stripe Payment Intent Created. ID: " + paymentIntent.getId();

        }
        catch (StripeException e){
            //if stripe fails delete the redis lock
            //user can try clicking the button again.
            redisTemplate.delete(redisKey);
            return "FAILED - Stripe Error: " + e.getMessage();
        }
    }
}
