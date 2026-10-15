package com.management.timer;

import com.management.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

@Tag(name = "Time tracking", description = "Live timers, manual time logs and productivity stats")
@RestController
@RequestMapping("/api/time-entries")
class TimeEntryController {

    private final TimeEntryService service;

    TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<TimeEntryResponse> list(
            @RequestParam(required = false) Focus focus,
            @ParameterObject @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(focus, pageable);
    }

    @Operation(summary = "The running timer, or 204 No Content if none is running")
    @GetMapping("/current")
    ResponseEntity<TimeEntryResponse> current() {
        return service.current()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Start a live timer (409 if another one is running)")
    @PostMapping("/start")
    ResponseEntity<TimeEntryResponse> start(@Valid @RequestBody StartTimerRequest request, UriComponentsBuilder uri) {
        var started = service.start(request);
        return ResponseEntity.created(uri.path("/api/time-entries/{id}").build(started.id())).body(started);
    }

    @Operation(summary = "Stop a running timer")
    @PostMapping("/{id}/stop")
    TimeEntryResponse stop(@PathVariable Long id) {
        return service.stop(id);
    }

    @Operation(summary = "Log a finished block of time after the fact")
    @PostMapping
    ResponseEntity<TimeEntryResponse> log(@Valid @RequestBody LogTimeRequest request, UriComponentsBuilder uri) {
        var logged = service.log(request);
        return ResponseEntity.created(uri.path("/api/time-entries/{id}").build(logged.id())).body(logged);
    }

    @Operation(summary = "Totals by focus, productivity score and top activities (defaults to the last 7 days)")
    @GetMapping("/stats")
    TimeStats stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.stats(from, to);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
