package com.paypipe.ledger_service.service;

import com.paypipe.ledger_service.entity.LedgerTransaction;
import com.paypipe.ledger_service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {
    @Autowired
    private TransactionRepository transactionRepository;
    //append only-only adds money in or out
    public LedgerTransaction recordTransaction(String transactionId, String userId , Double amount , String type){
        LedgerTransaction transaction = new LedgerTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setUserId(userId);

        //if debit ensure , ensure the nuber is negative
        if("DEBIT".equalsIgnoreCase(type) && amount > 0 ){
            transaction.setAmount(amount * -1);
        }
        else{
            transaction.setAmount(amount);
        }
        transaction.setType(type.toUpperCase());
        return transactionRepository.save(transaction);
    }
    //balance calculator
    public Double getBalance(String userId){
        return transactionRepository.calculateBalanceForUser(userId);
    }

}
