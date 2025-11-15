package com.paas.core.service;

import com.paas.common.dto.EnvironmentDTO;
import com.paas.core.entity.Environment;
import com.paas.core.mapper.EnvironmentMapper;
import com.paas.core.repository.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentMapper environmentMapper;

    @Transactional(readOnly = true)
    public List<EnvironmentDTO> getAllEnvironments() {
        log.info("Fetching all environments");
        return environmentRepository.findAll().stream()
                .map(environmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnvironmentDTO getEnvironmentById(Long id) {
        log.info("Fetching environment with id: {}", id);
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found with id: " + id));
        return environmentMapper.toDTO(environment);
    }

    @Transactional(readOnly = true)
    public EnvironmentDTO getEnvironmentByName(String name) {
        log.info("Fetching environment with name: {}", name);
        Environment environment = environmentRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Environment not found with name: " + name));
        return environmentMapper.toDTO(environment);
    }

    @Transactional
    public EnvironmentDTO createEnvironment(EnvironmentDTO environmentDTO) {
        log.info("Creating new environment: {}", environmentDTO.getName());

        if (environmentRepository.existsByName(environmentDTO.getName())) {
            throw new RuntimeException("Environment already exists with name: " + environmentDTO.getName());
        }

        if (environmentRepository.existsByNamespace(environmentDTO.getNamespace())) {
            throw new RuntimeException("Environment already exists with namespace: " + environmentDTO.getNamespace());
        }

        Environment environment = environmentMapper.toEntity(environmentDTO);
        Environment savedEnvironment = environmentRepository.save(environment);

        log.info("Environment created with id: {}", savedEnvironment.getId());
        return environmentMapper.toDTO(savedEnvironment);
    }

    @Transactional
    public EnvironmentDTO updateEnvironment(Long id, EnvironmentDTO environmentDTO) {
        log.info("Updating environment with id: {}", id);

        Environment existingEnvironment = environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found with id: " + id));

        if (environmentDTO.getDescription() != null) {
            existingEnvironment.setDescription(environmentDTO.getDescription());
        }
        if (environmentDTO.getEnvironmentVariables() != null) {
            existingEnvironment.setEnvironmentVariables(environmentDTO.getEnvironmentVariables());
        }
        if (environmentDTO.getAutoDeployEnabled() != null) {
            existingEnvironment.setAutoDeployEnabled(environmentDTO.getAutoDeployEnabled());
        }

        Environment updatedEnvironment = environmentRepository.save(existingEnvironment);
        log.info("Environment updated successfully");
        return environmentMapper.toDTO(updatedEnvironment);
    }

    @Transactional
    public void deleteEnvironment(Long id) {
        log.info("Deleting environment with id: {}", id);

        if (!environmentRepository.existsById(id)) {
            throw new RuntimeException("Environment not found with id: " + id);
        }

        environmentRepository.deleteById(id);
        log.info("Environment deleted successfully");
    }
}
