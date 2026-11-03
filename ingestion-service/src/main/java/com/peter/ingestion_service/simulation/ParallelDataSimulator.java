package com.peter.ingestion_service.simulation;

import com.peter.ingestion_service.dto.EnergyDto;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class ParallelDataSimulator implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.requests-per-interval}")
    private int requestsPerInterval;

    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    private final ExecutorService executorService;

    public ParallelDataSimulator() {
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting parallel data simulation with {} threads", parallelThreads);
        ((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        int batchSize = requestsPerInterval / parallelThreads;
        int remainder = requestsPerInterval % parallelThreads;

        for (int i=1; i<parallelThreads; i++) {
            int requestsForThread = batchSize + (i < remainder ? 1 : 0);
            executorService.submit(() -> {
                for (int j = 1; j <= requestsForThread; j++) {
                    EnergyDto dto = EnergyDto.builder()
                            .deviceId(random.nextLong(1, 6))
                            .energyConsumed(Math.round(random.nextDouble(0.0, 10.0) * 100.0) / 100.0)
                            .timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
                            .build();

                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        HttpEntity<EnergyDto> request = new HttpEntity<>(dto, headers);
                        restTemplate.postForEntity(ingestionEndpoint, request, Void.class);

                        log.info("Sent mock data: {}", dto);
                    }catch (Exception e) {
                        log.error("Failed to send mock data: {}", e.getMessage());
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        log.info("Executor service shutdown complete");
    }
}
