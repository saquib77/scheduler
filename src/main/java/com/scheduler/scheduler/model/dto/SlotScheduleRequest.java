package com.scheduler.scheduler.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request DTO for scheduling product slots in a game service (e.g., Wheel of Fortune campaigns).
 * Slots can be scheduled daily, weekly, or monthly based on the campaign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotScheduleRequest {

    @NotNull(message = "Campaign ID is required")
    private String campaignId;

    @NotNull(message = "Product slot ID is required")
    private String productSlotId;

    /**
     * Slot schedule type: DAILY, WEEKLY, or MONTHLY
     */
    @NotNull(message = "Slot type is required")
    private SlotType slotType;

    /**
     * Game type identifier (e.g., "WHEEL_OF_FORTUNE")
     */
    private String gameType;

    /**
     * Start time in ISO-8601 format or epoch milliseconds
     */
    @NotNull(message = "Start time is required")
    private Object startTime;

    /**
     * End time in ISO-8601 format or epoch milliseconds.
     * For recurring slots, this is the end of the campaign/slot period.
     */
    @NotNull(message = "End time is required")
    private Object endTime;

    /**
     * Duration of each slot in minutes (e.g., 120 for 2 hours).
     * If null or 0, slot runs from start to end without work/pause cycle.
     */
    @Builder.Default
    private Integer slotDurationMinutes = 120;

    /**
     * Pause between slot occurrences in minutes (e.g., 60 for 1 hour).
     * Used when multiple slots per day with breaks.
     */
    @Builder.Default
    private Integer pauseBetweenSlotsMinutes = 60;

    /**
     * If true, slot ends at midnight (no cross-day slots).
     */
    @Builder.Default
    private boolean endOnDateChange = false;

    /**
     * Optional timezone (e.g., "America/New_York")
     */
    private String timezone;

    /**
     * Additional slot/campaign metadata passed to the job
     */
    private Map<String, Object> metadata;

    public enum SlotType {
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
