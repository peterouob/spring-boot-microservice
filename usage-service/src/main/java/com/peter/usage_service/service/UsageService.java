package com.peter.usage_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.peter.usage_service.client.DeviceClient;
import com.peter.usage_service.client.UserClient;
import com.peter.usage_service.config.InfluxProperties;
import com.peter.usage_service.dto.DeviceDto;
import com.peter.usage_service.dto.UserDto;
import com.peter.usage_service.model.DeviceEnergy;
import kafka.event.AlertingEvent;
import kafka.event.EnergyUsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;
    private final InfluxProperties influxProperties;

    public UsageService(InfluxDBClient influxDBClient,
                        InfluxProperties influxProperties) {
        this.influxDBClient = influxDBClient;
        this.influxProperties = influxProperties;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent event) {
        log.info("Received energy usage event: {}", event);

        Point point = Point.measurement("energy_usage")
                .addTag("device_id", String.valueOf(event.deviceId()))
                .addField("energyConsumed", event.energyUsage())
                .time(event.timestamp(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(influxProperties.bucket(),
                influxProperties.org(), point);
    }
}
