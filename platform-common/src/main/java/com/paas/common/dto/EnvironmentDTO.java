package com.paas.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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
public class EnvironmentDTO {

    private Long id;

    @NotBlank(message = "Environment name is required")
    private String name; // development, staging, production, preview

    private String description;

    private String namespace; // Kubernetes namespace

    private String clusterId;

    private Map<String, String> environmentVariables;

    private Map<String, String> secrets;

    private Boolean autoDeployEnabled;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
