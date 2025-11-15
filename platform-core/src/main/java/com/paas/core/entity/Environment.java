package com.paas.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "environments", indexes = {
    @Index(name = "idx_env_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Environment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, unique = true, length = 100)
    private String namespace;

    @Column(name = "cluster_id", length = 100)
    private String clusterId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "environment_variables",
                    joinColumns = @JoinColumn(name = "environment_id"))
    @MapKeyColumn(name = "env_key")
    @Column(name = "env_value", length = 2000)
    @Builder.Default
    private Map<String, String> environmentVariables = new HashMap<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "environment_secrets",
                    joinColumns = @JoinColumn(name = "environment_id"))
    @MapKeyColumn(name = "secret_key")
    @Column(name = "secret_value", length = 2000)
    @Builder.Default
    private Map<String, String> secrets = new HashMap<>();

    @Column(name = "auto_deploy_enabled")
    @Builder.Default
    private Boolean autoDeployEnabled = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
