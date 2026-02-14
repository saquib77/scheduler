package com.scheduler.scheduler.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import com.scheduler.scheduler.util.Constants;

import lombok.extern.slf4j.Slf4j;

/**
 * Job implementation for executing product slot schedules (e.g., Wheel of Fortune campaigns).
 * Supports both SlotAddRequest format (productCode, gameCode, slotAddRequestJson) and
 * legacy format (campaignId, productSlotId, gameType).
 */
@Slf4j
public class SlotExecutionJob implements Job {


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String slotAddRequestJson = dataMap.getString(Constants.KEY_SLOT_ADD_REQUEST_JSON);

        String gameCode = dataMap.getString(Constants.KEY_GAME_CODE);
        if (gameCode == null) {
            gameCode = dataMap.getString(Constants.KEY_GAME_TYPE);
        }
        String productCode = dataMap.getString(Constants.KEY_PRODUCT_CODE);
        if (productCode == null) {
            productCode = dataMap.getString(Constants.KEY_PRODUCT_SLOT_ID);
        }

        log.info("Executing slot visibility: productCode={}, gameCode={}", productCode, gameCode);

        String topic = Constants.KAFKA_TOPIC_PREFIX_SLOT_SCHEDULE + (gameCode != null ? gameCode.toLowerCase() : Constants.KAFKA_TOPIC_DEFAULT);
        Object payload = buildSlotPayload(dataMap, slotAddRequestJson);
        kafkaTemplate.send(topic, payload);
    }

    private Object buildSlotPayload(JobDataMap dataMap, String slotAddRequestJson) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        dataMap.forEach((k, v) -> {
            if (v != null && !Constants.KEY_SLOT_ADD_REQUEST_JSON.equals(k)) {
                payload.put(k, v);
            }
        });
        if (slotAddRequestJson != null) {
            payload.put(Constants.KEY_SLOT_CONFIG, slotAddRequestJson);
        }
        payload.put(Constants.KEY_EXECUTION_TIME, java.time.Instant.now().toString());
        payload.put(Constants.KEY_ACTION, Constants.ACTION_SLOT_VISIBILITY_START);
        return payload;
    }
}
