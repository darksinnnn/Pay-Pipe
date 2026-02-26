package com.paypipe.ledger_service.controller;

import com.paypipe.ledger_service.entity.LedgerTransaction;
import com.paypipe.ledger_service.service.LedgerService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ledger")
public class LedgeController {

    @Autowired
    private LedgerService ledgerService;

    @PostMapping("/record")
    public ResponseEntity<LedgerTransaction> recordTransaction(@RequestBody Map<String, Object> payload){

        String transactionId = payload.get("transactionId").toString();
        String userId = payload.get("userId").toString();
        Double amount = Double.valueOf(payload.get("amount").toString());
        String type =payload.get("type").toString();
        LedgerTransaction savedTransaction =ledgerService.recordTransaction(transactionId,userId,amount,type);

        return ResponseEntity.ok(savedTransaction);
    }

    //get user's balance
    @GetMapping("balance/{userId}")
    public ResponseEntity<Double> getaBalance(@PathVariable String userId){
        Double balance = ledgerService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

}
