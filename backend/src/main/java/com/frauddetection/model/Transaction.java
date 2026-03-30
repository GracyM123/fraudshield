package com.frauddetection.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_is_flagged", columnList = "is_flagged")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_category")
    private String merchantCategory;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "city")
    private String city;

    @Column(name = "transaction_type", length = 20)
    private String transactionType;

    @Column(name = "is_flagged")
    private boolean flagged;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "flag_reasons", columnDefinition = "TEXT")
    private String flagReasons;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
