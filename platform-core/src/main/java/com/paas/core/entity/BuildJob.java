package com.paas.core.entity;

import com.paas.common.enums.BuildStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "build_jobs", indexes = {
    @Index(name = "idx_build_app", columnList = "application_id"),
    @Index(name = "idx_build_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class BuildJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "git_commit_sha", length = 100)
    private String gitCommitSha;

    @Column(name = "git_branch", length = 100)
    private String gitBranch;

    @Column(name = "repository_url", length = 500)
    private String repositoryUrl;

    @Column(name = "docker_image_tag", length = 255)
    private String dockerImageTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private BuildStatus status = BuildStatus.PENDING;

    @Column(name = "build_log", columnDefinition = "TEXT")
    private String buildLog;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "build_job_metadata",
                    joinColumns = @JoinColumn(name = "build_job_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 2000)
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
