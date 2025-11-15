package com.paas.core.entity;

import com.paas.common.enums.ApplicationType;
import com.paas.common.enums.SourceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "applications", indexes = {
    @Index(name = "idx_app_name", columnList = "name"),
    @Index(name = "idx_app_environment", columnList = "environment_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false)
    private ApplicationType applicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "repository_url", length = 500)
    private String repositoryUrl;

    @Column(length = 100)
    private String branch;

    @Column(name = "docker_image", length = 500)
    private String dockerImage;

    @Column(name = "build_command", length = 1000)
    private String buildCommand;

    @Column(name = "start_command", length = 1000)
    private String startCommand;

    @Column
    private Integer port;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "application_env_vars",
                    joinColumns = @JoinColumn(name = "application_id"))
    @MapKeyColumn(name = "env_key")
    @Column(name = "env_value", length = 2000)
    @Builder.Default
    private Map<String, String> environmentVariables = new HashMap<>();

    @Column(name = "cpu_request", length = 20)
    private String cpuRequest;

    @Column(name = "cpu_limit", length = 20)
    private String cpuLimit;

    @Column(name = "memory_request", length = 20)
    private String memoryRequest;

    @Column(name = "memory_limit", length = 20)
    private String memoryLimit;

    @Column(length = 50)
    private String status;

    @Column(name = "environment_id")
    private Long environmentId;

    @Builder.Default
    @Column(nullable = false)
    private Integer replicas = 1;

    @Column(name = "health_check_path", length = 200)
    private String healthCheckPath;

    @Column(name = "health_check_port")
    private Integer healthCheckPort;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
