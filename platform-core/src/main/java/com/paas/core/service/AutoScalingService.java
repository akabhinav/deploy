package com.paas.core.service;

import com.paas.core.entity.AutoScalingConfig;
import com.paas.core.repository.AutoScalingConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoScalingService {

    private final AutoScalingConfigRepository autoScalingConfigRepository;

    @Transactional
    public AutoScalingConfig createOrUpdateConfig(AutoScalingConfig config) {
        log.info("Creating/Updating autoscaling config for application: {}", config.getApplicationId());

        Optional<AutoScalingConfig> existing = autoScalingConfigRepository
                .findByApplicationId(config.getApplicationId());

        if (existing.isPresent()) {
            AutoScalingConfig existingConfig = existing.get();
            existingConfig.setEnabled(config.getEnabled());
            existingConfig.setMinReplicas(config.getMinReplicas());
            existingConfig.setMaxReplicas(config.getMaxReplicas());
            existingConfig.setTargetCpuUtilization(config.getTargetCpuUtilization());
            existingConfig.setTargetMemoryUtilization(config.getTargetMemoryUtilization());
            existingConfig.setScaleUpCooldownSeconds(config.getScaleUpCooldownSeconds());
            existingConfig.setScaleDownCooldownSeconds(config.getScaleDownCooldownSeconds());
            return autoScalingConfigRepository.save(existingConfig);
        }

        return autoScalingConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    public AutoScalingConfig getConfigByApplicationId(Long applicationId) {
        return autoScalingConfigRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new RuntimeException("AutoScaling config not found for application: " + applicationId));
    }

    @Transactional(readOnly = true)
    public List<AutoScalingConfig> getAllConfigs() {
        return autoScalingConfigRepository.findAll();
    }

    @Transactional
    public void deleteConfig(Long id) {
        log.info("Deleting autoscaling config: {}", id);
        autoScalingConfigRepository.deleteById(id);
    }

    @Transactional
    public void enableAutoScaling(Long applicationId) {
        AutoScalingConfig config = getConfigByApplicationId(applicationId);
        config.setEnabled(true);
        autoScalingConfigRepository.save(config);
        log.info("Auto-scaling enabled for application: {}", applicationId);
    }

    @Transactional
    public void disableAutoScaling(Long applicationId) {
        AutoScalingConfig config = getConfigByApplicationId(applicationId);
        config.setEnabled(false);
        autoScalingConfigRepository.save(config);
        log.info("Auto-scaling disabled for application: {}", applicationId);
    }
}
