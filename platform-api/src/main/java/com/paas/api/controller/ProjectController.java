package com.paas.api.controller;

import com.paas.common.dto.ProjectDTO;
import com.paas.core.entity.Project;
import com.paas.core.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Projects", description = "Project management")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        log.info("GET /api/v1/projects - Fetching all projects");
        List<ProjectDTO> projects = projectService.getAllProjects().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        log.info("GET /api/v1/projects/{}", id);
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(toDTO(project));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get project by slug")
    public ResponseEntity<ProjectDTO> getProjectBySlug(@PathVariable String slug) {
        log.info("GET /api/v1/projects/slug/{}", slug);
        Project project = projectService.getProjectBySlug(slug);
        return ResponseEntity.ok(toDTO(project));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get projects by organization")
    public ResponseEntity<List<ProjectDTO>> getProjectsByOrganization(@PathVariable Long organizationId) {
        log.info("GET /api/v1/projects/organization/{}", organizationId);
        List<ProjectDTO> projects = projectService.getProjectsByOrganization(organizationId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    @Operation(summary = "Create project")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO dto) {
        log.info("POST /api/v1/projects - Creating project: {}", dto.getName());

        Project project = projectService.createProject(
                dto.getName(),
                dto.getSlug(),
                dto.getDescription(),
                dto.getOrganizationId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(project));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO dto) {
        log.info("PUT /api/v1/projects/{}", id);

        Project project = projectService.updateProject(
                id,
                dto.getName(),
                dto.getDescription()
        );

        return ResponseEntity.ok(toDTO(project));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("DELETE /api/v1/projects/{}", id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    private ProjectDTO toDTO(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .slug(project.getSlug())
                .description(project.getDescription())
                .organizationId(project.getOrganization().getId())
                .defaultEnvironmentId(project.getDefaultEnvironmentId())
                .active(project.getActive())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
