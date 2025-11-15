package com.paas.api.controller;

import com.paas.core.entity.AuditLog;
import com.paas.core.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Audit trail and compliance")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user")
    public ResponseEntity<Page<AuditLog>> getLogsByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        log.info("GET /api/v1/audit-logs/user/{}", userId);
        return ResponseEntity.ok(auditService.getLogsByUser(userId, pageable));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get audit logs by organization")
    public ResponseEntity<Page<AuditLog>> getLogsByOrganization(
            @PathVariable Long organizationId,
            Pageable pageable) {
        log.info("GET /api/v1/audit-logs/organization/{}", organizationId);
        return ResponseEntity.ok(auditService.getLogsByOrganization(organizationId, pageable));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    @Operation(summary = "Get audit logs by resource")
    public ResponseEntity<List<AuditLog>> getLogsByResource(
            @PathVariable String resourceType,
            @PathVariable Long resourceId) {
        log.info("GET /api/v1/audit-logs/resource/{}/{}", resourceType, resourceId);
        return ResponseEntity.ok(auditService.getLogsByResource(resourceType, resourceId));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action and time range")
    public ResponseEntity<List<AuditLog>> getLogsByActionAndTime(
            @PathVariable String action,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        log.info("GET /api/v1/audit-logs/action/{}", action);
        return ResponseEntity.ok(auditService.getLogsByActionAndTimeRange(action, startTime, endTime));
    }
}
