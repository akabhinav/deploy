package com.paas.core.repository;

import com.paas.core.entity.AutoScalingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoScalingConfigRepository extends JpaRepository<AutoScalingConfig, Long> {

    Optional<AutoScalingConfig> findByApplicationId(Long applicationId);

    void deleteByApplicationId(Long applicationId);
}
