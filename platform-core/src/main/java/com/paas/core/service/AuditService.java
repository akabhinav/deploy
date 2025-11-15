package com.paas.core.service;

import com.paas.core.entity.AuditLog;
import com.paas.core.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(
            Long userId,
            Long organizationId,
            String action,
            String resourceType,
            Long resourceId,
            String description,
            String ipAddress,
            String userAgent,
            boolean success,
            String errorMessage,
            Map<String, String> metadata) {

        log.debug("Logging audit action: {} for resource: {}/{}", action, resourceType, resourceId);

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .organizationId(organizationId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .errorMessage(errorMessage)
                .metadata(metadata != null ? metadata : Map.of())
                .build();

        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void logAction(Long userId, String action, String resourceType, Long resourceId, String description) {
        logAction(userId, null, action, resourceType, resourceId, description, null, null, true, null, null);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByOrganization(Long organizationId, Pageable pageable) {
        return auditLogRepository.findByOrganizationId(organizationId, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByResource(String resourceType, Long resourceId) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByActionAndTimeRange(String action, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByActionAndTimestampBetween(action, startTime, endTime);
    }
}
