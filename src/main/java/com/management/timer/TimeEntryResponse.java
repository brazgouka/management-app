package com.management.timer;

import java.time.Instant;

public record TimeEntryResponse(
        Long id,
        String activity,
        Focus focus,
        Instant startedAt,
        Instant endedAt,
        long durationSeconds,
        boolean running) {

    static TimeEntryResponse from(TimeEntry entry, Instant now) {
        return new TimeEntryResponse(entry.getId(), entry.getActivity(), entry.getFocus(), entry.getStartedAt(),
                entry.getEndedAt(), entry.duration(now).toSeconds(), entry.isRunning());
    }
}
