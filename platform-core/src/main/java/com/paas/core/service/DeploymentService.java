package com.paas.core.service;

import com.paas.common.dto.DeploymentDTO;
import com.paas.common.enums.DeploymentStatus;
import com.paas.core.entity.Deployment;
import com.paas.core.mapper.DeploymentMapper;
import com.paas.core.repository.DeploymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentService {

    private final DeploymentRepository deploymentRepository;
    private final DeploymentMapper deploymentMapper;

    @Transactional(readOnly = true)
    public List<DeploymentDTO> getAllDeployments() {
        log.info("Fetching all deployments");
        return deploymentRepository.findAll().stream()
                .map(deploymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeploymentDTO getDeploymentById(Long id) {
        log.info("Fetching deployment with id: {}", id);
        Deployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found with id: " + id));
        return deploymentMapper.toDTO(deployment);
    }

    @Transactional(readOnly = true)
    public List<DeploymentDTO> getDeploymentsByApplication(Long applicationId) {
        log.info("Fetching deployments for application: {}", applicationId);
        return deploymentRepository.findByApplicationId(applicationId).stream()
                .map(deploymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DeploymentDTO> getDeploymentsByApplication(Long applicationId, Pageable pageable) {
        log.info("Fetching deployments for application: {} with pagination", applicationId);
        return deploymentRepository.findByApplicationId(applicationId, pageable)
                .map(deploymentMapper::toDTO);
    }

    @Transactional
    public DeploymentDTO createDeployment(DeploymentDTO deploymentDTO) {
        log.info("Creating new deployment for application: {}", deploymentDTO.getApplicationId());

        Deployment deployment = deploymentMapper.toEntity(deploymentDTO);
        deployment.setStatus(DeploymentStatus.PENDING);
        deployment.setStartedAt(LocalDateTime.now());

        Deployment savedDeployment = deploymentRepository.save(deployment);
        log.info("Deployment created with id: {}", savedDeployment.getId());

        return deploymentMapper.toDTO(savedDeployment);
    }

    @Transactional
    public DeploymentDTO updateDeploymentStatus(Long id, DeploymentStatus status) {
        log.info("Updating deployment {} status to: {}", id, status);

        Deployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found with id: " + id));

        deployment.setStatus(status);

        if (status == DeploymentStatus.SUCCESS || status == DeploymentStatus.FAILED) {
            deployment.setCompletedAt(LocalDateTime.now());
        }

        Deployment updatedDeployment = deploymentRepository.save(deployment);
        return deploymentMapper.toDTO(updatedDeployment);
    }

    @Transactional
    public void addBuildLog(Long id, String logEntry) {
        Deployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found with id: " + id));

        String currentLog = deployment.getBuildLog() != null ? deployment.getBuildLog() : "";
        deployment.setBuildLog(currentLog + "\n" + logEntry);
        deploymentRepository.save(deployment);
    }

    @Transactional
    public void addDeploymentLog(Long id, String logEntry) {
        Deployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found with id: " + id));

        String currentLog = deployment.getDeploymentLog() != null ? deployment.getDeploymentLog() : "";
        deployment.setDeploymentLog(currentLog + "\n" + logEntry);
        deploymentRepository.save(deployment);
    }
}
