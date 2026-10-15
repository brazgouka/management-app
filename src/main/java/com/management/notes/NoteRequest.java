package com.management.notes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record NoteRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 10_000) String content,
        @Size(max = 10) Set<@NotBlank @Size(max = 32) @Pattern(regexp = "[\\p{L}\\p{N} _-]+",
                message = "may contain only letters, digits, spaces, '-' and '_'") String> tags) {

    public NoteRequest {
        tags = tags == null ? Set.of() : tags;
    }
}
