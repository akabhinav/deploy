package com.paas.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequirementsDTO {
    private String cpuRequest;     // e.g., "100m", "1"
    private String cpuLimit;       // e.g., "500m", "2"
    private String memoryRequest;  // e.g., "128Mi", "1Gi"
    private String memoryLimit;    // e.g., "512Mi", "2Gi"
}
