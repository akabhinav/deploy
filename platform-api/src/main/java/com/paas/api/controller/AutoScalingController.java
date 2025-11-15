package com.paas.api.controller;

import com.paas.core.entity.AutoScalingConfig;
import com.paas.core.service.AutoScalingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/autoscaling")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auto-Scaling", description = "Auto-scaling configuration")
public class AutoScalingController {

    private final AutoScalingService autoScalingService;

    @GetMapping
    @Operation(summary = "Get all auto-scaling configs")
    public ResponseEntity<List<AutoScalingConfig>> getAllConfigs() {
        log.info("GET /api/v1/autoscaling");
        return ResponseEntity.ok(autoScalingService.getAllConfigs());
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get auto-scaling config by application")
    public ResponseEntity<AutoScalingConfig> getConfigByApplication(@PathVariable Long applicationId) {
        log.info("GET /api/v1/autoscaling/application/{}", applicationId);
        return ResponseEntity.ok(autoScalingService.getConfigByApplicationId(applicationId));
    }

    @PostMapping
    @Operation(summary = "Create or update auto-scaling config")
    public ResponseEntity<AutoScalingConfig> createOrUpdateConfig(@RequestBody AutoScalingConfig config) {
        log.info("POST /api/v1/autoscaling - Config for application: {}", config.getApplicationId());
        AutoScalingConfig saved = autoScalingService.createOrUpdateConfig(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/application/{applicationId}/enable")
    @Operation(summary = "Enable auto-scaling")
    public ResponseEntity<Void> enableAutoScaling(@PathVariable Long applicationId) {
        log.info("POST /api/v1/autoscaling/application/{}/enable", applicationId);
        autoScalingService.enableAutoScaling(applicationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/application/{applicationId}/disable")
    @Operation(summary = "Disable auto-scaling")
    public ResponseEntity<Void> disableAutoScaling(@PathVariable Long applicationId) {
        log.info("POST /api/v1/autoscaling/application/{}/disable", applicationId);
        autoScalingService.disableAutoScaling(applicationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete auto-scaling config")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        log.info("DELETE /api/v1/autoscaling/{}", id);
        autoScalingService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}
