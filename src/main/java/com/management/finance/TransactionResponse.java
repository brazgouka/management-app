package com.management.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate occurredOn) {

    static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getDescription(), transaction.getAmount(),
                transaction.getType(), transaction.getCategory(), transaction.getOccurredOn());
    }
}
