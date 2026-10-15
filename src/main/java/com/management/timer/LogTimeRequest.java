package com.management.timer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * A finished block of time recorded after the fact.
 */
public record LogTimeRequest(
        @NotBlank @Size(max = 120) String activity,
        @NotNull Focus focus,
        @NotNull Instant startedAt,
        @NotNull Instant endedAt) {
}
