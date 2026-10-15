package com.management.finance;

import java.math.BigDecimal;

public record CategoryTotal(TransactionType type, String category, BigDecimal total, long count) {
}
