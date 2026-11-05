package com.peter.usage_service.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.peter.usage_service.config.InfluxProperties;
import com.peter.usage_service.model.DeviceEnergy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EnergyUsageRepository {
    private final InfluxDBClient influxDBClient;
    private final InfluxProperties influxProperties;

    public EnergyUsageRepository(InfluxDBClient influxDBClient,
        InfluxProperties influxProperties){
        this.influxDBClient = influxDBClient;
        this.influxProperties = influxProperties;
    }

    public List<DeviceEnergy> findDeviceEnergies(Instant start, Instant stop) {
        String fluxQuery = """
                from(bucket: "%s")
                  |> range(start: %s, stop: %s)
                  |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                  |> filter(fn: (r) => r["_field"] == "energyConsumed")
                  |> group(columns: ["device_id"])
                  |> sum(column: "_value")
                """.formatted(influxProperties.bucket(), start, stop);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery);

        List<DeviceEnergy> result = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("device_id");
                if (deviceIdStr == null) {
                    continue;
                }
                double energy = record.getValue() instanceof Number n ? n.doubleValue() : 0.0;
                result.add(new DeviceEnergy(Long.parseLong(deviceIdStr), energy));
            }
        }
        return result;
    }
}
