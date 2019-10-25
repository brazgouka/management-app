package com.management.timer.controller;

import com.management.timer.model.TimeRecord;
import com.management.timer.repository.TimeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TimeRecordController {

    private final TimeRecordRepository timeRecordRepository;

    @Autowired
    public TimeRecordController(final TimeRecordRepository timeRecordRepository) {
        this.timeRecordRepository = timeRecordRepository;
    }

    @GetMapping
    public List<TimeRecord> findAll() {
        List<TimeRecord> records = new ArrayList<>();
        timeRecordRepository.findAll().forEach(records::add);
        return records;
    }

    @PostMapping
    public void save(final TimeRecord record) {
        timeRecordRepository.save(record);
    }
}
