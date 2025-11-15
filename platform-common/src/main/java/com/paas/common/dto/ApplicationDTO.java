package com.paas.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {

    private Long id;

    @NotBlank(message = "Application name is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Name must contain only lowercase letters, numbers, and hyphens")
    private String name;

    private String description;

    private String applicationType; // WEB_SERVICE, WORKER, CRON_JOB, STATIC_SITE

    private String sourceType; // GIT, DOCKER_REGISTRY, DOCKERFILE, BUILDPACK

    private String repositoryUrl;

    private String branch;

    private String dockerImage;

    private String buildCommand;

    private String startCommand;

    private Integer port;

    private Map<String, String> environmentVariables;

    private ResourceRequirementsDTO resources;

    private String status; // ACTIVE, INACTIVE, DEPLOYING, FAILED

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String environmentId;

    private Integer replicas;

    private HealthCheckDTO healthCheck;
}
