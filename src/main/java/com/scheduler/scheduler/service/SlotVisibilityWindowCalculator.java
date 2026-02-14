package com.scheduler.scheduler.service;

import com.scheduler.scheduler.model.dto.SlotAddRequest;
import com.scheduler.scheduler.util.Constants;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates slot visibility windows based on SlotAddRequest schedule config.
 * Handles daysOfWeek, timeSlots, and exclusionDates.
 */
public class SlotVisibilityWindowCalculator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_HH_MM);

    private static final Set<String> DAY_ABBREVS = new HashSet<>();
    static {
        DAY_ABBREVS.add(Constants.DAY_MON); DAY_ABBREVS.add(Constants.DAY_TUE); DAY_ABBREVS.add(Constants.DAY_WED);
        DAY_ABBREVS.add(Constants.DAY_THU); DAY_ABBREVS.add(Constants.DAY_FRI); DAY_ABBREVS.add(Constants.DAY_SAT);
        DAY_ABBREVS.add(Constants.DAY_SUN);
    }

    /**
     * Calculates all visibility window start times - when the slot becomes visible to users.
     *
     * @param request the slot add request
     * @param zoneId  timezone for date/time handling
     * @return list of ZonedDateTime when each visibility window starts
     */
    public static List<ZonedDateTime> calculateVisibilityWindows(
            SlotAddRequest request,
            ZoneId zoneId) {

        SlotAddRequest.SlotScheduleConfig schedule = request.getSchedule();
        if (schedule == null || schedule.getValidFrom() == null || schedule.getValidTo() == null) {
            return Collections.emptyList();
        }

        ZonedDateTime validFromRaw = parseZonedDateTime(schedule.getValidFrom(), zoneId);
        ZonedDateTime validToRaw = parseZonedDateTime(schedule.getValidTo(), zoneId);
        if (validFromRaw == null || validToRaw == null) {
            return Collections.emptyList();
        }
        ZonedDateTime validFrom = validFromRaw.withZoneSameInstant(zoneId);
        ZonedDateTime validTo = validToRaw.withZoneSameInstant(zoneId);
        if (validFrom.isAfter(validTo)) {
            return Collections.emptyList();
        }

        Set<DayOfWeek> allowedDays = parseDaysOfWeek(schedule.getDaysOfWeek());
        Set<LocalDate> exclusionDates = parseExclusionDates(schedule.getExclusionDates());
        List<SlotAddRequest.TimeSlot> timeSlots = schedule.getTimeSlots();
        if (timeSlots == null || timeSlots.isEmpty()) {
            timeSlots = defaultTimeSlots();
        }

        List<ZonedDateTime> windows = new ArrayList<>();
        LocalDate currentDate = validFrom.toLocalDate();
        LocalDate endDate = validTo.toLocalDate();

        ZonedDateTime now = ZonedDateTime.now(zoneId);

        while (!currentDate.isAfter(endDate)) {
            if (exclusionDates.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            if (!allowedDays.isEmpty() && !allowedDays.contains(currentDate.getDayOfWeek())) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            for (SlotAddRequest.TimeSlot ts : timeSlots) {
                ZonedDateTime windowStart = parseTimeSlotStart(currentDate, ts.getStart(), zoneId);
                if (windowStart.isBefore(validFrom) || windowStart.isAfter(validTo)) {
                    continue;
                }
                if (!windowStart.isBefore(now)) {
                    windows.add(windowStart);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return windows;
    }

    private static ZonedDateTime parseZonedDateTime(String value, ZoneId zoneId) {
        if (value == null || value.isEmpty()) return null;
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (Exception e) {
            try {
                return ZonedDateTime.parse(value + Constants.DATETIME_SUFFIX_MIDNIGHT_UTC);
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(value.trim(), DATE_FORMAT).atStartOfDay(zoneId);
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    private static Set<DayOfWeek> parseDaysOfWeek(List<String> days) {
        if (days == null || days.isEmpty()) return Collections.emptySet();
        Set<DayOfWeek> result = new HashSet<>();
        for (String d : days) {
            if (d == null) continue;
            DayOfWeek dow = parseDayOfWeek(d.toUpperCase().trim());
            if (dow != null) result.add(dow);
        }
        return result;
    }

    private static DayOfWeek parseDayOfWeek(String abbrev) {
        switch (abbrev) {
            case Constants.DAY_MON: return DayOfWeek.MONDAY;
            case Constants.DAY_TUE: return DayOfWeek.TUESDAY;
            case Constants.DAY_WED: return DayOfWeek.WEDNESDAY;
            case Constants.DAY_THU: return DayOfWeek.THURSDAY;
            case Constants.DAY_FRI: return DayOfWeek.FRIDAY;
            case Constants.DAY_SAT: return DayOfWeek.SATURDAY;
            case Constants.DAY_SUN: return DayOfWeek.SUNDAY;
            default: return null;
        }
    }

    private static Set<LocalDate> parseExclusionDates(List<String> dates) {
        if (dates == null || dates.isEmpty()) return Collections.emptySet();
        return dates.stream()
                .filter(d -> d != null && !d.isEmpty())
                .map(d -> {
                    try {
                        return LocalDate.parse(d.trim(), DATE_FORMAT);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(d -> d != null)
                .collect(Collectors.toSet());
    }

    private static ZonedDateTime parseTimeSlotStart(LocalDate date, String timeStr, ZoneId zoneId) {
        if (timeStr == null || timeStr.isEmpty()) {
            return date.atStartOfDay(zoneId);
        }
        try {
            LocalTime time = LocalTime.parse(timeStr.trim(), TIME_FORMAT);
            return date.atTime(time).atZone(zoneId);
        } catch (Exception e) {
            return date.atStartOfDay(zoneId);
        }
    }

    private static List<SlotAddRequest.TimeSlot> defaultTimeSlots() {
        SlotAddRequest.TimeSlot allDay = SlotAddRequest.TimeSlot.builder()
                .start(Constants.TIME_MIDNIGHT)
                .end(Constants.TIME_END_OF_DAY)
                .build();
        return Collections.singletonList(allDay);
    }
}
