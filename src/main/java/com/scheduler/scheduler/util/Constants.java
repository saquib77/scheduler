package com.scheduler.scheduler.util;

public final class Constants {

    private Constants() {
    }

    // ─── Job Classes ───────────────────────────────────────────────────────────
    public static final String SLOT_EXECUTION_JOB_CLASS = "com.scheduler.scheduler.job.SlotExecutionJob";

    // ─── JobDataMap Keys ───────────────────────────────────────────────────────
    public static final String KEY_JOB_ID = "jobId";
    public static final String KEY_JOB_NAME = "jobName";
    public static final String KEY_JOB_GROUP = "jobGroup";
    public static final String KEY_PRODUCT_CODE = "productCode";
    public static final String KEY_GAME_CODE = "gameCode";
    public static final String KEY_GAME_TYPE = "gameType";
    public static final String KEY_CAMPAIGN_ID = "campaignId";
    public static final String KEY_PRODUCT_SLOT_ID = "productSlotId";
    public static final String KEY_SLOT_ADD_REQUEST_JSON = "slotAddRequestJson";
    public static final String KEY_INVOKE_PARAM = "invokeParam";
    public static final String KEY_START_TIME = "startTime";
    public static final String KEY_END_TIME = "endTime";
    public static final String KEY_RECURRING = "recurring";
    public static final String KEY_RECURRENCE_FREQUENCY = "recurrenceFrequency";
    public static final String KEY_WORK_DURATION_MINUTES = "workDurationMinutes";
    public static final String KEY_PAUSE_DURATION_MINUTES = "pauseDurationMinutes";
    public static final String KEY_END_ON_DATE_CHANGE = "endOnDateChange";
    public static final String KEY_TIMEZONE = "timezone";
    public static final String KEY_SCHEDULED = "scheduled";
    public static final String KEY_TRIGGER_COUNT = "triggerCount";
    public static final String KEY_SLOT_CONFIG = "slotConfig";
    public static final String KEY_EXECUTION_TIME = "executionTime";
    public static final String KEY_ACTION = "action";
    public static final String KEY_COUNT = "count";

    // ─── Job Naming & Groups ───────────────────────────────────────────────────
    public static final String JOB_PREFIX_SLOT = "slot-";
    public static final String JOB_GROUP_PREFIX_SLOTS = "slots-";
    public static final String JOB_GROUP_SUFFIX_TRIGGERS = "-triggers";
    public static final String TRIGGER_NAME_SUFFIX = "-trigger-";
    public static final String JOB_PREFIX_LEGACY = "job-";
    public static final String DEFAULT_JOB_GROUP = "default";

    // ─── Kafka ─────────────────────────────────────────────────────────────────
    public static final String KAFKA_TOPIC_PREFIX_SLOT_SCHEDULE = "slot-schedule-";
    public static final String KAFKA_TOPIC_DEFAULT = "default";

    // ─── Slot Actions ──────────────────────────────────────────────────────────
    public static final String ACTION_SLOT_VISIBILITY_START = "SLOT_VISIBILITY_START";

    // ─── Default Values ────────────────────────────────────────────────────────
    public static final String DEFAULT_TIMEZONE_UTC = "UTC";
    public static final String DEFAULT_GAME_TYPE_WHEEL_OF_FORTUNE = "WHEEL_OF_FORTUNE";

    // ─── Recurrence Frequency Strings ──────────────────────────────────────────
    public static final String RECURRENCE_DAILY = "DAILY";
    public static final String RECURRENCE_WEEKLY = "WEEKLY";
    public static final String RECURRENCE_MONTHLY = "MONTHLY";

    // ─── Day Abbreviations (for schedule parsing) ──────────────────────────────
    public static final String DAY_MON = "MON";
    public static final String DAY_TUE = "TUE";
    public static final String DAY_WED = "WED";
    public static final String DAY_THU = "THU";
    public static final String DAY_FRI = "FRI";
    public static final String DAY_SAT = "SAT";
    public static final String DAY_SUN = "SUN";

    // ─── Time Format Patterns ──────────────────────────────────────────────────
    public static final String TIME_PATTERN_HH_MM = "HH:mm";
    public static final String TIME_MIDNIGHT = "00:00";
    public static final String TIME_END_OF_DAY = "23:59";
    public static final String DATETIME_SUFFIX_MIDNIGHT_UTC = "T00:00:00Z";

    // ─── Messages ──────────────────────────────────────────────────────────────
    public static final String MSG_NO_VISIBILITY_WINDOWS = "No visibility windows found in the schedule";
    public static final String MSG_NO_WORK_WINDOWS = "No work windows found in the given time range";
    public static final String MSG_SCHEDULE_REQUIRES_START_END = "Schedule requires startTime and endTime in jobTriggerDetails";
    public static final String MSG_JOB_CLASS_NOT_FOUND = "Job class not found: ";
    public static final String MSG_FAILED_TO_SCHEDULE = "Failed to schedule: ";
    public static final String MSG_SLOT_VISIBILITY_SCHEDULED = "Slot visibility scheduled with %d execution(s)";
    public static final String MSG_JOB_SCHEDULED = "Job scheduled successfully with %d execution(s)";
    public static final String MSG_DATETIME_CANNOT_BE_NULL = "Date/time value cannot be null";
    public static final String MSG_START_BEFORE_END = "Start time must be before end time";
    public static final String MSG_START_IN_FUTURE = "Start time must be in the future";
    public static final String MSG_JOB_DETAIL_NOT_FOUND = "jobDetail cannot be found";
    public static final String MSG_JOB_KEY_NOT_EXIST = "jobKey does not exist";
    public static final String MSG_JOB_ALREADY_RUNNING = " is already running.";
    public static final String MSG_INSIDE_CHECK_JOB_EXIST = "Inside check job exist";

    // ─── Scheduler Config ──────────────────────────────────────────────────────
    public static final String QUARTZ_DRIVER_DELEGATE = "org.quartz.jobStore.driverDelegateClass";
    public static final String QUARTZ_POSTGRESQL_DELEGATE = "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
    public static final String BEAN_SCHEDULER = "scheduler";

    // ─── Legacy (keep for backward compatibility) ──────────────────────────────
    public static final String DEFAULT_DATA_SOURCE = "fb8c0b81-1062-43c1-a341-6677e8687c32";
}
