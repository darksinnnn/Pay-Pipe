package com.paypipe.ledger_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name="transactions")
@Data
@NoArgsConstructor

public class LedgerTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //primary key (auto incrementing)

    @Column(nullable = false,unique = true) //stripe id must be unique
    private String transactionId;

    @Column(nullable=false)
    private String userId;

    @Column(nullable=false)
    private double amount;

    @Column(nullable = false)
    private String type;

    @CreationTimestamp
    @Column(nullable=false)
    private LocalDateTime timestamp;
}
