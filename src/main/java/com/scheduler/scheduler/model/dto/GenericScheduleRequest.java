package com.scheduler.scheduler.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Generic request for scheduling any type of job.
 * Supports one-time or recurring jobs with work/pause cycles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericScheduleRequest {

    @NotBlank(message = "Job ID is required")
    private String jobId;

    /**
     * Job name for Quartz. Defaults to jobId if not provided.
     */
    private String jobName;

    /**
     * Job group for Quartz. Defaults to "default" if not provided.
     */
    private String jobGroup;

    /**
     * Fully qualified class name of the Job implementation.
     * e.g. com.scheduler.scheduler.job.NewJobClass
     */
    @NotBlank(message = "Job class name is required")
    private String jobClassName;

    /**
     * Custom payload passed to the job at execution time.
     */
    private Map<String, Object> jobDetails;

    /**
     * Start time in ISO-8601 format or epoch milliseconds.
     */
    @NotNull(message = "Start time is required")
    private Object startTime;

    /**
     * End time in ISO-8601 format or epoch milliseconds.
     */
    @NotNull(message = "End time is required")
    private Object endTime;

    @Builder.Default
    private boolean recurring = false;

    @Builder.Default
    private RecurrenceType recurrenceFrequency = RecurrenceType.DAILY;

    /**
     * Work period duration in minutes. Null or 0 = single run at start time.
     */
    @Builder.Default
    private Integer workDurationMinutes = 120;

    /**
     * Pause between work periods in minutes.
     */
    @Builder.Default
    private Integer pauseDurationMinutes = 60;

    @Builder.Default
    private boolean endOnDateChange = false;

    private String timezone;

    /**
     * Optional description for audit/tracking.
     */
    private String description;

    public enum RecurrenceType {
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
