package com.peter.usage_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public record DeviceEnergy(
        Long deviceId,
        double energyConsumed
) {
}