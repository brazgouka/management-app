package com.management.demo;

import com.management.finance.Transaction;
import com.management.finance.TransactionRepository;
import com.management.finance.TransactionType;
import com.management.notes.Note;
import com.management.notes.NoteRepository;
import com.management.timer.Focus;
import com.management.timer.TimeEntry;
import com.management.timer.TimeEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Seeds an empty database with realistic sample data so the dashboard has something to show on first run.
 * Disable with {@code app.demo-data.enabled=false}.
 */
@Component
@ConditionalOnBooleanProperty("app.demo-data.enabled")
class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final TransactionRepository transactions;
    private final NoteRepository notes;
    private final TimeEntryRepository timeEntries;
    private final Clock clock;

    DemoDataLoader(TransactionRepository transactions, NoteRepository notes, TimeEntryRepository timeEntries, Clock clock) {
        this.transactions = transactions;
        this.notes = notes;
        this.timeEntries = timeEntries;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (transactions.count() > 0 || notes.count() > 0 || timeEntries.count() > 0) {
            return;
        }
        seedTransactions();
        seedNotes();
        seedTimeEntries();
        log.info("Seeded demo data ({} transactions, {} notes, {} time entries)",
                transactions.count(), notes.count(), timeEntries.count());
    }

    private void seedTransactions() {
        LocalDate thisMonth = LocalDate.now(clock).withDayOfMonth(1);
        for (LocalDate month : new LocalDate[]{thisMonth.minusMonths(1), thisMonth}) {
            transaction("Monthly salary", "4200.00", TransactionType.INCOME, "salary", month);
            transaction("Freelance landing page", "650.00", TransactionType.INCOME, "freelance", month.plusDays(9));
            transaction("Rent", "1350.00", TransactionType.EXPENSE, "housing", month);
            transaction("Groceries", "86.40", TransactionType.EXPENSE, "food", month.plusDays(2));
            transaction("Groceries", "112.15", TransactionType.EXPENSE, "food", month.plusDays(12));
            transaction("Electricity & internet", "94.99", TransactionType.EXPENSE, "utilities", month.plusDays(4));
            transaction("Gym membership", "39.00", TransactionType.EXPENSE, "health", month.plusDays(5));
            transaction("Dinner with friends", "58.70", TransactionType.EXPENSE, "leisure", month.plusDays(7));
            transaction("Index fund", "500.00", TransactionType.EXPENSE, "investments", month.plusDays(1));
        }
    }

    private void transaction(String description, String amount, TransactionType type, String category, LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        transactions.save(new Transaction(description, new BigDecimal(amount), type, category,
                date.isAfter(today) ? today : date));
    }

    private void seedNotes() {
        Note welcome = new Note("Welcome 👋",
                "This dashboard tracks money, notes and time. Everything here is demo data, so feel free to edit or delete it.",
                Set.of("getting-started"));
        welcome.setPinned(true);
        notes.save(welcome);
        notes.save(new Note("Book list",
                "Designing Data-Intensive Applications\nThe Pragmatic Programmer\nA Philosophy of Software Design",
                Set.of("reading", "learning")));
        notes.save(new Note("Weekend ideas", "Hike the ridge trail, try the new ramen place, call grandma.",
                Set.of("personal")));
        notes.save(new Note("Spring Boot 4 notes",
                "Starters are modular now (spring-boot-starter-webmvc, spring-boot-starter-flyway). Jackson 3 lives in tools.jackson.",
                Set.of("learning", "java")));
    }

    private void seedTimeEntries() {
        Instant today = LocalDate.now(clock).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MINUTES);
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            Instant day = today.minus(Duration.ofDays(daysAgo));
            entry("Deep work: side project", Focus.PRODUCTIVE, day.plus(Duration.ofHours(9)), Duration.ofMinutes(110 + daysAgo * 7), now);
            entry("Reading tech articles", Focus.PRODUCTIVE, day.plus(Duration.ofHours(12)), Duration.ofMinutes(35), now);
            entry("Social media scrolling", Focus.PROCRASTINATION, day.plus(Duration.ofHours(13)), Duration.ofMinutes(20 + (daysAgo % 3) * 15), now);
            entry("Workout", Focus.PRODUCTIVE, day.plus(Duration.ofHours(18)), Duration.ofMinutes(50), now);
            entry("Video rabbit hole", Focus.PROCRASTINATION, day.plus(Duration.ofHours(21)), Duration.ofMinutes(25 + daysAgo * 4), now);
        }
    }

    private void entry(String activity, Focus focus, Instant start, Duration length, Instant now) {
        Instant end = start.plus(length);
        if (end.isAfter(now)) {
            return;
        }
        timeEntries.save(new TimeEntry(activity, focus, start, end));
    }
}
