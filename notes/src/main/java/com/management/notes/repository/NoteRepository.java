package com.management.notes.repository;

import com.management.notes.model.Note;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends ElasticsearchRepository<Note, String> { }
