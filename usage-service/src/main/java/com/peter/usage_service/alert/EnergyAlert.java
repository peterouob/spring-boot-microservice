package com.peter.usage_service.alert;

import com.peter.usage_service.client.DeviceClient;
import com.peter.usage_service.client.UserClient;
import com.peter.usage_service.dto.DeviceDto;
import com.peter.usage_service.dto.UserDto;
import com.peter.usage_service.model.DeviceEnergy;
import com.peter.usage_service.repository.EnergyUsageRepository;
import kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EnergyAlert {

    private static final String ALERT_TOPIC = "energy-alerts";

    private final EnergyUsageRepository energyUsageRepository;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;
    private final AlertCooldown alertCooldown;

    public EnergyAlert(EnergyUsageRepository energyUsageRepository,
                              DeviceClient deviceClient,
                              UserClient userClient,
                              KafkaTemplate<String, AlertingEvent> kafkaTemplate,
                              AlertCooldown alertCooldown) {
        this.energyUsageRepository = energyUsageRepository;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
        this.alertCooldown = alertCooldown;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void checkEnergyAlerts() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(Duration.ofHours(1));

        List<DeviceEnergy> deviceEnergies = energyUsageRepository.findDeviceEnergies(oneHourAgo, now);
        Map<Long, Double> energyByUser = sumEnergyByUser(deviceEnergies);

        for (Map.Entry<Long,Double> entry: energyByUser.entrySet()) {
            checkUser(entry.getKey(), entry.getValue(),now);
        }
    }

    private Map<Long, Double> sumEnergyByUser(List<DeviceEnergy> deviceEnergies) {
        Map<Long, Double> energyMapUser = new HashMap<>();
        for (DeviceEnergy deviceEnergy: deviceEnergies) {
            Long userId = findOwnerId(deviceEnergy.deviceId());
            if (userId == null) {
                continue;
            }
            energyMapUser.merge(userId, deviceEnergy.energyConsumed(), Double::sum);
        }
        return energyMapUser;
    }

    private Long findOwnerId(Long deviceId) {
        try {
            DeviceDto deviceDto = deviceClient.getDeviceById(deviceId);
            if (deviceDto == null || deviceDto.userId() == null) {
                log.warn("Device {} not found or has no owner", deviceId);
                return null;
            }
            return deviceDto.userId();
        }catch (Exception e) {
            log.warn("Error finding owner for device {}", deviceId, e);
            return null;
        }
    }

    private void checkUser(Long userId, double totalConsumption, Instant now) {

        if (!alertCooldown.canAlert(userId, now)) {
            return;
        }

        UserDto user = fetchUser(userId);
        if (user == null || !user.alerting()) {
            return;
        }

        double threshold = user.energyAlertingThreshold();
        if (totalConsumption <= threshold) {
            log.info("User {} ok: consumption {}, threshold {}", userId, totalConsumption, threshold);
            return;
        }

        log.info("User {} exceeded: consumption {}, threshold {}", userId, totalConsumption, threshold);
        AlertingEvent event = AlertingEvent.builder()
                .userId(userId)
                .message("Energy consumption threshold exceeded")
                .threshold(threshold)
                .energyConsumed(totalConsumption)
                .email(user.email())
                .build();

        kafkaTemplate.send(ALERT_TOPIC, String.valueOf(userId), event);
        alertCooldown.markAlerted(userId, now);
    }


    private UserDto fetchUser(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (Exception e) {
            log.warn("Failed to fetch user {}", userId, e);
            return null;
        }
    }
}
