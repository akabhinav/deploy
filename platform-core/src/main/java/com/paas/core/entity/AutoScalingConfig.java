package com.paas.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "autoscaling_configs", indexes = {
    @Index(name = "idx_autoscale_app", columnList = "application_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AutoScalingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "min_replicas", nullable = false)
    @Builder.Default
    private Integer minReplicas = 1;

    @Column(name = "max_replicas", nullable = false)
    @Builder.Default
    private Integer maxReplicas = 10;

    @Column(name = "target_cpu_utilization")
    @Builder.Default
    private Integer targetCpuUtilization = 80;

    @Column(name = "target_memory_utilization")
    @Builder.Default
    private Integer targetMemoryUtilization = 80;

    @Column(name = "scale_up_cooldown_seconds")
    @Builder.Default
    private Integer scaleUpCooldownSeconds = 60;

    @Column(name = "scale_down_cooldown_seconds")
    @Builder.Default
    private Integer scaleDownCooldownSeconds = 300;

    @Column(name = "custom_metrics", columnDefinition = "TEXT")
    private String customMetrics;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
