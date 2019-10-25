package com.management.finance.service;

import com.management.finance.model.Record;
import com.management.finance.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecordService {

    private final RecordRepository recordRepository;

    @Autowired
    public RecordService(final RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public List<Record> findAll() {
        List<Record> records = new ArrayList<>();
        recordRepository.findAll().forEach(records::add);
        return records;
    }

    public void save(final Record record) {
        recordRepository.save(record);
    }

}
