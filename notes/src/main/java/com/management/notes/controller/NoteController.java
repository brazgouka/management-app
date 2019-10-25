package com.management.notes.controller;

import com.management.notes.model.Label;
import com.management.notes.model.Note;
import com.management.notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class NoteController {

    private final NoteService noteService;

    @Autowired
    public NoteController(final NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<Note> findAll() {
        Note note = new Note("1", "Header", "Text", null, Label.PROCRASTINATION, LocalDateTime.now());
        noteService.save(note);
        return noteService.findAll();
    }

    @PostMapping
    public void save(final Note note) {
        noteService.save(note);
    }
}
