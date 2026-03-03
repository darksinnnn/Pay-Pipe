package com.paypipe.fraud_service.repository;

import com.paypipe.fraud_service.entity.FraudRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FraudRecordRepository extends JpaRepository<FraudRecord, Long> {
    Optional<FraudRecord> findByTransactionId(String transactionId);
}