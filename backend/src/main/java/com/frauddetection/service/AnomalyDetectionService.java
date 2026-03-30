package com.frauddetection.service;

import com.frauddetection.model.Alert;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.AlertRepository;
import com.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;

    @Value("${anomaly.zscore.threshold:2.5}")
    private double zScoreThreshold;

    @Value("${anomaly.velocity.max-transactions-per-minute:5}")
    private int maxTransactionsPerMinute;

    @Value("${anomaly.amount.max-single-transaction:9500.00}")
    private double maxSingleTransactionAmount;

    @Value("${anomaly.geography.suspicious-countries:NG,RU,KP,IR}")
    private String suspiciousCountries;

    /**
     * Core anomaly detection pipeline.
     * Runs 4 independent detectors and aggregates risk scores.
     * Each detector contributes independently to the final risk score.
     */
    public AnomalyResult analyze(Transaction tx) {
        List<String> reasons = new ArrayList<>();
        double totalRisk = 0.0;

        // Rule 1: Z-Score amount anomaly (statistical outlier detection)
        double zScoreRisk = checkZScoreAnomaly(tx, reasons);
        totalRisk += zScoreRisk * 0.35;

        // Rule 2: Velocity check (transaction frequency abuse)
        double velocityRisk = checkVelocityAnomaly(tx, reasons);
        totalRisk += velocityRisk * 0.30;

        // Rule 3: Large amount structuring (just below reporting thresholds)
        double structuringRisk = checkStructuringAnomaly(tx, reasons);
        totalRisk += structuringRisk * 0.20;

        // Rule 4: Geography mismatch
        double geoRisk = checkGeographyAnomaly(tx, reasons);
        totalRisk += geoRisk * 0.15;

        double finalScore = Math.min(totalRisk, 1.0);
        boolean isFlagged = finalScore > 0.45 || !reasons.isEmpty();

        return new AnomalyResult(isFlagged, finalScore, reasons);
    }

    /**
     * Z-Score detection: flags transactions whose amount is statistically
     * unusual compared to this account's historical average.
     * Formula: z = (x - μ) / σ
     */
    private double checkZScoreAnomaly(Transaction tx, List<String> reasons) {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        Double avg = transactionRepository.findAvgAmountByAccountSince(tx.getAccountId(), thirtyDaysAgo);
        Double stdDev = transactionRepository.findStdDevAmountByAccountSince(tx.getAccountId(), thirtyDaysAgo);

        if (avg == null || stdDev == null || stdDev < 0.01) {
            // Fall back to global statistics for new accounts
            avg = transactionRepository.findGlobalAvgAmount();
            stdDev = transactionRepository.findGlobalStdDevAmount();
        }

        if (avg == null || stdDev == null || stdDev < 0.01) return 0.0;

        double amount = tx.getAmount().doubleValue();
        double zScore = Math.abs((amount - avg) / stdDev);

        if (zScore > zScoreThreshold) {
            reasons.add(String.format("Amount z-score=%.2f exceeds threshold %.1f (account avg=$%.2f)", zScore, zScoreThreshold, avg));
            return Math.min(zScore / 5.0, 1.0);
        }
        return 0.0;
    }

    /**
     * Velocity detection: flags accounts submitting too many transactions
     * in a short window — a common card-testing pattern.
     */
    private double checkVelocityAnomaly(Transaction tx, List<String> reasons) {
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);
        long recentCount = transactionRepository.countByAccountSince(tx.getAccountId(), oneMinuteAgo);

        if (recentCount >= maxTransactionsPerMinute) {
            reasons.add(String.format("Velocity alert: %d transactions in last 60 seconds (max=%d)", recentCount, maxTransactionsPerMinute));
            return Math.min((double) recentCount / maxTransactionsPerMinute, 1.0);
        }
        return 0.0;
    }

    /**
     * Structuring detection: amounts just below $10,000 reporting thresholds
     * are a known money-laundering pattern (structuring / smurfing).
     */
    private double checkStructuringAnomaly(Transaction tx, List<String> reasons) {
        double amount = tx.getAmount().doubleValue();
        if (amount >= maxSingleTransactionAmount && amount < 10000.0) {
            reasons.add(String.format("Structuring pattern: $%.2f is just below $10,000 reporting threshold", amount));
            return 0.85;
        }
        if (amount >= 10000.0) {
            reasons.add(String.format("High-value transaction: $%.2f exceeds $10,000", amount));
            return 0.60;
        }
        return 0.0;
    }

    /**
     * Geography anomaly: transactions from high-risk jurisdictions.
     */
    private double checkGeographyAnomaly(Transaction tx, List<String> reasons) {
        if (tx.getCountryCode() == null) return 0.0;
        Set<String> suspicious = new HashSet<>(Arrays.asList(suspiciousCountries.split(",")));
        if (suspicious.contains(tx.getCountryCode().toUpperCase())) {
            reasons.add(String.format("High-risk jurisdiction: %s", tx.getCountryCode()));
            return 0.70;
        }
        return 0.0;
    }

    /**
     * Creates and persists an Alert from a flagged transaction.
     */
    public Alert createAlert(Transaction tx, AnomalyResult result) {
        String severity = result.riskScore() > 0.75 ? "CRITICAL"
                : result.riskScore() > 0.55 ? "HIGH"
                : result.riskScore() > 0.35 ? "MEDIUM" : "LOW";

        String alertType = determineAlertType(result.reasons());

        Alert alert = Alert.builder()
                .transactionId(tx.getId())
                .accountId(tx.getAccountId())
                .alertType(alertType)
                .severity(severity)
                .description(String.join("; ", result.reasons()))
                .riskScore(result.riskScore())
                .status("OPEN")
                .build();

        return alertRepository.save(alert);
    }

    private String determineAlertType(List<String> reasons) {
        for (String r : reasons) {
            if (r.contains("Velocity")) return "VELOCITY_ABUSE";
            if (r.contains("Structuring") || r.contains("High-value")) return "STRUCTURING";
            if (r.contains("jurisdiction")) return "GEOGRAPHY_RISK";
            if (r.contains("z-score")) return "STATISTICAL_OUTLIER";
        }
        return "SUSPICIOUS_ACTIVITY";
    }

    public record AnomalyResult(boolean flagged, double riskScore, List<String> reasons) {}
}
