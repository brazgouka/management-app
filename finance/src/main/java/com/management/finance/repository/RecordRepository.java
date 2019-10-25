package com.management.finance.repository;

import com.management.finance.model.Record;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository extends Neo4jRepository<Record, Long> { }
