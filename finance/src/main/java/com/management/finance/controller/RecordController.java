package com.management.finance.controller;

import com.management.finance.controller.converter.ConverterToResponse;
import com.management.finance.controller.form.RecordForm;
import com.management.finance.controller.response.RecordResponse;
import com.management.finance.model.Record;
import com.management.finance.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RecordController {

    private final RecordService recordService;
    private final ConverterToResponse converter;

    @Autowired
    public RecordController(final RecordService recordService,
                            final ConverterToResponse converter) {
        this.recordService = recordService;
        this.converter = converter;
    }

    @GetMapping("/finances")
    public List<RecordResponse> findAll() {
        return recordService.findAll().stream()
                .map(converter::convert)
                .collect(Collectors.toList());
    }

    @PostMapping("/finances/save")
    public void save(@RequestBody final RecordForm recordForm) {
        recordService.save(converter.convert(recordForm));
    }
}
