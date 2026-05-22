package com.devflow.analytics.controller;

import com.devflow.analytics.dto.MetricsSummaryDto;
import com.devflow.analytics.service.MetricsService;
import com.devflow.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final MetricsService metricsService;

    // REST endpoint to get current metrics snapshot
    // Dashboard can poll this on load, then switch
    // to WebSocket for live updates
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MetricsSummaryDto>> getSummary() {
        log.info("Analytics summary requested");
        MetricsSummaryDto summary = metricsService.getMetricsSummary();
        return ResponseEntity.ok(
                ApiResponse.success("Metrics retrieved", summary));
    }

    // Health check for the analytics service
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Analytics service is running",
                        "UP"));
    }
}