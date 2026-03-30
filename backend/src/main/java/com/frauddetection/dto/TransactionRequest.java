package com.frauddetection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequest(
    @NotBlank String accountId,
    @NotNull @Positive BigDecimal amount,
    String currency,
    String merchantName,
    String merchantCategory,
    String countryCode,
    String city,
    String transactionType
) {}
