package com.peter.ingestion_service.service;

import com.peter.ingestion_service.dto.EnergyDto;
import kafka.event.EnergyUsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    public IngestionService(KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void ingestEnergyUsage(EnergyDto intput) {
        EnergyUsageEvent event = EnergyUsageEvent.builder()
                .deviceId(intput.deviceId())
                .energyUsage(intput.energyConsumed())
                .timestamp(intput.timestamp())
                .build();

        kafkaTemplate.send("energy-usage", event);
        log.info("Energy usage event ingested: {}", event);
    }
}
