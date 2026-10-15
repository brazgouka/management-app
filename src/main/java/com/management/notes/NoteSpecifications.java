package com.management.notes;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class NoteSpecifications {

    private NoteSpecifications() {
    }

    /**
     * Case-insensitive full-text match on title or content, optionally restricted to a tag.
     */
    static Specification<Note> matching(String text, String tag) {
        List<Specification<Note>> specs = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            String pattern = "%" + escapeLike(text.trim().toLowerCase()) + "%";
            specs.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern, '\\'),
                    cb.like(cb.lower(root.get("content")), pattern, '\\')));
        }
        if (tag != null && !tag.isBlank()) {
            String normalized = tag.trim().toLowerCase();
            specs.add((root, query, cb) -> cb.isMember(normalized, root.get("tags")));
        }
        return Specification.allOf(specs);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
