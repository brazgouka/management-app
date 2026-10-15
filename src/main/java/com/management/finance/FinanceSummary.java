package com.management.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aggregated view of a period: totals, balance and the share of income that was kept.
 *
 * @param savingsRate percentage of income not spent, or {@code null} when there was no income
 */
public record FinanceSummary(
        LocalDate from,
        LocalDate to,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance,
        BigDecimal savingsRate,
        List<CategoryTotal> categories) {
}
