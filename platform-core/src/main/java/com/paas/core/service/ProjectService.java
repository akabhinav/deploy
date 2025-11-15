package com.paas.core.service;

import com.paas.core.entity.Organization;
import com.paas.core.entity.Project;
import com.paas.core.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationService organizationService;

    @Transactional
    public Project createProject(String name, String slug, String description, Long organizationId) {
        log.info("Creating project: {} in organization: {}", name, organizationId);

        if (projectRepository.existsBySlug(slug)) {
            throw new RuntimeException("Project slug already exists: " + slug);
        }

        Organization organization = organizationService.getOrganizationById(organizationId);

        Project project = Project.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .organization(organization)
                .active(true)
                .build();

        Project savedProject = projectRepository.save(project);
        log.info("Project created with id: {}", savedProject.getId());
        return savedProject;
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
    }

    @Transactional(readOnly = true)
    public Project getProjectBySlug(String slug) {
        return projectRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Project not found: " + slug));
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByOrganization(Long organizationId) {
        return projectRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional
    public Project updateProject(Long id, String name, String description) {
        Project project = getProjectById(id);

        if (name != null) {
            project.setName(name);
        }
        if (description != null) {
            project.setDescription(description);
        }

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project: {}", id);
        projectRepository.deleteById(id);
    }
}
