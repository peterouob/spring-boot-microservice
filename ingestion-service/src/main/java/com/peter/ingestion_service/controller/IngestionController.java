package com.peter.ingestion_service.controller;

import com.peter.ingestion_service.dto.EnergyDto;
import com.peter.ingestion_service.service.IngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestData(@RequestBody EnergyDto usageDto) {
        ingestionService.ingestEnergyUsage(usageDto);
    }
}
