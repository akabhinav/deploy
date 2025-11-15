package com.paas.core.service;

import com.paas.common.dto.ApplicationDTO;
import com.paas.core.entity.Application;
import com.paas.core.mapper.ApplicationMapper;
import com.paas.core.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    @Transactional(readOnly = true)
    public List<ApplicationDTO> getAllApplications() {
        log.info("Fetching all applications");
        return applicationRepository.findAll().stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationDTO getApplicationById(Long id) {
        log.info("Fetching application with id: {}", id);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        return applicationMapper.toDTO(application);
    }

    @Transactional(readOnly = true)
    public ApplicationDTO getApplicationByName(String name) {
        log.info("Fetching application with name: {}", name);
        Application application = applicationRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Application not found with name: " + name));
        return applicationMapper.toDTO(application);
    }

    @Transactional
    public ApplicationDTO createApplication(ApplicationDTO applicationDTO) {
        log.info("Creating new application: {}", applicationDTO.getName());

        if (applicationRepository.existsByName(applicationDTO.getName())) {
            throw new RuntimeException("Application already exists with name: " + applicationDTO.getName());
        }

        Application application = applicationMapper.toEntity(applicationDTO);
        application.setStatus("INACTIVE");
        Application savedApplication = applicationRepository.save(application);

        log.info("Application created with id: {}", savedApplication.getId());
        return applicationMapper.toDTO(savedApplication);
    }

    @Transactional
    public ApplicationDTO updateApplication(Long id, ApplicationDTO applicationDTO) {
        log.info("Updating application with id: {}", id);

        Application existingApplication = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));

        // Update fields
        if (applicationDTO.getDescription() != null) {
            existingApplication.setDescription(applicationDTO.getDescription());
        }
        if (applicationDTO.getRepositoryUrl() != null) {
            existingApplication.setRepositoryUrl(applicationDTO.getRepositoryUrl());
        }
        if (applicationDTO.getBranch() != null) {
            existingApplication.setBranch(applicationDTO.getBranch());
        }
        if (applicationDTO.getDockerImage() != null) {
            existingApplication.setDockerImage(applicationDTO.getDockerImage());
        }
        if (applicationDTO.getBuildCommand() != null) {
            existingApplication.setBuildCommand(applicationDTO.getBuildCommand());
        }
        if (applicationDTO.getStartCommand() != null) {
            existingApplication.setStartCommand(applicationDTO.getStartCommand());
        }
        if (applicationDTO.getPort() != null) {
            existingApplication.setPort(applicationDTO.getPort());
        }
        if (applicationDTO.getEnvironmentVariables() != null) {
            existingApplication.setEnvironmentVariables(applicationDTO.getEnvironmentVariables());
        }
        if (applicationDTO.getReplicas() != null) {
            existingApplication.setReplicas(applicationDTO.getReplicas());
        }

        // Update resources if provided
        if (applicationDTO.getResources() != null) {
            existingApplication.setCpuRequest(applicationDTO.getResources().getCpuRequest());
            existingApplication.setCpuLimit(applicationDTO.getResources().getCpuLimit());
            existingApplication.setMemoryRequest(applicationDTO.getResources().getMemoryRequest());
            existingApplication.setMemoryLimit(applicationDTO.getResources().getMemoryLimit());
        }

        Application updatedApplication = applicationRepository.save(existingApplication);
        log.info("Application updated successfully");
        return applicationMapper.toDTO(updatedApplication);
    }

    @Transactional
    public void deleteApplication(Long id) {
        log.info("Deleting application with id: {}", id);

        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("Application not found with id: " + id);
        }

        applicationRepository.deleteById(id);
        log.info("Application deleted successfully");
    }

    @Transactional(readOnly = true)
    public List<ApplicationDTO> getApplicationsByEnvironment(Long environmentId) {
        log.info("Fetching applications for environment: {}", environmentId);
        return applicationRepository.findByEnvironmentId(environmentId).stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
    }
}
