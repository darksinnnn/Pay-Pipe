package com.paypipe.ledger_service.repository;

import com.paypipe.ledger_service.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<LedgerTransaction, Long> {
    //coalesce when no transaction then return 0
    //  use 0.0 to prevent Integer/Double conversion errors
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM LedgerTransaction t WHERE t.userId = :userId")
    Double calculateBalanceForUser(@Param("userId") String userId);
}