package com.management.finance.controller.response;

import com.management.finance.model.RecordType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordResponse {

    private Long id;

    private String name;

    private Boolean approved;

    private Double amount;

    private RecordType recordType;

    private String date;

}
