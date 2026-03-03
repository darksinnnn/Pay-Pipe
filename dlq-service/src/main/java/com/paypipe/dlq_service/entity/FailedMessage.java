package com.paypipe.dlq_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "failed_messages")
public class FailedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String errorReason;
    private String originalTopic;
    private LocalDateTime failedAt;
    private String status; // e.g., "NEEDS_REVIEW", "RETRIED", "DISCARDED"

    public FailedMessage() {}

    public FailedMessage(String payload, String errorReason, String originalTopic) {
        this.payload = payload;
        this.errorReason = errorReason;
        this.originalTopic = originalTopic;
        this.failedAt = LocalDateTime.now();
        this.status = "NEEDS_REVIEW";
    }

    // Getters
    public Long getId() { return id; }
    public String getPayload() { return payload; }
    public String getErrorReason() { return errorReason; }
    public String getOriginalTopic() { return originalTopic; }
    public LocalDateTime getFailedAt() { return failedAt; }
    public String getStatus() { return status; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}