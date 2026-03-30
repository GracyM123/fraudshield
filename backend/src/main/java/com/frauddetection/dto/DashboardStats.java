package com.frauddetection.dto;

import java.util.Map;

public record DashboardStats(
    long totalTransactions,
    long flaggedTransactions,
    long openAlerts,
    long last24hTransactions,
    double flagRate,
    Map<String, Long> alertsBySeverity,
    Map<String, Long> alertsByType
) {}
