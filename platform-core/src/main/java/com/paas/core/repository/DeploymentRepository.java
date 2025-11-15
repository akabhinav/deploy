package com.paas.core.repository;

import com.paas.common.enums.DeploymentStatus;
import com.paas.core.entity.Deployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    List<Deployment> findByApplicationId(Long applicationId);

    Page<Deployment> findByApplicationId(Long applicationId, Pageable pageable);

    List<Deployment> findByStatus(DeploymentStatus status);

    Optional<Deployment> findFirstByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
