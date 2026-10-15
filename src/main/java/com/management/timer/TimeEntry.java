package com.management.timer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String activity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Focus focus;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    protected TimeEntry() {
    }

    public TimeEntry(String activity, Focus focus, Instant startedAt, Instant endedAt) {
        this.activity = activity;
        this.focus = focus;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static TimeEntry startedAt(String activity, Focus focus, Instant startedAt) {
        return new TimeEntry(activity, focus, startedAt, null);
    }

    public boolean isRunning() {
        return endedAt == null;
    }

    public void stop(Instant at) {
        if (!isRunning()) {
            throw new IllegalStateException("Time entry " + id + " is already stopped");
        }
        this.endedAt = at.isBefore(startedAt) ? startedAt : at;
    }

    /**
     * Duration of the entry; running entries are measured up to {@code now}.
     */
    public Duration duration(Instant now) {
        Instant end = isRunning() ? now : endedAt;
        return end.isAfter(startedAt) ? Duration.between(startedAt, end) : Duration.ZERO;
    }

    public Long getId() {
        return id;
    }

    public String getActivity() {
        return activity;
    }

    public Focus getFocus() {
        return focus;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }
}
