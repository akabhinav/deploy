package com.paas.api.controller;

import com.paas.common.enums.DatabaseType;
import com.paas.core.entity.ManagedDatabase;
import com.paas.core.service.DatabaseProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/databases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Managed Databases", description = "Database provisioning and management")
public class ManagedDatabaseController {

    private final DatabaseProvisioningService databaseService;

    @GetMapping
    @Operation(summary = "Get all databases")
    public ResponseEntity<List<ManagedDatabase>> getAllDatabases() {
        log.info("GET /api/v1/databases");
        return ResponseEntity.ok(databaseService.getAllDatabases());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get database by ID")
    public ResponseEntity<ManagedDatabase> getDatabaseById(@PathVariable Long id) {
        log.info("GET /api/v1/databases/{}", id);
        return ResponseEntity.ok(databaseService.getDatabaseById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get databases by project")
    public ResponseEntity<List<ManagedDatabase>> getDatabasesByProject(@PathVariable Long projectId) {
        log.info("GET /api/v1/databases/project/{}", projectId);
        return ResponseEntity.ok(databaseService.getDatabasesByProject(projectId));
    }

    @PostMapping
    @Operation(summary = "Provision new database")
    public ResponseEntity<ManagedDatabase> provisionDatabase(
            @RequestParam String name,
            @RequestParam DatabaseType type,
            @RequestParam String version,
            @RequestParam Long projectId,
            @RequestParam(required = false) Long environmentId,
            @RequestParam(defaultValue = "10") Integer storageSizeGb,
            @RequestParam(defaultValue = "true") Boolean backupEnabled,
            @RequestParam(defaultValue = "7") Integer backupRetentionDays,
            @RequestParam(defaultValue = "false") Boolean highAvailability) {

        log.info("POST /api/v1/databases - Provisioning database: {}", name);

        ManagedDatabase database = databaseService.provisionDatabase(
                name, type, version, projectId, environmentId,
                storageSizeGb, backupEnabled, backupRetentionDays, highAvailability
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(database);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete database")
    public ResponseEntity<Void> deleteDatabase(@PathVariable Long id) {
        log.info("DELETE /api/v1/databases/{}", id);
        databaseService.deleteDatabase(id);
        return ResponseEntity.noContent().build();
    }
}
