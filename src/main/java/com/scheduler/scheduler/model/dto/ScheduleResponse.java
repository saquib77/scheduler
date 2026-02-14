package com.scheduler.scheduler.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private String jobId;
    private String jobName;
    private String jobGroup;
    private boolean success;
    private String message;
    private int triggerCount;
    private List<String> scheduledTimes;
}
