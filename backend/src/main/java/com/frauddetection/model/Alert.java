package com.frauddetection.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_transaction", columnList = "transaction_id"),
    @Index(name = "idx_alert_status", columnList = "status"),
    @Index(name = "idx_alert_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
