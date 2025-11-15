package com.paas.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class DeploymentDTO {

    private Long id;
    private Long applicationId;
    private String applicationName;
    private String version;
    private String gitCommitSha;
    private String deploymentStrategy; // ROLLING, BLUE_GREEN, CANARY
    private String status; // PENDING, IN_PROGRESS, SUCCESS, FAILED, ROLLED_BACK
    private String triggeredBy;
    private String buildLog;
    private String deploymentLog;
    private Map<String, String> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
