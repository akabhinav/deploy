package com.paas.core.entity;

import com.paas.common.enums.DatabaseType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "managed_databases", indexes = {
    @Index(name = "idx_db_name", columnList = "name"),
    @Index(name = "idx_db_project", columnList = "project_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ManagedDatabase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "database_type", nullable = false)
    private DatabaseType databaseType;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "environment_id")
    private Long environmentId;

    @Column(name = "connection_string", length = 500)
    private String connectionString;

    @Column(length = 100)
    private String host;

    @Column
    private Integer port;

    @Column(name = "database_name", length = 100)
    private String databaseName;

    @Column(length = 100)
    private String username;

    @Column(name = "password_secret", length = 255)
    private String passwordSecret;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "database_config",
                    joinColumns = @JoinColumn(name = "database_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value", length = 1000)
    @Builder.Default
    private Map<String, String> configuration = new HashMap<>();

    @Column(name = "storage_size_gb")
    private Integer storageSizeGb;

    @Column(name = "backup_enabled")
    @Builder.Default
    private Boolean backupEnabled = true;

    @Column(name = "backup_retention_days")
    @Builder.Default
    private Integer backupRetentionDays = 7;

    @Column(name = "high_availability")
    @Builder.Default
    private Boolean highAvailability = false;

    @Column(length = 50)
    @Builder.Default
    private String status = "PROVISIONING";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
