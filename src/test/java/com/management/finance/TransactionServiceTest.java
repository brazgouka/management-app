package com.management.finance;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    private final TransactionRepository repository = mock(TransactionRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);
    private final TransactionService service = new TransactionService(repository, clock);

    @Test
    void summaryDefaultsToCurrentMonth() {
        service.summary(null, null);

        verify(repository).totalsByCategory(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-28"));
    }

    @Test
    void savingsRateIsNullWithoutIncome() {
        when(repository.totalsByCategory(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-28"))).thenReturn(List.of(
                new CategoryTotal(TransactionType.EXPENSE, "food", new BigDecimal("42.00"), 1)));

        FinanceSummary summary = service.summary(null, null);

        assertThat(summary.totalIncome()).isEqualByComparingTo("0");
        assertThat(summary.balance()).isEqualByComparingTo("-42.00");
        assertThat(summary.savingsRate()).isNull();
    }

    @Test
    void savingsRateCanBeNegativeWhenOverspending() {
        when(repository.totalsByCategory(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-28"))).thenReturn(List.of(
                new CategoryTotal(TransactionType.INCOME, "salary", new BigDecimal("1000.00"), 1),
                new CategoryTotal(TransactionType.EXPENSE, "travel", new BigDecimal("1250.00"), 2)));

        FinanceSummary summary = service.summary(null, null);

        assertThat(summary.savingsRate()).isEqualByComparingTo("-25.0");
    }
}
