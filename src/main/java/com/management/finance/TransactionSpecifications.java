package com.management.finance;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    static Specification<Transaction> matching(TransactionType type, String category, LocalDate from, LocalDate to) {
        List<Specification<Transaction>> specs = new ArrayList<>();
        if (type != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (category != null && !category.isBlank()) {
            String normalized = category.trim().toLowerCase();
            specs.add((root, query, cb) -> cb.equal(cb.lower(root.get("category")), normalized));
        }
        if (from != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredOn"), from));
        }
        if (to != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredOn"), to));
        }
        return Specification.allOf(specs);
    }
}
