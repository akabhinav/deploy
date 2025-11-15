package com.paas.core.repository;

import com.paas.common.enums.BuildStatus;
import com.paas.core.entity.BuildJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildJobRepository extends JpaRepository<BuildJob, Long> {

    List<BuildJob> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    List<BuildJob> findByStatus(BuildStatus status);

    List<BuildJob> findByApplicationIdAndStatus(Long applicationId, BuildStatus status);
}
