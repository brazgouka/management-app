package com.management.timer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    Optional<TimeEntry> findFirstByEndedAtIsNull();

    Page<TimeEntry> findByFocus(Focus focus, Pageable pageable);

    List<TimeEntry> findByStartedAtGreaterThanEqualAndStartedAtLessThan(Instant from, Instant to);
}
