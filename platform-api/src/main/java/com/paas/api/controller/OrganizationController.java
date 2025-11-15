package com.paas.api.controller;

import com.paas.common.dto.OrganizationDTO;
import com.paas.common.enums.Role;
import com.paas.core.entity.Organization;
import com.paas.core.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organizations", description = "Organization management")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @Operation(summary = "Get all organizations")
    public ResponseEntity<List<OrganizationDTO>> getAllOrganizations() {
        log.info("GET /api/v1/organizations - Fetching all organizations");
        List<OrganizationDTO> orgs = organizationService.getAllOrganizations().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orgs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable Long id) {
        log.info("GET /api/v1/organizations/{}", id);
        Organization org = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(toDTO(org));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get organization by slug")
    public ResponseEntity<OrganizationDTO> getOrganizationBySlug(@PathVariable String slug) {
        log.info("GET /api/v1/organizations/slug/{}", slug);
        Organization org = organizationService.getOrganizationBySlug(slug);
        return ResponseEntity.ok(toDTO(org));
    }

    @PostMapping
    @Operation(summary = "Create organization")
    public ResponseEntity<OrganizationDTO> createOrganization(
            @Valid @RequestBody OrganizationDTO dto,
            @RequestParam Long ownerUserId) {
        log.info("POST /api/v1/organizations - Creating organization: {}", dto.getName());

        Organization org = organizationService.createOrganization(
                dto.getName(),
                dto.getSlug(),
                dto.getDescription(),
                ownerUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(org));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    public ResponseEntity<OrganizationDTO> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationDTO dto) {
        log.info("PUT /api/v1/organizations/{}", id);

        Organization org = organizationService.updateOrganization(
                id,
                dto.getName(),
                dto.getDescription()
        );

        return ResponseEntity.ok(toDTO(org));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to organization")
    public ResponseEntity<Void> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam Role role) {
        log.info("POST /api/v1/organizations/{}/members - Adding user: {}", id, userId);
        organizationService.addMember(id, userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from organization")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        log.info("DELETE /api/v1/organizations/{}/members/{}", id, userId);
        organizationService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/members/{userId}/role")
    @Operation(summary = "Update member role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam Role role) {
        log.info("PUT /api/v1/organizations/{}/members/{}/role", id, userId);
        organizationService.updateMemberRole(id, userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate organization")
    public ResponseEntity<Void> deactivateOrganization(@PathVariable Long id) {
        log.info("DELETE /api/v1/organizations/{}", id);
        organizationService.deactivateOrganization(id);
        return ResponseEntity.noContent().build();
    }

    private OrganizationDTO toDTO(Organization org) {
        return OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .description(org.getDescription())
                .billingEmail(org.getBillingEmail())
                .maxProjects(org.getMaxProjects())
                .maxApplications(org.getMaxApplications())
                .active(org.getActive())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}
