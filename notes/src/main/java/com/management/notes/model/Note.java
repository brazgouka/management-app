package com.management.notes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document(indexName = "notes", type = "note")
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    private String id;

    private String header;

    private String text;

    private List<String> list;

    private Label label;

    private LocalDateTime date;

}