package com.management.demo;

import com.management.finance.TransactionRepository;
import com.management.notes.NoteRepository;
import com.management.timer.TimeEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:demo-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
})
class DemoDataLoaderTest {

    @Autowired
    TransactionRepository transactions;
    @Autowired
    NoteRepository notes;
    @Autowired
    TimeEntryRepository timeEntries;

    @Test
    void seedsEveryModuleOnStartup() {
        assertThat(transactions.count()).isPositive();
        assertThat(notes.count()).isPositive();
        assertThat(timeEntries.count()).isPositive();
    }
}
