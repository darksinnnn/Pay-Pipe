package com.paypipe.payment_gateway.controller;

import com.paypipe.payment_gateway.dto.PaymentRequest;
import com.paypipe.payment_gateway.service.PaymentService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/charge")
    public ResponseEntity <String> processPayment( @RequestHeader("Idempotency-key") String idempotencyKey, @Valid @RequestBody PaymentRequest request){

        System.out.println("Received request with Idempotency-Key" + idempotencyKey );

        //calling the paymentService

        String responseMessage = paymentService.processPayment(idempotencyKey,request.getUserId(),request.getAmount(),request.getCurrency());

        //if redis blocked it return with 409 conflict
        if(responseMessage.startsWith("DUPLICATE_REQUEST")){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessage);
        }

        if (responseMessage.startsWith("FAILED")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        }

        return ResponseEntity.ok(responseMessage);
    }
}
