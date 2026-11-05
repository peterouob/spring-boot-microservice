package com.peter.usage_service.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InfluxProperties.class)
public class InfluxConfig {

    @Bean
    public InfluxDBClient influxDBClient(InfluxProperties influxProperties) {
        return InfluxDBClientFactory.create(influxProperties.url(), influxProperties.token().toCharArray(), influxProperties.org());
    }
}