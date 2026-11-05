package com.peter.usage_service.alert;

import com.peter.usage_service.client.DeviceClient;
import com.peter.usage_service.client.UserClient;
import com.peter.usage_service.dto.DeviceDto;
import com.peter.usage_service.dto.UserDto;
import com.peter.usage_service.model.DeviceEnergy;
import com.peter.usage_service.repository.EnergyUsageRepository;
import kafka.event.AlertingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnergyAlertTest {

    @Mock
    EnergyUsageRepository energyUsageRepository;

    @Mock
    DeviceClient deviceClient;

    @Mock
    UserClient userClient;

    @Mock
    KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    EnergyAlert energyAlert;


    @BeforeEach
    void setUp() {
        energyAlert = new EnergyAlert(
                energyUsageRepository, deviceClient, userClient, kafkaTemplate, new AlertCooldown());
    }

    @Test
    void sendsAlertWhenOverThreshold() {
        // given：裝置 10 屬於 user 1，用了 150，門檻 100
        when(energyUsageRepository.findDeviceEnergies(any(), any()))
                .thenReturn(List.of(new DeviceEnergy(10L, 150.0)));
        when(deviceClient.getDeviceById(10L)).thenReturn(device(10L, 1L));
        when(userClient.getUserById(1L)).thenReturn(user(1L, 100.0, true));

        // when
        energyAlert.checkEnergyAlerts();

        // then：有送一筆，而且內容正確
        ArgumentCaptor<AlertingEvent> captor = ArgumentCaptor.forClass(AlertingEvent.class);
        verify(kafkaTemplate).send(eq("energy-alerts"), eq("1"), captor.capture());

        AlertingEvent event = captor.getValue();
        assertEquals(1L, event.userId());
        assertEquals(150.0, event.energyConsumed());
        assertEquals(100.0, event.threshold());
    }

    @Test
    void noAlertWhenUnderThreshold() {
        when(energyUsageRepository.findDeviceEnergies(any(), any()))
                .thenReturn(List.of(new DeviceEnergy(10L, 50.0)));
        when(deviceClient.getDeviceById(10L)).thenReturn(device(10L, 1L));
        when(userClient.getUserById(1L)).thenReturn(user(1L, 100.0, true));

        energyAlert.checkEnergyAlerts();

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void sumsAllDevicesOfSameUser() {
        // 兩台各 60，單看都沒超標，加起來 120 超標
        // 這個測試就是在防止之前「用 deviceId 分組」的 bug 再出現
        when(energyUsageRepository.findDeviceEnergies(any(), any()))
                .thenReturn(List.of(new DeviceEnergy(10L, 60.0), new DeviceEnergy(11L, 60.0)));
        when(deviceClient.getDeviceById(10L)).thenReturn(device(10L, 1L));
        when(deviceClient.getDeviceById(11L)).thenReturn(device(11L, 1L));
        when(userClient.getUserById(1L)).thenReturn(user(1L, 100.0, true));

        energyAlert.checkEnergyAlerts();

        ArgumentCaptor<AlertingEvent> captor = ArgumentCaptor.forClass(AlertingEvent.class);
        verify(kafkaTemplate).send(eq("energy-alerts"), eq("1"), captor.capture());
        assertEquals(120.0, captor.getValue().energyConsumed());
    }

    @Test
    void noAlertWhenUserDisabledAlerting() {
        when(energyUsageRepository.findDeviceEnergies(any(), any()))
                .thenReturn(List.of(new DeviceEnergy(10L, 150.0)));
        when(deviceClient.getDeviceById(10L)).thenReturn(device(10L, 1L));
        when(userClient.getUserById(1L)).thenReturn(user(1L, 100.0, false));

        energyAlert.checkEnergyAlerts();

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void oneDeviceFailingDoesNotStopOthers() {
        // 裝置 10 查詢失敗，裝置 20 正常且超標，user 2 還是要收到告警
        when(energyUsageRepository.findDeviceEnergies(any(), any()))
                .thenReturn(List.of(new DeviceEnergy(10L, 50.0), new DeviceEnergy(20L, 150.0)));
        when(deviceClient.getDeviceById(10L)).thenThrow(new RuntimeException("device-service down"));
        when(deviceClient.getDeviceById(20L)).thenReturn(device(20L, 2L));
        when(userClient.getUserById(2L)).thenReturn(user(2L, 100.0, true));

        energyAlert.checkEnergyAlerts();

        verify(kafkaTemplate).send(eq("energy-alerts"), eq("2"), any(AlertingEvent.class));
    }

    @Test
    void secondRunWithinCooldownDoesNotSendAgain() {
        when(energyUsageRepository.findDeviceEnergies(any(), any()))
                .thenReturn(List.of(new DeviceEnergy(10L, 150.0)));
        when(deviceClient.getDeviceById(10L)).thenReturn(device(10L, 1L));
        when(userClient.getUserById(1L)).thenReturn(user(1L, 100.0, true));

        energyAlert.checkEnergyAlerts();
        energyAlert.checkEnergyAlerts();   // 模擬 10 秒後排程又跑了一次

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(AlertingEvent.class));
    }


    private DeviceDto device(Long id, Long userId) {
        return DeviceDto.builder()
                .id(id)
                .userId(userId)
                .build();
    }

    private UserDto user(Long id, double threshold, boolean alerting) {
        return UserDto.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .alerting(alerting)
                .energyAlertingThreshold(threshold)
                .build();
    }}
