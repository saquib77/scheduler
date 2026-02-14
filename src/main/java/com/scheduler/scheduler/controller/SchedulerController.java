package com.scheduler.scheduler.controller;

import com.scheduler.scheduler.model.dto.GenericScheduleRequest;
import com.scheduler.scheduler.model.dto.JobScheduleRequest;
import com.scheduler.scheduler.model.dto.ScheduleResponse;
import com.scheduler.scheduler.model.dto.SlotAddRequest;
import com.scheduler.scheduler.model.dto.SlotScheduleRequest;
import com.scheduler.scheduler.service.SchedulerService;
import com.scheduler.scheduler.util.ApiMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(ApiMapping.BASE_PATH)
public class SchedulerController {

	@Autowired
	private SchedulerService schedulerService;

	/**
	 * Add a slot and schedule its visibility (Wheel of Fortune).
	 * Uses schedule config: validFrom/validTo, daysOfWeek, timeSlots, exclusionDates.
	 */
	@PostMapping(ApiMapping.SLOTS)
	public ResponseEntity<ScheduleResponse> addSlot(@RequestBody SlotAddRequest request) {
		ScheduleResponse response = schedulerService.scheduleSlotVisibility(request);
		return response.isSuccess()
				? ResponseEntity.ok(response)
				: ResponseEntity.badRequest().body(response);
	}

	/**
	 * Schedule a product slot for game campaigns (e.g., Wheel of Fortune).
	 * Slots can be daily, weekly, or monthly.
	 */
	@PostMapping(ApiMapping.SLOT)
	public ResponseEntity<ScheduleResponse> scheduleSlot(@RequestBody SlotScheduleRequest request) {
		ScheduleResponse response = schedulerService.scheduleSlot(request);
		return response.isSuccess()
				? ResponseEntity.ok(response)
				: ResponseEntity.badRequest().body(response);
	}

	/**
	 * Schedule a job with start/end time, recurring option, and work/pause cycles.
	 * Example: work 2hr, pause 1hr, work 2hr until end time or date change.
	 */
	@PostMapping(ApiMapping.JOB)
	public ResponseEntity<ScheduleResponse> scheduleJob(@RequestBody JobScheduleRequest request) {
		ScheduleResponse response = schedulerService.scheduleJob(request);
		return response.isSuccess()
				? ResponseEntity.ok(response)
				: ResponseEntity.badRequest().body(response);
	}

	/**
	 * Generic scheduler - schedule any type of job with typed request and structured response.
	 * Supports one-time or recurring jobs with work/pause cycles.
	 */
	@PostMapping(ApiMapping.GENERIC)
	public ResponseEntity<ScheduleResponse> scheduleGeneric(@Valid @RequestBody GenericScheduleRequest request) {
		ScheduleResponse response = schedulerService.scheduleGeneric(request);
		return response.isSuccess()
				? ResponseEntity.ok(response)
				: ResponseEntity.badRequest().body(response);
	}

	@DeleteMapping(ApiMapping.DELETE_JOB)
	public ResponseEntity<Boolean> deleteScheduledJob(
			@PathVariable("name") String jobName,
			@PathVariable("group") String jobGroup) {
		boolean response = schedulerService.deleteScheduledJob(jobName,jobGroup);
		if(response) {
			return new ResponseEntity<>(response,HttpStatus.OK);
		}
		return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
	}
	
	
}
