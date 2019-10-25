package com.management.finance.model;

import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@NodeEntity
public class Record {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private Boolean approved;

    private Double amount = 0.0;

    private RecordType recordType;

    private LocalDateTime date;

}