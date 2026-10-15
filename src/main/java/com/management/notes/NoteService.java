package com.management.notes;

import com.management.common.NotFoundException;
import com.management.common.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository repository;

    public NoteService(NoteRepository repository) {
        this.repository = repository;
    }

    public PageResponse<NoteResponse> search(String text, String tag, Pageable pageable) {
        return PageResponse.from(repository.findAll(NoteSpecifications.matching(text, tag), pageable)
                .map(NoteResponse::from));
    }

    public NoteResponse get(Long id) {
        return NoteResponse.from(find(id));
    }

    public List<TagCount> tags() {
        return repository.countByTag();
    }

    @Transactional
    public NoteResponse create(NoteRequest request) {
        var note = new Note(request.title().trim(), request.content(), normalizeTags(request.tags()));
        return NoteResponse.from(repository.save(note));
    }

    @Transactional
    public NoteResponse update(Long id, NoteRequest request) {
        var note = find(id);
        note.update(request.title().trim(), request.content(), normalizeTags(request.tags()));
        return NoteResponse.from(repository.saveAndFlush(note));
    }

    @Transactional
    public NoteResponse setPinned(Long id, boolean pinned) {
        var note = find(id);
        note.setPinned(pinned);
        return NoteResponse.from(repository.saveAndFlush(note));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Note find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Note", id));
    }

    private static Set<String> normalizeTags(Set<String> tags) {
        return tags.stream()
                .map(tag -> tag.trim().toLowerCase())
                .collect(Collectors.toSet());
    }
}
