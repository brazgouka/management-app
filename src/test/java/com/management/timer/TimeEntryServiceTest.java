package com.management.timer;

import com.management.common.BadRequestException;
import com.management.common.ConflictException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimeEntryServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-10T15:00:00Z");

    private final TimeEntryRepository repository = mock(TimeEntryRepository.class);
    private final TimeEntryService service = new TimeEntryService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void refusesToStartSecondTimer() {
        when(repository.findFirstByEndedAtIsNull())
                .thenReturn(Optional.of(TimeEntry.startedAt("Writing", Focus.PRODUCTIVE, NOW.minusSeconds(60))));

        assertThatThrownBy(() -> service.start(new StartTimerRequest("Gaming", Focus.PROCRASTINATION)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Writing");
        verify(repository, never()).save(any());
    }

    @Test
    void runningEntryIsMeasuredUpToNow() {
        var running = TimeEntry.startedAt("Writing", Focus.PRODUCTIVE, NOW.minus(Duration.ofMinutes(25)));
        when(repository.findFirstByEndedAtIsNull()).thenReturn(Optional.of(running));

        assertThat(service.current()).hasValueSatisfying(entry -> {
            assertThat(entry.running()).isTrue();
            assertThat(entry.durationSeconds()).isEqualTo(25 * 60);
        });
    }

    @Test
    void rejectsLoggingTimeThatEndsBeforeItStarts() {
        var request = new LogTimeRequest("Oops", Focus.PRODUCTIVE, NOW.minusSeconds(60), NOW.minusSeconds(120));

        assertThatThrownBy(() -> service.log(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsLoggingTimeInTheFuture() {
        var request = new LogTimeRequest("Later", Focus.PRODUCTIVE, NOW.minusSeconds(60), NOW.plusSeconds(60));

        assertThatThrownBy(() -> service.log(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void statsComputeProductivityScoreAndTopActivities() {
        Instant day = Instant.parse("2026-05-10T08:00:00Z");
        when(repository.findByStartedAtGreaterThanEqualAndStartedAtLessThan(
                Instant.parse("2026-05-04T00:00:00Z"), Instant.parse("2026-05-11T00:00:00Z"))).thenReturn(List.of(
                new TimeEntry("Coding", Focus.PRODUCTIVE, day, day.plus(Duration.ofHours(2))),
                new TimeEntry("Coding", Focus.PRODUCTIVE, day.plus(Duration.ofHours(3)), day.plus(Duration.ofHours(4))),
                new TimeEntry("Scrolling", Focus.PROCRASTINATION, day.plus(Duration.ofHours(5)), day.plus(Duration.ofHours(6))),
                TimeEntry.startedAt("Reading", Focus.PRODUCTIVE, NOW.minus(Duration.ofMinutes(30)))));

        TimeStats stats = service.stats(null, null);

        assertThat(stats.from()).isEqualTo(LocalDate.parse("2026-05-04"));
        assertThat(stats.to()).isEqualTo(LocalDate.parse("2026-05-10"));
        assertThat(stats.secondsByFocus())
                .containsEntry(Focus.PRODUCTIVE, Duration.ofMinutes(210).toSeconds())
                .containsEntry(Focus.PROCRASTINATION, Duration.ofHours(1).toSeconds());
        assertThat(stats.totalSeconds()).isEqualTo(Duration.ofMinutes(270).toSeconds());
        assertThat(stats.productivityScore()).isEqualTo(78);
        assertThat(stats.topActivities()).first().satisfies(top -> {
            assertThat(top.activity()).isEqualTo("Coding");
            assertThat(top.seconds()).isEqualTo(Duration.ofHours(3).toSeconds());
        });
    }

    @Test
    void statsWithoutDataHaveNoScore() {
        TimeStats stats = service.stats(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-01"));

        assertThat(stats.totalSeconds()).isZero();
        assertThat(stats.productivityScore()).isNull();
        assertThat(stats.topActivities()).isEmpty();
    }
}
