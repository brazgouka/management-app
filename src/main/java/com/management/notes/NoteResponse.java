package com.management.notes;

import java.time.Instant;
import java.util.List;

public record NoteResponse(
        Long id,
        String title,
        String content,
        boolean pinned,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt) {

    static NoteResponse from(Note note) {
        return new NoteResponse(note.getId(), note.getTitle(), note.getContent(), note.isPinned(),
                note.getTags().stream().sorted().toList(), note.getCreatedAt(), note.getUpdatedAt());
    }
}
