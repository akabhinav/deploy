package com.paas.core.entity;

import com.paas.common.enums.DeploymentStatus;
import com.paas.common.enums.DeploymentStrategy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "deployments", indexes = {
    @Index(name = "idx_deployment_app", columnList = "application_id"),
    @Index(name = "idx_deployment_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(length = 50)
    private String version;

    @Column(name = "git_commit_sha", length = 100)
    private String gitCommitSha;

    @Enumerated(EnumType.STRING)
    @Column(name = "deployment_strategy", nullable = false)
    @Builder.Default
    private DeploymentStrategy deploymentStrategy = DeploymentStrategy.ROLLING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private DeploymentStatus status = DeploymentStatus.PENDING;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    @Column(name = "build_log", columnDefinition = "TEXT")
    private String buildLog;

    @Column(name = "deployment_log", columnDefinition = "TEXT")
    private String deploymentLog;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "deployment_metadata",
                    joinColumns = @JoinColumn(name = "deployment_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 2000)
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
