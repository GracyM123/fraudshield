package com.frauddetection.service;

import com.frauddetection.dto.TransactionRequest;
import com.frauddetection.dto.DashboardStats;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.AlertRepository;
import com.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final AnomalyDetectionService anomalyService;

    private static final String[] MERCHANTS = {
        "Amazon", "Walmart", "Shell Gas", "McDonald's", "Apple Store",
        "Best Buy", "Starbucks", "Target", "Home Depot", "Netflix",
        "Uber Eats", "Steam Games", "PayPal Transfer", "ATM Withdrawal",
        "Casino Online", "Wire Transfer Intl", "Crypto Exchange", "Jewelry Store"
    };

    private static final String[] CATEGORIES = {
        "RETAIL", "GAS_STATION", "FOOD", "ELECTRONICS", "STREAMING",
        "TRANSFER", "GAMBLING", "CRYPTO", "LUXURY", "ATM"
    };

    private static final String[] COUNTRIES = {
        "US", "CA", "GB", "DE", "FR", "AU", "US", "CA", "US", "CA",
        "US", "US", "NG", "RU", "US", "CA", "US", "CA", "KP", "US"
    };

    private static final String[] ACCOUNTS = {
        "ACC-1001", "ACC-1002", "ACC-1003", "ACC-1004", "ACC-1005",
        "ACC-1006", "ACC-1007", "ACC-1008"
    };

    @Transactional
    public Transaction processTransaction(TransactionRequest req) {
        Transaction tx = Transaction.builder()
                .accountId(req.accountId())
                .amount(req.amount())
                .currency(req.currency() != null ? req.currency() : "USD")
                .merchantName(req.merchantName())
                .merchantCategory(req.merchantCategory())
                .countryCode(req.countryCode())
                .city(req.city())
                .transactionType(req.transactionType() != null ? req.transactionType() : "PURCHASE")
                .build();

        AnomalyDetectionService.AnomalyResult result = anomalyService.analyze(tx);

        tx.setFlagged(result.flagged());
        tx.setRiskScore(result.riskScore());
        tx.setFlagReasons(String.join("; ", result.reasons()));

        Transaction saved = transactionRepository.save(tx);

        if (result.flagged()) {
            anomalyService.createAlert(saved, result);
            log.warn("FLAGGED transaction {} for account {} - risk={} reasons={}",
                    saved.getId(), saved.getAccountId(), result.riskScore(), result.reasons());
        }

        return saved;
    }

    /**
     * Simulates real-time transaction stream — generates 1-3 transactions every 4 seconds.
     * This makes the dashboard feel live without needing Kafka.
     */
    @Scheduled(fixedDelay = 4000)
    public void simulateLiveTransactions() {
        Random rng = new Random();
        int batch = 1 + rng.nextInt(3);
        for (int i = 0; i < batch; i++) {
            generateRandomTransaction(rng);
        }
    }

    private void generateRandomTransaction(Random rng) {
        String account = ACCOUNTS[rng.nextInt(ACCOUNTS.length)];
        String country = COUNTRIES[rng.nextInt(COUNTRIES.length)];

        // 8% chance of structuring amount (just below $10k)
        double amount;
        int roll = rng.nextInt(100);
        if (roll < 3) {
            amount = 9500 + rng.nextDouble() * 490; // structuring
        } else if (roll < 8) {
            amount = 10000 + rng.nextDouble() * 5000; // high value
        } else if (roll < 15) {
            amount = 500 + rng.nextDouble() * 2000; // elevated
        } else {
            amount = 5 + rng.nextDouble() * 350; // normal
        }

        TransactionRequest req = new TransactionRequest(
                account,
                BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP),
                "USD",
                MERCHANTS[rng.nextInt(MERCHANTS.length)],
                CATEGORIES[rng.nextInt(CATEGORIES.length)],
                country,
                "City",
                "PURCHASE"
        );

        try {
            processTransaction(req);
        } catch (Exception e) {
            log.error("Error generating transaction: {}", e.getMessage());
        }
    }

    public DashboardStats getDashboardStats() {
        long total = transactionRepository.count();
        long flagged = transactionRepository.countByFlaggedTrue();
        long openAlerts = alertRepository.countByStatus("OPEN");
        long last24hCount = transactionRepository.countByCreatedAtAfter(
                Instant.now().minus(24, ChronoUnit.HOURS));

        double flagRate = total > 0 ? (double) flagged / total * 100 : 0;

        Map<String, Long> alertsBySeverity = new LinkedHashMap<>();
        alertRepository.countBySeverity().forEach(row ->
                alertsBySeverity.put((String) row[0], (Long) row[1]));

        Map<String, Long> alertsByType = new LinkedHashMap<>();
        alertRepository.countByAlertType().forEach(row ->
                alertsByType.put((String) row[0], (Long) row[1]));

        return new DashboardStats(total, flagged, openAlerts, last24hCount,
                flagRate, alertsBySeverity, alertsByType);
    }
}
