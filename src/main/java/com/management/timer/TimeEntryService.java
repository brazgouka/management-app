package com.management.timer;

import com.management.common.BadRequestException;
import com.management.common.ConflictException;
import com.management.common.NotFoundException;
import com.management.common.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TimeEntryService {

    private static final int TOP_ACTIVITIES = 5;
    private static final int DEFAULT_STATS_DAYS = 7;

    private final TimeEntryRepository repository;
    private final Clock clock;

    public TimeEntryService(TimeEntryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public PageResponse<TimeEntryResponse> list(Focus focus, Pageable pageable) {
        var page = focus == null ? repository.findAll(pageable) : repository.findByFocus(focus, pageable);
        Instant now = clock.instant();
        return PageResponse.from(page.map(entry -> TimeEntryResponse.from(entry, now)));
    }

    public Optional<TimeEntryResponse> current() {
        return repository.findFirstByEndedAtIsNull().map(entry -> TimeEntryResponse.from(entry, clock.instant()));
    }

    /**
     * Starts a live timer. Only one timer may run at a time, so a running one has to be stopped first.
     */
    @Transactional
    public TimeEntryResponse start(StartTimerRequest request) {
        repository.findFirstByEndedAtIsNull().ifPresent(running -> {
            throw new ConflictException("Timer for '%s' is already running (id %d); stop it first"
                    .formatted(running.getActivity(), running.getId()));
        });
        Instant now = clock.instant();
        var entry = repository.save(TimeEntry.startedAt(request.activity().trim(), request.focus(), now));
        return TimeEntryResponse.from(entry, now);
    }

    @Transactional
    public TimeEntryResponse stop(Long id) {
        var entry = find(id);
        if (!entry.isRunning()) {
            throw new ConflictException("Time entry %d is already stopped".formatted(id));
        }
        Instant now = clock.instant();
        entry.stop(now);
        return TimeEntryResponse.from(entry, now);
    }

    @Transactional
    public TimeEntryResponse log(LogTimeRequest request) {
        if (!request.endedAt().isAfter(request.startedAt())) {
            throw new BadRequestException("'endedAt' must be after 'startedAt'");
        }
        if (request.endedAt().isAfter(clock.instant())) {
            throw new BadRequestException("Cannot log time in the future");
        }
        var entry = repository.save(new TimeEntry(request.activity().trim(), request.focus(),
                request.startedAt(), request.endedAt()));
        return TimeEntryResponse.from(entry, clock.instant());
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    /**
     * Aggregates entries started within {@code [from, to]} (UTC days, inclusive). Defaults to the last 7 days.
     */
    public TimeStats stats(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_STATS_DAYS - 1);
        if (start.isAfter(end)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }

        Instant now = clock.instant();
        List<TimeEntry> entries = repository.findByStartedAtGreaterThanEqualAndStartedAtLessThan(
                start.atStartOfDay().toInstant(ZoneOffset.UTC),
                end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));

        Map<Focus, Long> byFocus = new EnumMap<>(Focus.class);
        for (Focus focus : Focus.values()) {
            byFocus.put(focus, 0L);
        }
        entries.forEach(entry -> byFocus.merge(entry.getFocus(), entry.duration(now).toSeconds(), Long::sum));

        long total = byFocus.values().stream().mapToLong(Long::longValue).sum();
        Integer score = total > 0 ? (int) Math.round(byFocus.get(Focus.PRODUCTIVE) * 100.0 / total) : null;

        List<TimeStats.ActivityTotal> top = entries.stream()
                .collect(Collectors.groupingBy(entry -> new ActivityKey(entry.getActivity(), entry.getFocus()),
                        Collectors.summingLong(entry -> entry.duration(now).toSeconds())))
                .entrySet().stream()
                .map(e -> new TimeStats.ActivityTotal(e.getKey().activity(), e.getKey().focus(), e.getValue()))
                .sorted(Comparator.comparingLong(TimeStats.ActivityTotal::seconds).reversed())
                .limit(TOP_ACTIVITIES)
                .toList();

        return new TimeStats(start, end, total, byFocus, score, top);
    }

    private TimeEntry find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Time entry", id));
    }

    private record ActivityKey(String activity, Focus focus) {
    }
}
