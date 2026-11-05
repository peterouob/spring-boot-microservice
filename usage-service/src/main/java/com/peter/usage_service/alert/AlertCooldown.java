package com.peter.usage_service.alert;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertCooldown {

    private static final Duration COOLDOWN_PERIOD = Duration.ofHours(1);

    private final Map<Long, Instant> lastAlertTime= new ConcurrentHashMap<>();

    public boolean canAlert(Long userId, Instant now) {
        Instant last = lastAlertTime.get(userId);
        if (last == null) {
            return true;
        }
        return now.isAfter(last.plus(COOLDOWN_PERIOD));
    }

    public void markAlerted(Long userId, Instant now) {
        lastAlertTime.put(userId, now);
    }
}
