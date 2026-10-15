package com.management.timer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StartTimerRequest(
        @NotBlank @Size(max = 120) String activity,
        @NotNull Focus focus) {
}
