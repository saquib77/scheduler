package com.scheduler.scheduler.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Slot add request for Wheel of Fortune.
 * Schedules slot visibility based on schedule config (daysOfWeek, timeSlots, exclusionDates).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotAddRequest {

    @NotNull(message = "Product code is required")
    private String productCode;

    @NotNull(message = "Game code is required")
    private String gameCode;

    @NotNull
    @Min(1)
    private Integer count;

    @NotNull
    @Valid
    private SlotScheduleConfig schedule;

    @Valid
    private RecurringConfig recurring;

    private List<String> channels;

    private List<String> sources;

    private Map<String, Object> metadata;

    /**
     * Optional timezone (e.g., "UTC", "America/New_York"). Defaults to UTC when validFrom/validTo use Z.
     */
    private String timezone;

    /**
     * Schedule configuration for slot visibility windows.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotScheduleConfig {
        @NotNull
        private String validFrom;

        @NotNull
        private String validTo;

        /**
         * Days when slot is visible. e.g. ["MON", "TUE", "WED", "THU", "FRI"]
         */
        private List<String> daysOfWeek;

        /**
         * Time slots within each day. e.g. [{"start": "10:00", "end": "22:00"}]
         */
        private List<TimeSlot> timeSlots;

        /**
         * Dates to exclude. e.g. ["2026-01-14"]
         */
        private List<String> exclusionDates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String start;
        private String end;
    }

    /**
     * Recurring configuration for slot refill behavior.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurringConfig {
        @Builder.Default
        private boolean enabled = true;

        @Builder.Default
        private RecurringFrequency frequency = RecurringFrequency.DAILY;

        @Builder.Default
        private RefillStrategy refillStrategy = RefillStrategy.RESET;

        @Builder.Default
        private boolean carryForwardUnused = false;

        private Integer maxCarryForward;
    }

    public enum RefillStrategy {
        FIXED,     // available = leftover + baseCount
        RESET,     // available = baseCount
        MAX_CAP    // available = min(leftover + baseCount, maxCarryForward)
    }

    public enum RecurringFrequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        CUSTOM_CRON
    }
}
