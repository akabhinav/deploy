package com.paas.api.controller;

import com.paas.core.entity.BuildJob;
import com.paas.core.service.DockerBuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/builds")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Build Jobs", description = "Docker build management")
public class BuildJobController {

    private final DockerBuildService dockerBuildService;

    @PostMapping
    @Operation(summary = "Trigger new build")
    public ResponseEntity<BuildJob> triggerBuild(
            @RequestParam Long applicationId,
            @RequestParam String commitSha) {
        log.info("POST /api/v1/builds - Triggering build for app {} at commit {}", applicationId, commitSha);
        BuildJob buildJob = dockerBuildService.buildFromGit(applicationId, commitSha);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildJob);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get build job by ID")
    public ResponseEntity<BuildJob> getBuildJob(@PathVariable Long id) {
        log.info("GET /api/v1/builds/{}", id);
        return ResponseEntity.ok(dockerBuildService.getBuildJob(id));
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get builds by application")
    public ResponseEntity<List<BuildJob>> getBuildsByApplication(@PathVariable Long applicationId) {
        log.info("GET /api/v1/builds/application/{}", applicationId);
        return ResponseEntity.ok(dockerBuildService.getBuildsByApplication(applicationId));
    }
}
