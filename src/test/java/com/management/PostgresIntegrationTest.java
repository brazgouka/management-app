package com.management;

import com.management.finance.TransactionRepository;
import com.management.notes.NoteRepository;
import com.management.timer.TimeEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the Flyway migrations, Hibernate schema validation and the demo seed against a real PostgreSQL.
 * Skipped automatically when Docker is not available (it always runs in CI).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "app.demo-data.enabled=true")
class PostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    TransactionRepository transactions;
    @Autowired
    NoteRepository notes;
    @Autowired
    TimeEntryRepository timeEntries;

    @Test
    void migratesSeedsAndQueriesOnPostgres() {
        assertThat(transactions.count()).isPositive();
        assertThat(timeEntries.count()).isPositive();
        assertThat(notes.countByTag()).isNotEmpty();
        assertThat(transactions.totalsByCategory(LocalDate.now().minusMonths(2), LocalDate.now())).isNotEmpty();
    }
}
