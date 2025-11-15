package com.paas.api.controller;

import com.paas.common.dto.EnvironmentDTO;
import com.paas.core.service.EnvironmentService;
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
@RequestMapping("/api/v1/environments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Environments", description = "Environment management endpoints")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @GetMapping
    @Operation(summary = "Get all environments")
    public ResponseEntity<List<EnvironmentDTO>> getAllEnvironments() {
        log.info("GET /api/v1/environments - Fetching all environments");
        List<EnvironmentDTO> environments = environmentService.getAllEnvironments();
        return ResponseEntity.ok(environments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get environment by ID")
    public ResponseEntity<EnvironmentDTO> getEnvironmentById(@PathVariable Long id) {
        log.info("GET /api/v1/environments/{} - Fetching environment", id);
        EnvironmentDTO environment = environmentService.getEnvironmentById(id);
        return ResponseEntity.ok(environment);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get environment by name")
    public ResponseEntity<EnvironmentDTO> getEnvironmentByName(@PathVariable String name) {
        log.info("GET /api/v1/environments/name/{} - Fetching environment", name);
        EnvironmentDTO environment = environmentService.getEnvironmentByName(name);
        return ResponseEntity.ok(environment);
    }

    @PostMapping
    @Operation(summary = "Create new environment")
    public ResponseEntity<EnvironmentDTO> createEnvironment(@Valid @RequestBody EnvironmentDTO environmentDTO) {
        log.info("POST /api/v1/environments - Creating environment: {}", environmentDTO.getName());
        EnvironmentDTO createdEnvironment = environmentService.createEnvironment(environmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEnvironment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update environment")
    public ResponseEntity<EnvironmentDTO> updateEnvironment(
            @PathVariable Long id,
            @Valid @RequestBody EnvironmentDTO environmentDTO) {
        log.info("PUT /api/v1/environments/{} - Updating environment", id);
        EnvironmentDTO updatedEnvironment = environmentService.updateEnvironment(id, environmentDTO);
        return ResponseEntity.ok(updatedEnvironment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete environment")
    public ResponseEntity<Void> deleteEnvironment(@PathVariable Long id) {
        log.info("DELETE /api/v1/environments/{} - Deleting environment", id);
        environmentService.deleteEnvironment(id);
        return ResponseEntity.noContent().build();
    }
}
