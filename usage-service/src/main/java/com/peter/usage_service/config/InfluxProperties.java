package com.peter.usage_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("influx")
public record InfluxProperties(
        String url,
        String token,
        String org,
        String bucket
) {
    @Override
    public String toString() {
        return "InfluxProperties[url=%s, org=%s, bucket=%s, token=***]"
                .formatted(url, org, bucket);
    }
}
