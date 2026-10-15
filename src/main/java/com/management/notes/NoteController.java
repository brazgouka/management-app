package com.management.notes;

import com.management.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Tag(name = "Notes", description = "Tagged notes with full-text search")
@RestController
@RequestMapping("/api/notes")
class NoteController {

    private final NoteService service;

    NoteController(NoteService service) {
        this.service = service;
    }

    @Operation(summary = "Search notes by text and/or tag. Pinned notes come first by default")
    @GetMapping
    PageResponse<NoteResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tag,
            @ParameterObject @PageableDefault(size = 20, sort = {"pinned", "updatedAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return service.search(q, tag, pageable);
    }

    @Operation(summary = "All tags in use, with how many notes carry each")
    @GetMapping("/tags")
    List<TagCount> tags() {
        return service.tags();
    }

    @GetMapping("/{id}")
    NoteResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request, UriComponentsBuilder uri) {
        var created = service.create(request);
        return ResponseEntity.created(uri.path("/api/notes/{id}").build(created.id())).body(created);
    }

    @PutMapping("/{id}")
    NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Pin a note to the top of the list")
    @PutMapping("/{id}/pin")
    NoteResponse pin(@PathVariable Long id) {
        return service.setPinned(id, true);
    }

    @Operation(summary = "Unpin a note")
    @DeleteMapping("/{id}/pin")
    NoteResponse unpin(@PathVariable Long id) {
        return service.setPinned(id, false);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
