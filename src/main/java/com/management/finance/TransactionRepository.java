package com.management.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("""
            select new com.management.finance.CategoryTotal(t.type, t.category, sum(t.amount), count(t))
            from Transaction t
            where t.occurredOn between :from and :to
            group by t.type, t.category
            order by sum(t.amount) desc
            """)
    List<CategoryTotal> totalsByCategory(LocalDate from, LocalDate to);
}
