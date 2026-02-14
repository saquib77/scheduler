package com.scheduler.scheduler.model.dao;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.quartz.Job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "job_scheduler")
public class SchedulerJobModelRequest {
	@Id
	@Column(name = "id")
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
			@Parameter(name = "UUID_gen_strategy_class", value = "org.hibernate.id.UUID.CustomVersionOneStrategy") })
    private UUID id;

    @Column(name = "job_name")
    private String jobName;
    
    @Column(name="job_topic")
    private String jobTopic;
    
    @Column(name="job_group")
    private String jobGroup;
    
    @Column(name="job_class")
    private Class<? extends Job> jobClass;
    
    @Column(name="job_desc")
    private String jobDesc;
    
    @Column(name = "start_time")
    private String startTime;
    
    @Column(name = "repeat_time")
    private Long repeatTime;
    
    @Column(name = "repeat_count", columnDefinition = "integer default 0")
    private Integer repeatCount;
    
    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "cron_job", columnDefinition = "boolean default true")
    private Boolean cronJob;
    
    @Column(name= "created")
    private long created;
    
    @Column(name="updated")
    private long updated;

    @Column(name = "invoke_param")
    private String invokeParam;
    
}
