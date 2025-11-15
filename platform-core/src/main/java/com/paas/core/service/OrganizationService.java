package com.paas.core.service;

import com.paas.common.enums.Role;
import com.paas.core.entity.Organization;
import com.paas.core.entity.OrganizationMember;
import com.paas.core.entity.User;
import com.paas.core.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    @Transactional
    public Organization createOrganization(String name, String slug, String description, Long ownerUserId) {
        log.info("Creating organization: {}", name);

        if (organizationRepository.existsBySlug(slug)) {
            throw new RuntimeException("Organization slug already exists: " + slug);
        }

        User owner = userService.getUserById(ownerUserId);

        Organization organization = Organization.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .maxProjects(10)
                .maxApplications(50)
                .active(true)
                .build();

        // Add owner as first member
        OrganizationMember ownerMember = OrganizationMember.builder()
                .organization(organization)
                .user(owner)
                .role(Role.OWNER)
                .build();

        organization.getMembers().add(ownerMember);

        Organization savedOrg = organizationRepository.save(organization);
        log.info("Organization created with id: {}", savedOrg.getId());
        return savedOrg;
    }

    @Transactional(readOnly = true)
    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + id));
    }

    @Transactional(readOnly = true)
    public Organization getOrganizationBySlug(String slug) {
        return organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + slug));
    }

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Transactional
    public Organization updateOrganization(Long id, String name, String description) {
        Organization org = getOrganizationById(id);

        if (name != null) {
            org.setName(name);
        }
        if (description != null) {
            org.setDescription(description);
        }

        return organizationRepository.save(org);
    }

    @Transactional
    public void addMember(Long organizationId, Long userId, Role role) {
        log.info("Adding member {} to organization {} with role {}", userId, organizationId, role);

        Organization org = getOrganizationById(organizationId);
        User user = userService.getUserById(userId);

        // Check if user is already a member
        boolean alreadyMember = org.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));

        if (alreadyMember) {
            throw new RuntimeException("User is already a member of this organization");
        }

        OrganizationMember member = OrganizationMember.builder()
                .organization(org)
                .user(user)
                .role(role)
                .build();

        org.getMembers().add(member);
        organizationRepository.save(org);
        log.info("Member added successfully");
    }

    @Transactional
    public void removeMember(Long organizationId, Long userId) {
        log.info("Removing member {} from organization {}", userId, organizationId);

        Organization org = getOrganizationById(organizationId);

        org.getMembers().removeIf(m -> m.getUser().getId().equals(userId));
        organizationRepository.save(org);
        log.info("Member removed successfully");
    }

    @Transactional
    public void updateMemberRole(Long organizationId, Long userId, Role newRole) {
        log.info("Updating role for member {} in organization {} to {}", userId, organizationId, newRole);

        Organization org = getOrganizationById(organizationId);

        org.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .ifPresentOrElse(
                        member -> member.setRole(newRole),
                        () -> { throw new RuntimeException("User is not a member of this organization"); }
                );

        organizationRepository.save(org);
        log.info("Member role updated successfully");
    }

    @Transactional
    public void deactivateOrganization(Long id) {
        Organization org = getOrganizationById(id);
        org.setActive(false);
        organizationRepository.save(org);
        log.info("Organization deactivated: {}", id);
    }
}
