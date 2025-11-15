package com.paas.api.controller;

import com.paas.core.service.KubernetesDeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/kubernetes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kubernetes", description = "Kubernetes deployment operations")
public class KubernetesController {

    private final KubernetesDeploymentService k8sService;

    @PostMapping("/deploy/{applicationId}")
    @Operation(summary = "Deploy application to Kubernetes")
    public ResponseEntity<Void> deployToKubernetes(
            @PathVariable Long applicationId,
            @RequestParam String namespace) {
        log.info("POST /api/v1/kubernetes/deploy/{} to namespace: {}", applicationId, namespace);
        k8sService.deployToKubernetes(applicationId, namespace);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{applicationId}")
    @Operation(summary = "Delete application from Kubernetes")
    public ResponseEntity<Void> deleteFromKubernetes(
            @PathVariable Long applicationId,
            @RequestParam String namespace) {
        log.info("DELETE /api/v1/kubernetes/delete/{} from namespace: {}", applicationId, namespace);
        k8sService.deleteFromKubernetes(applicationId, namespace);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/scale/{applicationId}")
    @Operation(summary = "Scale application")
    public ResponseEntity<Void> scaleApplication(
            @PathVariable Long applicationId,
            @RequestParam String namespace,
            @RequestParam int replicas) {
        log.info("POST /api/v1/kubernetes/scale/{} to {} replicas", applicationId, replicas);
        k8sService.scaleApplication(applicationId, namespace, replicas);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{applicationId}")
    @Operation(summary = "Get deployment status")
    public ResponseEntity<Map<String, Object>> getDeploymentStatus(
            @PathVariable Long applicationId,
            @RequestParam String namespace) {
        log.info("GET /api/v1/kubernetes/status/{}", applicationId);
        Map<String, Object> status = k8sService.getDeploymentStatus(applicationId, namespace);
        return ResponseEntity.ok(status);
    }
}
