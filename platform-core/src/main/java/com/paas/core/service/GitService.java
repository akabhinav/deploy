package com.paas.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Service
@Slf4j
public class GitService {

    private static final String BUILD_DIR = "/tmp/platform-builds";

    /**
     * Clone a Git repository
     */
    public File cloneRepository(String repositoryUrl, String branch, String commitSha) {
        try {
            // Create build directory
            File buildDir = new File(BUILD_DIR);
            if (!buildDir.exists()) {
                buildDir.mkdirs();
            }

            // Create unique directory for this build
            String dirName = "build-" + System.currentTimeMillis();
            File repoDir = new File(buildDir, dirName);
            repoDir.mkdirs();

            log.info("Cloning repository to: {}", repoDir.getAbsolutePath());

            // Execute git clone
            ProcessBuilder cloneBuilder = new ProcessBuilder(
                    "git", "clone", "--depth", "1", "--branch", branch, repositoryUrl, repoDir.getAbsolutePath()
            );
            cloneBuilder.redirectErrorStream(true);
            Process cloneProcess = cloneBuilder.start();

            // Read output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(cloneProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("Git clone: {}", line);
                }
            }

            int exitCode = cloneProcess.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Git clone failed with exit code: " + exitCode);
            }

            // Checkout specific commit if provided
            if (commitSha != null && !commitSha.isEmpty()) {
                ProcessBuilder checkoutBuilder = new ProcessBuilder(
                        "git", "checkout", commitSha
                );
                checkoutBuilder.directory(repoDir);
                checkoutBuilder.redirectErrorStream(true);
                Process checkoutProcess = checkoutBuilder.start();

                exitCode = checkoutProcess.waitFor();
                if (exitCode != 0) {
                    log.warn("Git checkout failed, using HEAD");
                }
            }

            log.info("Repository cloned successfully");
            return repoDir;

        } catch (Exception e) {
            log.error("Failed to clone repository: {}", e.getMessage(), e);
            throw new RuntimeException("Git clone failed: " + e.getMessage(), e);
        }
    }

    /**
     * Clean up build directory
     */
    public void cleanup(File directory) {
        try {
            if (directory != null && directory.exists()) {
                log.info("Cleaning up directory: {}", directory.getAbsolutePath());
                Files.walk(directory.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup directory: {}", e.getMessage());
        }
    }
}
