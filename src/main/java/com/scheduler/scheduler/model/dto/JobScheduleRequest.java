package com.scheduler.scheduler.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request DTO for scheduling a job with flexible options:
 * - Start/end time
 * - Recurring or one-time
 * - Work/pause cycles (e.g., work 2hr, pause 1hr, work 2hr until end)
 * - End on date change (midnight) option
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobScheduleRequest {

    @NotNull(message = "Job ID is required")
    private String jobId;

    @NotNull(message = "Job name is required")
    private String jobName;

    @NotNull(message = "Job group is required")
    private String jobGroup;

    /**
     * Fully qualified class name of the Job implementation (e.g., com.scheduler.scheduler.job.NewJobClass)
     */
    @NotNull(message = "Job class name is required")
    private String jobClassName;

    /**
     * Custom payload passed to the job at execution time
     */
    private Map<String, Object> jobDetails;

    /**
     * Start time in ISO-8601 format (e.g., "2025-02-15T09:00:00") or epoch milliseconds
     */
    @NotNull(message = "Start time is required")
    private Object startTime;

    /**
     * End time in ISO-8601 format or epoch milliseconds.
     * For recurring jobs, this is the end of the recurrence period.
     */
    @NotNull(message = "End time is required")
    private Object endTime;

    /**
     * If true, job repeats based on recurrenceFrequency (e.g., daily, weekly).
     * If false, job runs only once between start and end time.
     */
    @Builder.Default
    private boolean recurring = false;

    /**
     * For recurring jobs: DAILY, WEEKLY, etc.
     */
    @Builder.Default
    private RecurrenceFrequency recurrenceFrequency = RecurrenceFrequency.DAILY;

    /**
     * Duration of each work period in minutes (e.g., 120 for 2 hours).
     * If null or 0, job runs continuously from start to end (no work/pause cycle).
     */
    @Builder.Default
    private Integer workDurationMinutes = 120;

    /**
     * Duration of pause between work periods in minutes (e.g., 60 for 1 hour).
     * Ignored if workDurationMinutes is null or 0.
     */
    @Builder.Default
    private Integer pauseDurationMinutes = 60;

    /**
     * If true, stop all job runs when date changes (at midnight).
     * Useful for jobs that should not span across days.
     */
    @Builder.Default
    private boolean endOnDateChange = false;

    /**
     * Optional timezone (e.g., "America/New_York"). Uses system default if not specified.
     */
    private String timezone;

    public enum RecurrenceFrequency {
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
