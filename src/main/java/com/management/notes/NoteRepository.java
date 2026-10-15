package com.management.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    @Query("""
            select new com.management.notes.TagCount(t, count(n))
            from Note n join n.tags t
            group by t
            order by count(n) desc, t
            """)
    List<TagCount> countByTag();
}
