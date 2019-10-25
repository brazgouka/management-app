package com.management.timer.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@Setter
@RedisHash("time-record")
public class TimeRecord {

    private String id;

    private String name;

    private String recordType;

    private Double timeInterval;

    private Label label;

    private LocalDateTime localDateTime;

}
