package com.paas.core.repository;

import com.paas.common.enums.NotificationStatus;
import com.paas.core.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByOrganizationId(Long organizationId);

    List<Notification> findByStatus(NotificationStatus status);
}
