package com.example.project.utils;

import com.example.project.config.FrameworkConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates a deterministic artifact structure under target/ so each execution
 * preserves traces and screenshots without mixing files between runs.
 */
public final class ArtifactManager {
    private static final DateTimeFormatter RUN_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final String RUN_ID = "run_" + LocalDateTime.now().format(RUN_FORMATTER);

    private final Path runDirectory;
    private final Path screenshotsDirectory;
    private final Path tracesDirectory;
    private final Path videosDirectory;
    private final Path reportsDirectory;

    public ArtifactManager(FrameworkConfig frameworkConfig) {
        this.runDirectory = frameworkConfig.getArtifactsBaseDirectory().resolve(RUN_ID);
        this.screenshotsDirectory = runDirectory.resolve("screenshots");
        this.tracesDirectory = runDirectory.resolve("traces");
        this.videosDirectory = runDirectory.resolve("videos");
        this.reportsDirectory = frameworkConfig.getReportsBaseDirectory();
        createDirectories();
    }

    public Path getRunDirectory() {
        return runDirectory;
    }

    public Path buildScreenshotPath(String className, String methodName) {
        return screenshotsDirectory.resolve(buildFileName(className, methodName, "png"));
    }

    public Path buildTracePath(String className, String methodName) {
        return tracesDirectory.resolve(buildFileName(className, methodName, "zip"));
    }

    public Path buildVideoPath(String className, String methodName) {
        return videosDirectory.resolve(buildFileName(className, methodName, "webm"));
    }

    public Path getRawVideosDirectory() {
        return videosDirectory.resolve("raw");
    }

    public Path getEvidenceReportPath() {
        return reportsDirectory.resolve("playwright-evidence-report.html");
    }

    public Path getLatestRunSummaryPath() {
        return runDirectory.resolve("execution-summary.html");
    }

    private String buildFileName(String className, String methodName, String extension) {
        String timestamp = LocalDateTime.now().format(FILE_FORMATTER);
        return sanitize(className) + "_" + sanitize(methodName) + "_" + timestamp + "." + extension;
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void createDirectories() {
        try {
            Files.createDirectories(screenshotsDirectory);
            Files.createDirectories(tracesDirectory);
            Files.createDirectories(videosDirectory);
            Files.createDirectories(getRawVideosDirectory());
            Files.createDirectories(reportsDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create artifact directories under " + runDirectory, exception);
        }
    }
}
