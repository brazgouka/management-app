package com.management.timer.repository;

import com.management.timer.model.TimeRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeRecordRepository extends CrudRepository<TimeRecord, String> { }
