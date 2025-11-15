package com.paas.api.controller;

import com.paas.common.dto.ApplicationDTO;
import com.paas.core.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Applications", description = "Application management endpoints")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "Get all applications")
    public ResponseEntity<List<ApplicationDTO>> getAllApplications() {
        log.info("GET /api/v1/applications - Fetching all applications");
        List<ApplicationDTO> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID")
    public ResponseEntity<ApplicationDTO> getApplicationById(@PathVariable Long id) {
        log.info("GET /api/v1/applications/{} - Fetching application", id);
        ApplicationDTO application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(application);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get application by name")
    public ResponseEntity<ApplicationDTO> getApplicationByName(@PathVariable String name) {
        log.info("GET /api/v1/applications/name/{} - Fetching application", name);
        ApplicationDTO application = applicationService.getApplicationByName(name);
        return ResponseEntity.ok(application);
    }

    @PostMapping
    @Operation(summary = "Create new application")
    public ResponseEntity<ApplicationDTO> createApplication(@Valid @RequestBody ApplicationDTO applicationDTO) {
        log.info("POST /api/v1/applications - Creating application: {}", applicationDTO.getName());
        ApplicationDTO createdApplication = applicationService.createApplication(applicationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdApplication);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update application")
    public ResponseEntity<ApplicationDTO> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationDTO applicationDTO) {
        log.info("PUT /api/v1/applications/{} - Updating application", id);
        ApplicationDTO updatedApplication = applicationService.updateApplication(id, applicationDTO);
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete application")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        log.info("DELETE /api/v1/applications/{} - Deleting application", id);
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/environment/{environmentId}")
    @Operation(summary = "Get applications by environment")
    public ResponseEntity<List<ApplicationDTO>> getApplicationsByEnvironment(@PathVariable Long environmentId) {
        log.info("GET /api/v1/applications/environment/{} - Fetching applications", environmentId);
        List<ApplicationDTO> applications = applicationService.getApplicationsByEnvironment(environmentId);
        return ResponseEntity.ok(applications);
    }
}
