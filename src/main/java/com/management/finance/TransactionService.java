package com.management.finance;

import com.management.common.BadRequestException;
import com.management.common.NotFoundException;
import com.management.common.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TransactionRepository repository;
    private final Clock clock;

    public TransactionService(TransactionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public PageResponse<TransactionResponse> search(TransactionType type, String category, LocalDate from, LocalDate to,
                                                    Pageable pageable) {
        requireValidRange(from, to);
        var spec = TransactionSpecifications.matching(type, category, from, to);
        return PageResponse.from(repository.findAll(spec, pageable).map(TransactionResponse::from));
    }

    public TransactionResponse get(Long id) {
        return TransactionResponse.from(find(id));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        var transaction = new Transaction(request.description().trim(), request.amount(), request.type(),
                normalizeCategory(request.category()), request.occurredOn());
        return TransactionResponse.from(repository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        var transaction = find(id);
        transaction.update(request.description().trim(), request.amount(), request.type(),
                normalizeCategory(request.category()), request.occurredOn());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    /**
     * Summarizes a date range (inclusive). Defaults to the current calendar month.
     */
    public FinanceSummary summary(LocalDate from, LocalDate to) {
        YearMonth currentMonth = YearMonth.now(clock);
        LocalDate start = from != null ? from : currentMonth.atDay(1);
        LocalDate end = to != null ? to : currentMonth.atEndOfMonth();
        requireValidRange(start, end);

        List<CategoryTotal> categories = repository.totalsByCategory(start, end);
        BigDecimal income = sumOf(categories, TransactionType.INCOME);
        BigDecimal expenses = sumOf(categories, TransactionType.EXPENSE);
        BigDecimal balance = income.subtract(expenses);
        BigDecimal savingsRate = income.signum() > 0
                ? balance.multiply(HUNDRED).divide(income, 1, RoundingMode.HALF_UP)
                : null;

        return new FinanceSummary(start, end, income, expenses, balance, savingsRate, categories);
    }

    private Transaction find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Transaction", id));
    }

    private static BigDecimal sumOf(List<CategoryTotal> categories, TransactionType type) {
        return categories.stream()
                .filter(total -> total.type() == type)
                .map(CategoryTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizeCategory(String category) {
        return category.trim().toLowerCase();
    }

    private static void requireValidRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
    }
}
