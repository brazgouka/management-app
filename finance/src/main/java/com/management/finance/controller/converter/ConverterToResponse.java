package com.management.finance.controller.converter;

import com.management.finance.controller.form.RecordForm;
import com.management.finance.controller.response.RecordResponse;
import com.management.finance.model.Record;
import com.management.finance.model.RecordType;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ConverterToResponse {

    public RecordResponse convert(final Record record) {
        RecordResponse response = new RecordResponse();
        response.setId(record.getId());
        response.setAmount(record.getAmount() != null ? record.getAmount() : 0.0);
        response.setName(record.getName());
        LocalDateTime dateTime = record.getDate();
        response.setDate(dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        response.setRecordType(record.getRecordType());
        response.setApproved(record.getApproved());
        return response;
    }

    public Record convert(final RecordForm recordForm) {
        Record record = new Record();
        record.setAmount(Double.valueOf(recordForm.getAmount()));
        record.setName(recordForm.getName());
        record.setDate(LocalDateTime.now());
        record.setRecordType(RecordType.valueOf(recordForm.getRecordType()));
        record.setApproved(Boolean.valueOf(recordForm.getApproved()));
        return record;
    }
}

