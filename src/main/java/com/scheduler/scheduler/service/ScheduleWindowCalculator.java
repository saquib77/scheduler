package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.dto.JobScheduleRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates work windows for jobs with work/pause cycles.
 * Example: work 2hr, pause 1hr, work 2hr until end time or date change.
 */
public class ScheduleWindowCalculator {

    /**
     * Calculates all work window start times between startTime and endTime.
     *
     * @param request the job schedule request
     * @param startTime start of scheduling period
     * @param endTime end of scheduling period
     * @return list of ZonedDateTime representing when each work period starts
     */
    public static List<ZonedDateTime> calculateWorkWindows(
            JobScheduleRequest request,
            ZonedDateTime startTime,
            ZonedDateTime endTime) {

        List<ZonedDateTime> windows = new ArrayList<>();

        Integer workMinutes = request.getWorkDurationMinutes();
        Integer pauseMinutes = request.getPauseDurationMinutes();

        // No work/pause cycle - single trigger at start time
        if (workMinutes == null || workMinutes <= 0) {
            if (!startTime.isAfter(endTime)) {
                windows.add(startTime);
            }
            return windows;
        }

        int pauseMinutesVal = (pauseMinutes == null || pauseMinutes < 0) ? 0 : pauseMinutes;
        int cycleDurationMinutes = workMinutes + pauseMinutesVal;

        // When endOnDateChange, cap end time to end of start date (midnight)
        ZonedDateTime effectiveEndTime = endTime;
        if (request.isEndOnDateChange()) {
            ZonedDateTime endOfStartDay = startTime.toLocalDate()
                    .plusDays(1).atStartOfDay(ZoneId.from(startTime)).minusSeconds(1);
            if (endTime.isAfter(endOfStartDay)) {
                effectiveEndTime = endOfStartDay;
            }
        }

        ZonedDateTime currentWindowStart = startTime;

        while (!currentWindowStart.isAfter(effectiveEndTime)) {
            // If endOnDateChange, don't schedule past midnight
            if (request.isEndOnDateChange() && !currentWindowStart.toLocalDate().equals(startTime.toLocalDate())) {
                break;
            }

            // Check if this window would end before effectiveEndTime
            ZonedDateTime windowEnd = currentWindowStart.plusMinutes(workMinutes);
            if (!windowEnd.isAfter(effectiveEndTime)) {
                windows.add(currentWindowStart);
            }

            currentWindowStart = currentWindowStart.plusMinutes(cycleDurationMinutes);

            // For endOnDateChange, stop at end of day
            if (request.isEndOnDateChange() && currentWindowStart.toLocalDate().isAfter(startTime.toLocalDate())) {
                break;
            }
        }

        return windows;
    }

    /**
     * For recurring jobs, generates work windows for each occurrence in the date range.
     * DAILY: each day from rangeStart to rangeEnd
     * WEEKLY: each week (same day of week) from rangeStart to rangeEnd
     * MONTHLY: each month (same day of month, or last day if invalid) from rangeStart to rangeEnd
     */
    public static List<ZonedDateTime> calculateRecurringWorkWindows(
            JobScheduleRequest request,
            ZonedDateTime rangeStart,
            ZonedDateTime rangeEnd,
            ZoneId zoneId) {

        List<ZonedDateTime> allWindows = new ArrayList<>();

        ZonedDateTime currentOccurrenceStart = rangeStart;

        while (!currentOccurrenceStart.isAfter(rangeEnd)) {
            ZonedDateTime occurrenceEnd;
            if (request.isEndOnDateChange()) {
                occurrenceEnd = currentOccurrenceStart.toLocalDate()
                        .atTime(23, 59, 59)
                        .atZone(zoneId);
                if (occurrenceEnd.isAfter(rangeEnd)) {
                    occurrenceEnd = rangeEnd;
                }
            } else {
                occurrenceEnd = rangeEnd;
            }

            List<ZonedDateTime> dayWindows = calculateWorkWindows(
                    request, currentOccurrenceStart, occurrenceEnd);
            allWindows.addAll(dayWindows);

            switch (request.getRecurrenceFrequency()) {
                case DAILY:
                    currentOccurrenceStart = currentOccurrenceStart.plusDays(1)
                            .withHour(rangeStart.getHour())
                            .withMinute(rangeStart.getMinute())
                            .withSecond(rangeStart.getSecond());
                    break;
                case WEEKLY:
                    currentOccurrenceStart = currentOccurrenceStart.plusWeeks(1);
                    break;
                case MONTHLY:
                    currentOccurrenceStart = plusMonthsSameDay(currentOccurrenceStart, 1, rangeStart);
                    break;
                default:
                    currentOccurrenceStart = currentOccurrenceStart.plusDays(1);
            }
        }

        return allWindows;
    }

    /**
     * Advances by N months. Java's plusMonths already handles end-of-month
     * (e.g., Jan 31 + 1 month = Feb 28). Preserves time from template.
     */
    private static ZonedDateTime plusMonthsSameDay(ZonedDateTime current, int months, ZonedDateTime template) {
        return current.plusMonths(months)
                .withHour(template.getHour())
                .withMinute(template.getMinute())
                .withSecond(template.getSecond());
    }
}
