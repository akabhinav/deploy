package com.paas.core.repository;

import com.paas.core.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    List<AuditLog> findByActionAndTimestampBetween(
            String action,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId);
}
