package com.paypipe.dlq_service.repository;

import com.paypipe.dlq_service.entity.FailedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedMessageRepository extends JpaRepository<FailedMessage, Long> {
}