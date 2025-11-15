package com.paas.api.controller;

import com.paas.common.dto.DeploymentDTO;
import com.paas.common.enums.DeploymentStatus;
import com.paas.core.service.DeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deployments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deployments", description = "Deployment management endpoints")
public class DeploymentController {

    private final DeploymentService deploymentService;

    @GetMapping
    @Operation(summary = "Get all deployments")
    public ResponseEntity<List<DeploymentDTO>> getAllDeployments() {
        log.info("GET /api/v1/deployments - Fetching all deployments");
        List<DeploymentDTO> deployments = deploymentService.getAllDeployments();
        return ResponseEntity.ok(deployments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get deployment by ID")
    public ResponseEntity<DeploymentDTO> getDeploymentById(@PathVariable Long id) {
        log.info("GET /api/v1/deployments/{} - Fetching deployment", id);
        DeploymentDTO deployment = deploymentService.getDeploymentById(id);
        return ResponseEntity.ok(deployment);
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get deployments by application")
    public ResponseEntity<List<DeploymentDTO>> getDeploymentsByApplication(@PathVariable Long applicationId) {
        log.info("GET /api/v1/deployments/application/{} - Fetching deployments", applicationId);
        List<DeploymentDTO> deployments = deploymentService.getDeploymentsByApplication(applicationId);
        return ResponseEntity.ok(deployments);
    }

    @GetMapping("/application/{applicationId}/paginated")
    @Operation(summary = "Get deployments by application with pagination")
    public ResponseEntity<Page<DeploymentDTO>> getDeploymentsByApplicationPaginated(
            @PathVariable Long applicationId,
            Pageable pageable) {
        log.info("GET /api/v1/deployments/application/{}/paginated - Fetching deployments", applicationId);
        Page<DeploymentDTO> deployments = deploymentService.getDeploymentsByApplication(applicationId, pageable);
        return ResponseEntity.ok(deployments);
    }

    @PostMapping
    @Operation(summary = "Create new deployment")
    public ResponseEntity<DeploymentDTO> createDeployment(@Valid @RequestBody DeploymentDTO deploymentDTO) {
        log.info("POST /api/v1/deployments - Creating deployment for application: {}", deploymentDTO.getApplicationId());
        DeploymentDTO createdDeployment = deploymentService.createDeployment(deploymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDeployment);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update deployment status")
    public ResponseEntity<DeploymentDTO> updateDeploymentStatus(
            @PathVariable Long id,
            @RequestParam DeploymentStatus status) {
        log.info("PATCH /api/v1/deployments/{}/status - Updating status to: {}", id, status);
        DeploymentDTO updatedDeployment = deploymentService.updateDeploymentStatus(id, status);
        return ResponseEntity.ok(updatedDeployment);
    }
}
