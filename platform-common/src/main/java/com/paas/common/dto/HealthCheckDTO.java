package com.paas.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckDTO {
    private String path;           // e.g., "/health", "/api/health"
    private Integer port;
    private Integer initialDelaySeconds;
    private Integer periodSeconds;
    private Integer timeoutSeconds;
    private Integer successThreshold;
    private Integer failureThreshold;
}
