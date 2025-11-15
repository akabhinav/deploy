package com.paas.core.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.paas.common.enums.BuildStatus;
import com.paas.core.entity.Application;
import com.paas.core.entity.BuildJob;
import com.paas.core.repository.ApplicationRepository;
import com.paas.core.repository.BuildJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerBuildService {

    private final ApplicationRepository applicationRepository;
    private final BuildJobRepository buildJobRepository;
    private final GitService gitService;

    /**
     * Build Docker image from Git repository
     */
    public BuildJob buildFromGit(Long applicationId, String commitSha) {
        log.info("Starting build for application {} from commit {}", applicationId, commitSha);

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        // Create build job
        BuildJob buildJob = BuildJob.builder()
                .applicationId(applicationId)
                .gitCommitSha(commitSha)
                .gitBranch(app.getBranch())
                .repositoryUrl(app.getRepositoryUrl())
                .status(BuildStatus.PENDING)
                .triggeredBy("system")
                .build();

        buildJob = buildJobRepository.save(buildJob);

        // Start build in background
        final Long buildJobId = buildJob.getId();
        executeBuildAsync(buildJobId, app);

        return buildJob;
    }

    private void executeBuildAsync(Long buildJobId, Application app) {
        new Thread(() -> {
            try {
                executeBuild(buildJobId, app);
            } catch (Exception e) {
                log.error("Build failed for job {}: {}", buildJobId, e.getMessage(), e);
                updateBuildStatus(buildJobId, BuildStatus.FAILED, e.getMessage());
            }
        }).start();
    }

    private void executeBuild(Long buildJobId, Application app) {
        log.info("Executing build job: {}", buildJobId);

        BuildJob buildJob = buildJobRepository.findById(buildJobId)
                .orElseThrow(() -> new RuntimeException("Build job not found: " + buildJobId));

        try {
            // Update status to BUILDING
            buildJob.setStatus(BuildStatus.BUILDING);
            buildJob.setStartedAt(LocalDateTime.now());
            buildJobRepository.save(buildJob);

            // Clone repository
            log.info("Cloning repository: {}", app.getRepositoryUrl());
            File repoDir = gitService.cloneRepository(
                    app.getRepositoryUrl(),
                    app.getBranch(),
                    buildJob.getGitCommitSha()
            );

            // Build Docker image
            String imageTag = String.format("%s:%s",
                    app.getName(),
                    buildJob.getGitCommitSha().substring(0, 7));

            log.info("Building Docker image: {}", imageTag);
            String buildLog = buildDockerImage(repoDir, imageTag);

            // Update build job
            buildJob.setDockerImageTag(imageTag);
            buildJob.setBuildLog(buildLog);
            buildJob.setStatus(BuildStatus.SUCCESS);
            buildJob.setCompletedAt(LocalDateTime.now());
            buildJob.setDurationSeconds(
                    (int) Duration.between(buildJob.getStartedAt(), buildJob.getCompletedAt()).getSeconds()
            );

            buildJobRepository.save(buildJob);

            log.info("Build completed successfully: {}", buildJobId);

            // Clean up
            gitService.cleanup(repoDir);

        } catch (Exception e) {
            log.error("Build execution failed: {}", e.getMessage(), e);
            buildJob.setStatus(BuildStatus.FAILED);
            buildJob.setBuildLog(buildJob.getBuildLog() + "\nError: " + e.getMessage());
            buildJob.setCompletedAt(LocalDateTime.now());
            buildJobRepository.save(buildJob);
        }
    }

    private String buildDockerImage(File contextDir, String imageTag) {
        StringBuilder logBuilder = new StringBuilder();

        try {
            DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .build();

            ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .build();

            DockerClient dockerClient = DockerClientBuilder.getInstance(config)
                    .withDockerHttpClient(httpClient)
                    .build();

            Set<String> tags = new HashSet<>();
            tags.add(imageTag);

            BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(contextDir)
                    .withTags(tags)
                    .withNoCache(false)
                    .withPull(true);

            // Execute build
            String imageId = buildImageCmd.exec(new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    super.onNext(item);
                    if (item.getStream() != null) {
                        logBuilder.append(item.getStream());
                        log.debug("Build output: {}", item.getStream());
                    }
                }
            }).awaitImageId();

            logBuilder.append("\nImage built successfully: ").append(imageId);
            log.info("Docker image built: {}", imageId);

            return logBuilder.toString();

        } catch (Exception e) {
            log.error("Docker build failed: {}", e.getMessage(), e);
            throw new RuntimeException("Docker build failed: " + e.getMessage(), e);
        }
    }

    private void updateBuildStatus(Long buildJobId, BuildStatus status, String errorMessage) {
        buildJobRepository.findById(buildJobId).ifPresent(job -> {
            job.setStatus(status);
            if (errorMessage != null) {
                job.setBuildLog(job.getBuildLog() + "\nError: " + errorMessage);
            }
            job.setCompletedAt(LocalDateTime.now());
            buildJobRepository.save(job);
        });
    }

    /**
     * Get build job status
     */
    public BuildJob getBuildJob(Long buildJobId) {
        return buildJobRepository.findById(buildJobId)
                .orElseThrow(() -> new RuntimeException("Build job not found: " + buildJobId));
    }

    /**
     * Get all builds for an application
     */
    public java.util.List<BuildJob> getBuildsByApplication(Long applicationId) {
        return buildJobRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
    }
}
