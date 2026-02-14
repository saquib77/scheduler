package com.scheduler.scheduler.repository;

import java.util.UUID;

import com.scheduler.scheduler.model.dao.SchedulerJobModelRequest;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomSchedulerRepository extends JpaRepository<SchedulerJobModelRequest, UUID>{

	SchedulerJobModelRequest findByJobName(String jobName);

}
