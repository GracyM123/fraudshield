package com.frauddetection.controller;

import com.frauddetection.dto.DashboardStats;
import com.frauddetection.dto.TransactionRequest;
import com.frauddetection.model.Alert;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.AlertRepository;
import com.frauddetection.repository.TransactionRepository;
import com.frauddetection.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> submit(@Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.ok(transactionService.processTransaction(req));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getRecent() {
        return ResponseEntity.ok(transactionRepository.findTop50ByOrderByCreatedAtDesc());
    }

    @GetMapping("/transactions/flagged")
    public ResponseEntity<List<Transaction>> getFlagged() {
        return ResponseEntity.ok(transactionRepository.findByFlaggedTrueOrderByCreatedAtDesc());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts() {
        return ResponseEntity.ok(alertRepository.findTop20ByOrderByCreatedAtDesc());
    }

    @GetMapping("/alerts/open")
    public ResponseEntity<List<Alert>> getOpenAlerts() {
        return ResponseEntity.ok(alertRepository.findByStatusOrderByCreatedAtDesc("OPEN"));
    }

    @PatchMapping("/alerts/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable String id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setStatus("RESOLVED");
            return ResponseEntity.ok(alertRepository.save(alert));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(transactionService.getDashboardStats());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "fraud-detection-api"));
    }
}
