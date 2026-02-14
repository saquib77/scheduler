package com.scheduler.scheduler.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.scheduler.model.dto.GenericScheduleRequest;
import com.scheduler.scheduler.model.dto.JobScheduleRequest;
import com.scheduler.scheduler.model.dto.JobScheduleRequest.RecurrenceFrequency;
import com.scheduler.scheduler.model.dto.ScheduleResponse;
import com.scheduler.scheduler.model.dto.SlotAddRequest;
import com.scheduler.scheduler.model.dto.SlotScheduleRequest;
import com.scheduler.scheduler.model.dto.SlotScheduleRequest.SlotType;
import com.scheduler.scheduler.util.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SchedulerService {

	private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Schedules slot visibility based on SlotAddRequest.
	 * Creates triggers at the start of each visibility window (daysOfWeek + timeSlots, excluding exclusionDates).
	 */
	public ScheduleResponse scheduleSlotVisibility(SlotAddRequest request) {
		try {
			ZoneId zoneId = request.getTimezone() != null && !request.getTimezone().isEmpty()
					? ZoneId.of(request.getTimezone())
					: ZoneId.of(Constants.DEFAULT_TIMEZONE_UTC);

			List<ZonedDateTime> visibilityWindows = SlotVisibilityWindowCalculator.calculateVisibilityWindows(
					request, zoneId);

			if (visibilityWindows.isEmpty()) {
				return ScheduleResponse.builder()
						.jobId(request.getProductCode() + "-" + request.getGameCode())
						.jobName(Constants.JOB_PREFIX_SLOT + request.getProductCode() + "-" + request.getGameCode())
						.jobGroup(Constants.JOB_GROUP_PREFIX_SLOTS + request.getGameCode())
						.success(false)
						.message(Constants.MSG_NO_VISIBILITY_WINDOWS)
						.triggerCount(0)
						.scheduledTimes(new ArrayList<>())
						.build();
			}

			String jobId = request.getProductCode() + "-" + request.getGameCode();
			String jobName = Constants.JOB_PREFIX_SLOT + request.getProductCode() + "-" + request.getGameCode();
			String jobGroup = Constants.JOB_GROUP_PREFIX_SLOTS + request.getGameCode();

			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put(Constants.KEY_JOB_ID, jobId);
			jobDataMap.put(Constants.KEY_PRODUCT_CODE, request.getProductCode());
			jobDataMap.put(Constants.KEY_GAME_CODE, request.getGameCode());
			jobDataMap.put(Constants.KEY_COUNT, request.getCount());
			jobDataMap.put(Constants.KEY_SLOT_ADD_REQUEST_JSON, objectMapper.writeValueAsString(request));
			if (request.getMetadata() != null) {
				request.getMetadata().forEach((k, v) -> {
					if (v != null && (v instanceof String || v instanceof Number || v instanceof Boolean)) {
						jobDataMap.put(k, v);
					}
				});
			}

			@SuppressWarnings("unchecked")
			Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(Constants.SLOT_EXECUTION_JOB_CLASS);

			JobDetail jobDetail = buildJobDetail(
					jobDataMap,
					"Slot visibility: " + request.getProductCode(),
					jobName,
					jobGroup,
					jobClass);

			JobKey jobKey = new JobKey(jobName, jobGroup);
			if (scheduler.checkExists(jobKey)) {
				scheduler.deleteJob(jobKey);
			}
			scheduler.addJob(jobDetail, true);

			List<String> scheduledTimes = new ArrayList<>();
			int triggerIndex = 0;
			for (ZonedDateTime windowStart : visibilityWindows) {
				String triggerName = jobName + Constants.TRIGGER_NAME_SUFFIX + triggerIndex++;
				Trigger trigger = buildJobTrigger(
						jobDetail,
						"Visibility window " + triggerIndex,
						triggerName,
						jobGroup + Constants.JOB_GROUP_SUFFIX_TRIGGERS,
						windowStart);
				scheduler.scheduleJob(trigger);
				scheduledTimes.add(windowStart.format(ISO_FORMATTER));
			}

			log.info("Scheduled slot visibility {} with {} trigger(s)", jobName, visibilityWindows.size());

			return ScheduleResponse.builder()
					.jobId(jobId)
					.jobName(jobName)
					.jobGroup(jobGroup)
					.success(true)
					.message(String.format(Constants.MSG_SLOT_VISIBILITY_SCHEDULED, visibilityWindows.size()))
					.triggerCount(visibilityWindows.size())
					.scheduledTimes(scheduledTimes)
					.build();
		} catch (Exception e) {
			log.error("Failed to schedule slot visibility: {}", e.getMessage());
			return ScheduleResponse.builder()
					.jobId(request.getProductCode() + "-" + request.getGameCode())
					.jobName(Constants.JOB_PREFIX_SLOT + request.getProductCode() + "-" + request.getGameCode())
					.jobGroup(Constants.JOB_GROUP_PREFIX_SLOTS + request.getGameCode())
					.success(false)
					.message(Constants.MSG_FAILED_TO_SCHEDULE + e.getMessage())
					.triggerCount(0)
					.scheduledTimes(new ArrayList<>())
					.build();
		}
	}

	/**
	 * Schedules a product slot for a game campaign (e.g., Wheel of Fortune).
	 * Slots can be daily, weekly, or monthly based on campaign.
	 */
	public ScheduleResponse scheduleSlot(SlotScheduleRequest request) {
		String jobId = request.getCampaignId() + "-" + request.getProductSlotId();
		String jobName = Constants.JOB_PREFIX_SLOT + request.getCampaignId() + "-" + request.getProductSlotId();
		String jobGroup = Constants.JOB_GROUP_PREFIX_SLOTS + (request.getGameType() != null ? request.getGameType() : Constants.DEFAULT_JOB_GROUP);

		Map<String, Object> jobDetails = new HashMap<>();
		jobDetails.put(Constants.KEY_CAMPAIGN_ID, request.getCampaignId());
		jobDetails.put(Constants.KEY_PRODUCT_SLOT_ID, request.getProductSlotId());
		jobDetails.put(Constants.KEY_GAME_TYPE, request.getGameType() != null ? request.getGameType() : Constants.DEFAULT_GAME_TYPE_WHEEL_OF_FORTUNE);
		if (request.getMetadata() != null) {
			jobDetails.putAll(request.getMetadata());
		}

		JobScheduleRequest jobRequest = JobScheduleRequest.builder()
				.jobId(jobId)
				.jobName(jobName)
				.jobGroup(jobGroup)
				.jobClassName(Constants.SLOT_EXECUTION_JOB_CLASS)
				.jobDetails(jobDetails)
				.startTime(request.getStartTime())
				.endTime(request.getEndTime())
				.recurring(true)
				.recurrenceFrequency(mapSlotTypeToRecurrence(request.getSlotType()))
				.workDurationMinutes(request.getSlotDurationMinutes())
				.pauseDurationMinutes(request.getPauseBetweenSlotsMinutes())
				.endOnDateChange(request.isEndOnDateChange())
				.timezone(request.getTimezone())
				.build();

		return scheduleJob(jobRequest);
	}

	private RecurrenceFrequency mapSlotTypeToRecurrence(SlotType slotType) {
		if (slotType == null) return RecurrenceFrequency.DAILY;
		switch (slotType) {
			case DAILY: return RecurrenceFrequency.DAILY;
			case WEEKLY: return RecurrenceFrequency.WEEKLY;
			case MONTHLY: return RecurrenceFrequency.MONTHLY;
			default: return RecurrenceFrequency.DAILY;
		}
	}

	/**
	 * Generic scheduler - schedules any type of job with full control over timing and recurrence.
	 */
	public ScheduleResponse scheduleGeneric(GenericScheduleRequest request) {
		JobScheduleRequest jobRequest = JobScheduleRequest.builder()
				.jobId(request.getJobId())
				.jobName(request.getJobName() != null ? request.getJobName() : request.getJobId())
				.jobGroup(request.getJobGroup() != null ? request.getJobGroup() : Constants.DEFAULT_JOB_GROUP)
				.jobClassName(request.getJobClassName())
				.jobDetails(request.getJobDetails())
				.startTime(request.getStartTime())
				.endTime(request.getEndTime())
				.recurring(request.isRecurring())
				.recurrenceFrequency(mapRecurrenceType(request.getRecurrenceFrequency()))
				.workDurationMinutes(request.getWorkDurationMinutes())
				.pauseDurationMinutes(request.getPauseDurationMinutes())
				.endOnDateChange(request.isEndOnDateChange())
				.timezone(request.getTimezone())
				.build();
		return scheduleJob(jobRequest);
	}

	private RecurrenceFrequency mapRecurrenceType(GenericScheduleRequest.RecurrenceType type) {
		if (type == null) return RecurrenceFrequency.DAILY;
		switch (type) {
			case WEEKLY: return RecurrenceFrequency.WEEKLY;
			case MONTHLY: return RecurrenceFrequency.MONTHLY;
			default: return RecurrenceFrequency.DAILY;
		}
	}

	/**
	 * Schedules a job with flexible options: start/end time, recurring or one-time,
	 * and work/pause cycles (e.g., work 2hr, pause 1hr, work 2hr until end).
	 */
	public ScheduleResponse scheduleJob(JobScheduleRequest request) {
		try {
			ZoneId zoneId = parseTimezone(request.getTimezone());
			ZonedDateTime startTime = parseDateTime(request.getStartTime(), zoneId);
			ZonedDateTime endTime = parseDateTime(request.getEndTime(), zoneId);

			validateScheduleTimes(startTime, endTime);

			List<ZonedDateTime> workWindows;
			if (request.isRecurring()) {
				workWindows = ScheduleWindowCalculator.calculateRecurringWorkWindows(
						request, startTime, endTime, zoneId);
			} else {
				workWindows = ScheduleWindowCalculator.calculateWorkWindows(
						request, startTime, endTime);
			}

			if (workWindows.isEmpty()) {
				return ScheduleResponse.builder()
						.jobId(request.getJobId())
						.jobName(request.getJobName())
						.jobGroup(request.getJobGroup())
						.success(false)
						.message(Constants.MSG_NO_WORK_WINDOWS)
						.triggerCount(0)
						.scheduledTimes(new ArrayList<>())
						.build();
			}

			@SuppressWarnings("unchecked")
			Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(request.getJobClassName());

			JobDataMap jobDataMap = new JobDataMap();
			if (request.getJobDetails() != null) {
				request.getJobDetails().forEach(jobDataMap::put);
			}
			jobDataMap.put(Constants.KEY_JOB_ID, request.getJobId());

			JobDetail jobDetail = buildJobDetail(
					jobDataMap,
					"Scheduled job: " + request.getJobName(),
					request.getJobName(),
					request.getJobGroup(),
					jobClass);

			JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());
			if (scheduler.checkExists(jobKey)) {
				scheduler.deleteJob(jobKey);
			}
			scheduler.addJob(jobDetail, true);

			List<String> scheduledTimes = new ArrayList<>();
			int triggerIndex = 0;
			for (ZonedDateTime windowStart : workWindows) {
				String triggerName = request.getJobName() + Constants.TRIGGER_NAME_SUFFIX + triggerIndex++;
				Trigger trigger = buildJobTrigger(
						jobDetail,
						"Work window " + triggerIndex,
						triggerName,
						request.getJobGroup() + Constants.JOB_GROUP_SUFFIX_TRIGGERS,
						windowStart);
				scheduler.scheduleJob(trigger);
				scheduledTimes.add(windowStart.format(ISO_FORMATTER));
			}

			log.info("Scheduled job {} with {} trigger(s) between {} and {}",
					request.getJobName(), workWindows.size(), startTime, endTime);

			return ScheduleResponse.builder()
					.jobId(request.getJobId())
					.jobName(request.getJobName())
					.jobGroup(request.getJobGroup())
					.success(true)
					.message(String.format(Constants.MSG_JOB_SCHEDULED, workWindows.size()))
					.triggerCount(workWindows.size())
					.scheduledTimes(scheduledTimes)
					.build();
		} catch (ClassNotFoundException e) {
			log.error("Job class not found: {}", request.getJobClassName());
			return ScheduleResponse.builder()
					.jobId(request.getJobId())
					.jobName(request.getJobName())
					.jobGroup(request.getJobGroup())
					.success(false)
					.message(Constants.MSG_JOB_CLASS_NOT_FOUND + request.getJobClassName())
					.triggerCount(0)
					.scheduledTimes(new ArrayList<>())
					.build();
		} catch (Exception e) {
			log.error("Failed to schedule job {}: {}", request.getJobName(), e.getMessage());
			return ScheduleResponse.builder()
					.jobId(request.getJobId())
					.jobName(request.getJobName())
					.jobGroup(request.getJobGroup())
					.success(false)
					.message(Constants.MSG_FAILED_TO_SCHEDULE + e.getMessage())
					.triggerCount(0)
					.scheduledTimes(new ArrayList<>())
					.build();
		}
	}

	private int getInt(Map<String, Object> map, String key, int defaultValue) {
		Object val = map.get(key);
		if (val == null) return defaultValue;
		if (val instanceof Number) return ((Number) val).intValue();
		try {
			return Integer.parseInt(String.valueOf(val));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private RecurrenceFrequency parseRecurrenceFrequency(Object val) {
		if (val == null) return RecurrenceFrequency.DAILY;
		String s = String.valueOf(val).toUpperCase();
		switch (s) {
			case Constants.RECURRENCE_WEEKLY: return RecurrenceFrequency.WEEKLY;
			case Constants.RECURRENCE_MONTHLY: return RecurrenceFrequency.MONTHLY;
			default: return RecurrenceFrequency.DAILY;
		}
	}

	private ZoneId parseTimezone(String timezone) {
		return timezone != null && !timezone.isEmpty()
				? ZoneId.of(timezone)
				: ZoneId.systemDefault();
	}

	private ZonedDateTime parseDateTime(Object value, ZoneId zoneId) {
		if (value == null) {
			throw new IllegalArgumentException(Constants.MSG_DATETIME_CANNOT_BE_NULL);
		}
		if (value instanceof Number) {
			long epoch = ((Number) value).longValue();
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), zoneId);
		}
		String str = String.valueOf(value).trim();
		if (str.matches("\\d+")) {
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(str)), zoneId);
		}
		try {
			return ZonedDateTime.parse(str, DateTimeFormatter.ISO_ZONED_DATE_TIME);
		} catch (Exception e) {
			LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return ZonedDateTime.of(ldt, zoneId);
		}
	}

	private void validateScheduleTimes(ZonedDateTime startTime, ZonedDateTime endTime) {
		if (startTime.isAfter(endTime)) {
			throw new IllegalArgumentException(Constants.MSG_START_BEFORE_END);
		}
		if (startTime.isBefore(ZonedDateTime.now())) {
			throw new IllegalArgumentException(Constants.MSG_START_IN_FUTURE);
		}
	}

	private void checkDateTime(ZonedDateTime zonedDateTime) {
		try {
			if(zonedDateTime.isBefore(ZonedDateTime.now())) {
		        throw new Exception("dateTime must be after current dateTime : "+ zonedDateTime + " Current :"+ZonedDateTime.now());
		    }
		}catch(Exception e) {
			log.error("dateTime must be after current dateTime : {} Current : {}",zonedDateTime,ZonedDateTime.now());
		}
	}


	private ZonedDateTime getDateTime(long dateTime) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime),ZoneId.systemDefault());
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
		return zonedDateTime;
	}



	private Trigger buildJobTrigger(JobDetail jobDetail,String description,String name,String group, ZonedDateTime dateTime) {
		return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(name, group)
                .withDescription(description)
                .startAt(Date.from(dateTime.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
	}

	
	private JobDetail buildJobDetail(JobDataMap jobDataMap,String description,String name,String group,Class<? extends Job> jobClass) {
		return JobBuilder.newJob(jobClass)
				.withDescription(description)
				.withIdentity(name, group)
				.usingJobData(jobDataMap)
				.storeDurably().build();
	}


	public boolean deleteScheduledJob(String jobName, String jobGroup) {
		JobKey jobKey = new JobKey(jobName, jobGroup);
		try {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			if (jobDetail == null) {
				throw new Exception(Constants.MSG_JOB_DETAIL_NOT_FOUND);
			} else if (!scheduler.checkExists(jobKey)) {
				throw new Exception(Constants.MSG_JOB_KEY_NOT_EXIST);
			} else {
				scheduler.deleteJob(jobKey);
				return true;
			}
		}catch(Exception e) {
			log.error("Job Not Found with  JobName {}, and JobGroup {}",jobName,jobGroup);
			return false;
		}
	}
	
}
